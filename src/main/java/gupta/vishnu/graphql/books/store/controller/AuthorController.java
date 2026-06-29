package gupta.vishnu.graphql.books.store.controller;

import gupta.vishnu.graphql.books.store.entity.Author;
import gupta.vishnu.graphql.books.store.entity.BookAuthor;
import gupta.vishnu.graphql.books.store.repo.AuthorRepository;
import gupta.vishnu.graphql.books.store.repo.BookAuthorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class AuthorController {

    private Logger log = LoggerFactory.getLogger(AuthorController.class);

    private final AuthorRepository authorRepository;
    private final BookAuthorRepository bookAuthorRepository;

    public AuthorController(AuthorRepository authorRepository, BookAuthorRepository bookAuthorRepository) {
        this.authorRepository = authorRepository;
        this.bookAuthorRepository = bookAuthorRepository;
    }

    @QueryMapping
    public List<Author> authors() {
        log.info("Searching for authors");
        return authorRepository.findAll();
    }

    @QueryMapping
    public Author author(@Argument Long id) {
        log.info("Searching for author: {}", id);
        return authorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Author " + id + " not found"));
    }


    //This causes N+1 query problem
    //@SchemaMapping(typeName = "Author", field = "books")
//    public List<BookAuthor> books(Author author) {
//        return bookAuthorRepository.findByAuthorId(author.getId());
//    }


    //using Map
//    @BatchMapping(typeName = "Author", field = "books")
//    public Map<Author, List<BookAuthor>> books(List<Author> authors) {
//        log.info("Searching for books written by author: {}", authors.stream().map(Author::getId).collect(Collectors.toList()));
//        List<Long> ids = authors.stream().map(Author::getId).toList();
//        List<BookAuthor> all = bookAuthorRepository.findByAuthorIdIn(ids);
//        Map<Long, Author> passedIdVsAuthor = authors.stream().collect(Collectors.toMap(Author::getId,author -> author));
//        return all.stream().collect(Collectors.groupingBy(bookAuthor -> passedIdVsAuthor.get(bookAuthor.getAuthor().getId()), Collectors.toList()));
//    }

    //using list approach, need to make sure order of authors list is same as response
    @BatchMapping
    public List<List<BookAuthor>> books(List<Author> authors) {
        log.info("Searching for books for authors: {}", (Object) authors.stream().map(Author::getId).toArray(Long[]::new));
        List<Long> authorIds = authors.stream()
                .map(Author::getId)
                .toList();
        List<BookAuthor> bookAuthors = bookAuthorRepository.findByAuthorIdIn(authorIds);

        Map<Long, List<BookAuthor>> authorIdVsBooks = bookAuthors.stream()
                .collect(Collectors.groupingBy(bookAuthor ->  bookAuthor.getAuthor().getId()));

        return authors.stream() // keep input order, fill gaps
                .map(author -> authorIdVsBooks.getOrDefault(author.getId(), List.of()))
                .toList();

    }

    //This will not get role
//    @SchemaMapping(typeName = "Author", field = "books")
//    public List<Book> books(Author author) {
//        return author.getBookAuthors().stream()
//                .map(BookAuthor::getBook)
//                .toList();
//    }

    //This won't work as Graphql only pass immediate parent which is author and book is grandparent.  To pass other context other than immediate parent we need to use DataFetcherResult to set context as per need.
//    @SchemaMapping(typeName = "Author", field = "role")
//    public String role(Author author, Book book) {
//        return bookAuthorRepository.findByBookIdAndAuthorId(book.getId(), author.getId()).map(bookAuthor -> bookAuthor.getAuthorRole().name()).orElse( null);
//    }
}
