package gupta.vishnu.graphql.books.store.controller;

import gupta.vishnu.graphql.books.store.dto.BookAuthorInput;
import gupta.vishnu.graphql.books.store.dto.BookInput;
import gupta.vishnu.graphql.books.store.entity.Author;
import gupta.vishnu.graphql.books.store.entity.Book;
import gupta.vishnu.graphql.books.store.entity.BookAuthor;
import gupta.vishnu.graphql.books.store.repo.AuthorRepository;
import gupta.vishnu.graphql.books.store.repo.BookAuthorRepository;
import gupta.vishnu.graphql.books.store.repo.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.*;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class BookAuthorController {

    private final Logger log = LoggerFactory.getLogger(BookAuthorController.class);

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final BookAuthorRepository bookAuthorRepository;

    public BookAuthorController(BookRepository bookRepository, AuthorRepository authorRepository, BookAuthorRepository bookAuthorRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.bookAuthorRepository = bookAuthorRepository;
    }

    //@SchemaMapping(typeName = "Query", field = "books")
    //@QueryMapping(name = "books")
    @QueryMapping
    public List<Book> books() {
        log.info("Getting all books");
        return bookRepository.findAll();
    }

    @QueryMapping
    public Book book(@Argument Long id) {
        log.info("Searching for book with id: {}", id);
        return bookRepository.findById(id).orElseThrow(() -> new RuntimeException("Book with id:" + id + " not found."));
    }

//This will not get role
//    @SchemaMapping(typeName = "Book", field = "authors")
//    public List<Author> authors(Book book) {
//        return book.getBookAuthors().stream()
//                .map(BookAuthor::getAuthor)
//                .toList();
//    }

    //This will not get role
//    @SchemaMapping(typeName = "Author", field = "books")
//    public List<Book> books(Author author) {
//        return author.getBookAuthors().stream()
//                .map(BookAuthor::getBook)
//                .toList();
//    }

    //This causes N+1 query problem
//    @SchemaMapping(typeName = "Book", field = "authors")
//    public List<BookAuthor> authors(Book book) {
//        //book.getBookAuthors();
//        return bookAuthorRepository.findByBookId(book.getId());
//    }

    @BatchMapping(typeName = "Book", field = "authors")
    public Map<Book, List<BookAuthor>> authors(List<Book> books) {
        log.info("Authors for books: {}", books);
        List<Long> ids = books.stream().map(Book::getId).toList();
        List<BookAuthor> all = bookAuthorRepository.findByBookIdIn(ids);
        //Not using equals and hash code as it might still break. In some case N+1 query still be present
       // return all.stream().collect(Collectors.groupingBy(BookAuthor::getBook, Collectors.toList()));

        Map<Long, Book> passedIdVsBook = books.stream().collect(Collectors.toMap(Book::getId,book -> book));
        return all.stream().collect(Collectors.groupingBy(bookAuthor -> passedIdVsBook.get(bookAuthor.getBook().getId()), Collectors.toList()));
    }

    //This causes N+1 query problem
    //@SchemaMapping(typeName = "Author", field = "books")
//    public List<BookAuthor> books(Author author) {
//        return bookAuthorRepository.findByAuthorId(author.getId());
//    }

    @BatchMapping(typeName = "Author", field = "books")
    public Map<Author, List<BookAuthor>> books(List<Author> authors) {
        log.info("Searching for books written by author: {}", authors.stream().map(Author::getId).collect(Collectors.toList()));
        List<Long> ids = authors.stream().map(Author::getId).toList();
        List<BookAuthor> all = bookAuthorRepository.findByAuthorIdIn(ids);
        Map<Long, Author> passedIdVsAuthor = authors.stream().collect(Collectors.toMap(Author::getId,author -> author));
        return all.stream().collect(Collectors.groupingBy(bookAuthor -> passedIdVsAuthor.get(bookAuthor.getAuthor().getId()), Collectors.toList()));
    }


    //This won't work as Graphql only pass immediate parent which is author and book is grandparent.  To pass other context other than immediate parent we need to use DataFetcherResult to set context as per need.
//    @SchemaMapping(typeName = "Author", field = "role")
//    public String role(Author author, Book book) {
//        return bookAuthorRepository.findByBookIdAndAuthorId(book.getId(), author.getId()).map(bookAuthor -> bookAuthor.getAuthorRole().name()).orElse( null);
//    }

    @MutationMapping
    public Book createBook(@Argument BookInput bookInput) {
        log.info("Creating new book: {}", bookInput);
        List<BookAuthorInput> bookAuthors = bookInput.authors();
        Book book = bookRepository.save(new Book(bookInput.name(), bookInput.title()));
        bookAuthors.forEach(bookAuthor -> bookAuthorRepository.save(new BookAuthor(book,
                authorRepository.findById(bookAuthor.authorId()).orElseThrow(),
                bookAuthor.authorRole())));
        return book;

    }

    @QueryMapping
    public Author author(@Argument Long id) {
        log.info("Searching for author: {}", id);
        return authorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Author " + id + " not found"));
    }

    @QueryMapping
    public List<Author> authors() {
        log.info("Searching for authors");
        return authorRepository.findAll();
    }

}
