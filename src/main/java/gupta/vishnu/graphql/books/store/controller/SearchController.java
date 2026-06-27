package gupta.vishnu.graphql.books.store.controller;

import gupta.vishnu.graphql.books.store.repo.AuthorRepository;
import gupta.vishnu.graphql.books.store.repo.BookRepository;
import jakarta.websocket.OnClose;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;

@Controller
public class SearchController {

    private Logger logger = LoggerFactory.getLogger(SearchController.class);
    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;

    public SearchController(BookRepository bookRepository, AuthorRepository authorRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
    }

    @QueryMapping
    public List<Object> search(@Argument String query) {
        logger.info("Searching for query: {}", query);
        List<Object> result = new ArrayList<>();
        result.addAll(this.authorRepository.findAllByFirstNameContainsIgnoreCase(query));
        result.addAll(this.bookRepository.findAllByTitleContainsIgnoreCase(query));
        return result;

    }
}
