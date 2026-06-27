# Book Store — Federated GraphQL Project Requirements

A build-it-yourself spec for **books.store**: a small online bookstore backend exposed through a single federated GraphQL API. It mirrors GetYourGuide's real stack (Java + Spring Boot + Netflix DGS, federated subgraphs behind an Apollo Router) so the project doubles as interview material. This document says **what to build and how to know you're done** — you write the code.

> Federation is ambitious. The milestones are ordered so you have a **working, demoable app after Milestone 3**, before the harder federation pieces. Build in order.

---

## 1. Overview

**What you're building:** a backend for browsing books, looking up authors, and placing orders — a stripped-down Amazon Books / bookstore.

**Why this domain:** books map cleanly onto the same patterns as GYG's activities platform — a catalog, related entities to join across services, search/pagination, and a transactional order flow. Perfect for practicing schema design, resolvers, N+1/DataLoader, mutations, authorization, and federation.

**Scope discipline:** one demo user browsing books and placing orders. No real payments, no real email. Keep the domain small; make the GraphQL layer excellent.

---

## 2. Architecture (target end state)

Three subgraphs composed into one supergraph by an Apollo Router:

```
                 ┌─────────────────────┐
   GraphQL  ───► │   Apollo Router      │   (gateway / supergraph)
   client        │  (composes subgraphs)│
                 └──────────┬───────────┘
            ┌───────────────┼────────────────┐
            ▼               ▼                 ▼
   ┌────────────────┐ ┌──────────────┐ ┌──────────────────┐
   │ Catalog         │ │ Authors      │ │ Orders            │
   │ subgraph (DGS)  │ │ subgraph(DGS)│ │ subgraph (DGS)    │
   └────────────────┘ └──────────────┘ └──────────────────┘
```

- **Catalog subgraph** — books, search, reviews.
- **Authors subgraph** — author details, joined onto books.
- **Orders subgraph** — placing and reading orders (cart-free, single-book orders to start).

Each subgraph is its own Spring Boot app with its own DGS endpoint and data store. The router composes them so a client can query a book *and* its author *and* order it through one endpoint.

**Subgraph** = one service's slice of the schema. **Supergraph** = all subgraphs composed. **Entity** = a type (e.g. `Book`) that more than one subgraph contributes fields to, joined by a `@key`.

---

## 3. Tech stack (required)

- **Java 17+** and **Spring Boot 3.x**
- **Netflix DGS** for GraphQL (annotation-based, schema-first, codegen)
- **Apollo Federation v2** + **Apollo Router** (use `rover` for schema composition)
- **Data:** in-memory or H2 to start; Postgres optional later. The graph is the point, not persistence.
- **Build:** Gradle (best DGS support) or Maven
- **Testing:** JUnit + DGS `DgsQueryExecutor`
- **Optional later:** Docker Compose to run all three subgraphs + router

---

## 4. Functional requirements

### 4.1 Catalog subgraph

```graphql
type Book @key(fields: "id") {
  id: ID!
  title: String!
  description: String!
  isbn: String!
  genre: Genre!
  price: Money!
  publishedYear: Int!
  authorId: ID!              # used by federation to join to Authors
  reviews(first: Int = 10): [Review!]!
  averageRating: Float
  inStock: Boolean!
}

enum Genre { FICTION NONFICTION SCIFI FANTASY MYSTERY BIOGRAPHY TECH }
type Review { id: ID!  rating: Int!  text: String  reviewer: String! }
type Money  { amount: Int!  currency: String! }   # amount in cents

type Query {
  book(id: ID!): Book
  searchBooks(genre: Genre, query: String, first: Int = 20, after: String): BookConnection!
}

# cursor-based pagination
type BookConnection { edges: [BookEdge!]!  pageInfo: PageInfo! }
type BookEdge { node: Book!  cursor: String! }
type PageInfo { hasNextPage: Boolean!  endCursor: String }
```

Requirements:
- `searchBooks` filters by genre and/or a free-text `query` (title/description) with **cursor-based pagination**.
- `averageRating` is **computed** from reviews (derived field), not stored.
- Seed at least 12 books across 4+ genres and 4+ authors.

### 4.2 Authors subgraph

```graphql
type Author @key(fields: "id") {
  id: ID!
  name: String!
  bio: String
  country: String
}

# extend the Book entity owned by the Catalog subgraph
type Book @key(fields: "authorId") {
  authorId: ID! @external
  author: Author!            # this subgraph resolves the author for a book
}

type Query { author(id: ID!): Author }
```

Requirement: a query like `book(id:"1"){ title author { name country } }` must work — `title` from Catalog, `author` from Authors, joined by the router. **This is the core federation deliverable.**

### 4.3 Orders subgraph

```graphql
type Order @key(fields: "id") {
  id: ID!
  bookId: ID!
  quantity: Int!
  status: OrderStatus!
  totalPrice: Money!
  createdAt: String!
}

enum OrderStatus { CONFIRMED PENDING FAILED }

type Mutation {
  placeOrder(input: PlaceOrderInput!): OrderResult!
}

input PlaceOrderInput { bookId: ID!  quantity: Int! }
type OrderResult { order: Order  errors: [String!]! }

type Query { order(id: ID!): Order  myOrders: [Order!]! }
```

Requirements:
- `placeOrder` validates input (quantity > 0, book exists and is in stock) and returns expected failures in the `errors` array rather than throwing.
- `totalPrice` = book price × quantity (fetch price via the router/internal call; note the cross-service dependency in your README).

---

## 5. Cross-cutting requirements (the "senior" details)

1. **Solve N+1 with DataLoader.** `reviews` and the `author` join must use a DGS `DataLoader` so loading 20 books doesn't fire 20 separate review/author calls.
2. **Authorization.** Require an API key / bearer token header for `placeOrder` and `myOrders`. Bonus: extract it into a small shared module used by more than one subgraph — exactly what GYG did with their shared auth library.
3. **Error handling.** Expected failures → `errors` array + partial data; unexpected → GraphQL errors.
4. **Tests.** At least one DGS query test per subgraph and one proving the federated join works through the router.
5. **Observability (stretch).** Log resolver timings; health endpoint per service.
6. **Schema checks.** Use `rover` to compose subgraph schemas and catch composition errors before running.

---

## 6. Milestones (build in this order)

**Milestone 0 — Setup.** One DGS Spring Boot app from the starter; `{ __typename }` returning. *Done when:* GraphiQL loads at `/graphiql`.

**Milestone 1 — Catalog, single service.** `Book`, `Review`, `book(id)`, `searchBooks` (no pagination yet), seeded in memory. *Done when:* you can query a book and its reviews.

**Milestone 2 — Make it real.** Cursor pagination on `searchBooks`, computed `averageRating`, DataLoader for `reviews`, first DGS test. *Done when:* pagination works and the test passes.

**Milestone 3 — Orders + mutations + auth.** Orders subgraph, `placeOrder` with validation, header-based auth. *Done when:* you can place an order and a bad input returns a clean error. **You now have a demoable app.**

**Milestone 4 — Federation.** Authors subgraph, Apollo Federation v2 (`@key`, entity resolvers), Apollo Router, composed supergraph. *Done when:* one query through the router returns a book + its author + lets you order it.

**Milestone 5 — Polish (any).** Shared auth module, Docker Compose, observability/logging, Postgres, federated test through the router.

---

## 7. Acceptance criteria (definition of done)

- [ ] A single router query returns fields resolved by **all three** subgraphs.
- [ ] `searchBooks` supports genre/text filter + cursor pagination.
- [ ] `averageRating` is computed, not stored.
- [ ] No N+1: batched DataLoaders for reviews and authors.
- [ ] `placeOrder` validates input and returns errors gracefully.
- [ ] `myOrders` / `placeOrder` require auth; unauthenticated calls rejected.
- [ ] At least 3 passing tests, including one federated query.
- [ ] A `README.md` explaining architecture, how to run, and trade-offs you made (your interview script).

---

## 8. Stretch goals (after acceptance criteria pass)

- A `Subscription` for order status changes (real-time).
- Persisted queries + a depth/complexity limit (security + cost control).
- An **AI-assisted field** to align with GYG's direction: e.g. `aiSummary` on `Book` summarizing its reviews via an LLM (keep it behind an interface so it's mockable; add a fallback when the model is unavailable). Be ready to discuss risks and human-in-the-loop framing.
- A tiny Vue or React page querying the router (GYG's frontend is Vue/TypeScript).

---

## 9. Keep a build journal

One or two sentences per decision: why cursor over offset pagination, why DataLoader, why errors-in-payload vs thrown, what broke during federation composition. These become your STAR stories and your senior "project retrospective." When asked "tell me about a project," you'll have a real, GYG-shaped system to walk through.

---

## 10. Resources

- Netflix DGS docs — getting started, data loaders, federation, testing.
- Apollo Federation v2 docs — `@key`, entities, the Router, `rover` composition.
- GYG blog: "GraphQL Federation Architecture at GetYourGuide" — your project retraces their journey.
- GraphQL.org — schema, queries, mutations, best practices.

Start at Milestone 0 today. Get one book query returning before you think about federation.
