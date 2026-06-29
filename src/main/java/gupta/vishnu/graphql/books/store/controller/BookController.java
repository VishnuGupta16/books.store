package gupta.vishnu.graphql.books.store.controller;

import gupta.vishnu.graphql.books.store.dto.BookAuthorInput;
import gupta.vishnu.graphql.books.store.dto.BookInput;
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
public class BookController {

    private final Logger log = LoggerFactory.getLogger(BookController.class);

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final BookAuthorRepository bookAuthorRepository;

    public BookController(BookRepository bookRepository, AuthorRepository authorRepository, BookAuthorRepository bookAuthorRepository) {
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


    //This causes N+1 query problem
//    @SchemaMapping(typeName = "Book", field = "authors")
//    public List<BookAuthor> authors(Book book) {
//        //book.getBookAuthors();
//        return bookAuthorRepository.findByBookId(book.getId());
//    }

    //omits books with no edges ( no book author entry in book author table)
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


}
