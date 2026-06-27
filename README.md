# books.store

A small **GraphQL bookstore API** built with Spring for GraphQL and JPA. It models a **many-to-many relationship between books and authors**, where each book↔author link also carries a **role** (main author, co-author, editor). The project is a learning/interview vehicle, so it intentionally mirrors a production-style stack and focuses on getting the GraphQL layer right.

## Tech stack

- Java + Spring Boot
- Spring for GraphQL (`spring-boot-starter-graphql`) — schema-first, controller-based resolvers
- Spring Data JPA + Hibernate
- PostgreSQL
- Gradle

## Domain model

Books and authors don't reference each other directly. They are joined through a **link entity**, `BookAuthor`, which also stores the role for that specific pairing:

```
Book  --<  BookAuthor  >--  Author
                |
            authorRole   (MAIN_AUTHOR / CO_AUTHOR / EDITOR)
```

Each row in `book_author` is one "this book <-> this author, in this role" connection. This is why a book can have many authors and an author many books — and, crucially, why `role` lives on the link rather than on either side.

Entities:
- `Book` — `id`, `name`, `title`, and `List<BookAuthor> bookAuthors`
- `Author` — `id`, `firstName`, `lastName`, `email`, `phoneNumber`, and `List<BookAuthor> bookAuthors`
- `BookAuthor` — the link: `@ManyToOne book`, `@ManyToOne author`, `authorRole`

Both `@ManyToOne` associations are **`FetchType.LAZY`** (eager `@ManyToOne` is an N+1 trap — see Performance below).

## Why an "edge" type in the schema

`role` is **not** a property of an author — an author can be MAIN_AUTHOR on one book and EDITOR on another. It's a property of the **relationship**. So the schema exposes the relationship itself as `BookAuthorEdge` (the GraphQL mirror of the `BookAuthor` link), and both `Book.authors` and `Author.books` return **edges**, not bare entities:

```graphql
type BookAuthorEdge {
    authorRole: String!
    author: Author!
    book: Book!
}

type Book {
    id: Int!
    title: String!
    name: String!
    authors: [BookAuthorEdge!]!
}

type Author {
    id: Int!
    firstName: String!
    lastName: String
    books: [BookAuthorEdge!]!
}
```

Because the edge knows **both** ends, `authorRole` resolves cleanly — no need to reach a "grandparent" book from an author. (An earlier attempt to put `role` on `Author` failed precisely because a bare `Author` has no book in context.)

## Queries

```graphql
type Query {
    books: [Book!]!
    book(id: ID): Book!
    author(id: ID): Author!
    authors: [Author!]!
    search(query: String!): [SearchBookOrAuthor]   # union result
}

union SearchBookOrAuthor = Author | Book
```

### Example: books with their authors and roles
```graphql
query {
  books {
    title
    authors {
      authorRole
      author { firstName lastName }
    }
  }
}
```

### Example: an author with the books they worked on
```graphql
query {
  author(id: 1) {
    firstName
    books {
      authorRole
      book { title }
    }
  }
}
```

## Search (union type)

`search(query)` returns either books or authors in a single list, modeled as a **GraphQL union**:

```graphql
union SearchBookOrAuthor = Author | Book

type Query {
    search(query: String!): [SearchBookOrAuthor]
}
```

The resolver lives in `SearchController`. It runs a case-insensitive "contains" match against author first names and book titles, and returns a mixed `List<Object>`:

```java
@QueryMapping
public List<Object> search(@Argument String query) {
    List<Object> result = new ArrayList<>();
    result.addAll(authorRepository.findAllByFirstNameContainsIgnoreCase(query));
    result.addAll(bookRepository.findAllByTitleContainsIgnoreCase(query));
    return result;
}
```

**How the union resolves to a concrete type:** Spring for GraphQL's default `ClassNameTypeResolver` maps each returned object to the GraphQL type whose name matches its Java class — an `Author` instance -> the `Author` type, a `Book` instance -> the `Book` type. Because the class names already match the union members, no custom `TypeResolver` is needed.

Example:
```graphql
query {
  search(query: "or") {
    __typename
    ... on Author { firstName lastName }
    ... on Book   { title }
  }
}
```

## Performance: avoiding the N+1 problem

A naive resolver that loads each book's authors one-by-one produces **1 + N** queries (one for the list, one per book). This project removes that on both directions of the relationship.

**1. Batch the per-parent loads with `@BatchMapping`.** Instead of being called once per parent, the resolver receives *all* parents at once and returns a `Map<Parent, List<edge>>`:

```java
@BatchMapping(typeName = "Book", field = "authors")
public Map<Book, List<BookAuthor>> authors(List<Book> books) {
    List<Long> ids = books.stream().map(Book::getId).toList();
    List<BookAuthor> all = bookAuthorRepository.findByBookIdIn(ids);

    // key the result by the SAME Book instances Spring passed in (by id),
    // instead of relying on entity equals/hashCode or the Hibernate session cache
    Map<Long, Book> passedIdVsBook = books.stream()
            .collect(Collectors.toMap(Book::getId, b -> b));
    return all.stream().collect(Collectors.groupingBy(
            ba -> passedIdVsBook.get(ba.getBook().getId()),
            Collectors.toList()));
}
```
`Author.books` is batched the same way via `findByAuthorIdIn`.

**Why the id-map keying:** entity `equals`/`hashCode` are deliberately *not* implemented here. Keying the result by the original `Book` instances (looked up by id) makes the batch correct regardless of `equals`/`hashCode` or whether Open-Session-In-View is on — and it avoids accidentally triggering lazy loads inside a hashing call.

**2. Fetch the far side in the same query with JPQL `join fetch`.** Because the associations are LAZY, resolving each edge's `author` would re-introduce N+1 at access time. The repository query loads the edges **and** their authors in one SQL statement:

```java
@Query("select ba from BookAuthor ba join fetch ba.author where ba.book.id in :bookIds")
List<BookAuthor> findByBookIdIn(List<Long> bookIds);

@Query("select ba from BookAuthor ba join fetch ba.book where ba.author.id in :authorIds")
List<BookAuthor> findByAuthorIdIn(List<Long> authorIds);
```
(`join fetch` is JPQL, not SQL — it operates on entity associations and Hibernate compiles it to a real SQL join.)

Net result for `books { authors { author { ... } } }`: **one** query for books, **one** for the edges-with-authors — instead of a per-book and per-author cascade.

## Project layout

```
src/main/java/.../books/store/
|-- controller/
|     |-- BookAuthorController.java   # book/author queries + @BatchMapping resolvers
|     `-- SearchController.java        # union search query
|-- entity/      Book, Author, BookAuthor, (AuthorRole)
|-- repo/        BookRepository, AuthorRepository, BookAuthorRepository
`-- BookStoreApplication.java
src/main/resources/
|-- graphql/     book.graphqls, author.graphqls, book-author.graphqls,
|                query.graphqls, mutation.graphqls, unions.graphqls
`-- import.sql   # seed data (5 authors, 10 books, book_author links with roles)
```

## Running

1. Start PostgreSQL (a `compose.yaml` is included).
2. Run the app:
   ```bash
   ./gradlew bootRun
   ```
3. Open the GraphiQL playground (enabled in `application.properties`) and run the example queries above.

## Notes & learnings captured so far

- `@ManyToOne` defaults to **EAGER**, which silently causes N+1; made both LAZY and fetched explicitly.
- LAZY alone doesn't remove N+1 — it moves it to access time. Pair LAZY with `join fetch` (or a batched resolver) to actually collapse the queries.
- Relationship-specific data (`role`) belongs on an **edge type**, not on either entity.
- `@BatchMapping` is Spring's wrapper over the **DataLoader** pattern: field loads are queued and dispatched once per query level.

## Known follow-ups (not yet done)

- Batch `Map` return omits parents that have zero links -> a parent with no edges would yield `null` for a non-null list field. Gap-fill with an empty list (or use the index-aligned `List<List<...>>` form).
