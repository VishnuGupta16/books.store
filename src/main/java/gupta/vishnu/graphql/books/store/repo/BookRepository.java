package gupta.vishnu.graphql.books.store.repo;

import gupta.vishnu.graphql.books.store.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface BookRepository extends JpaRepository<Book, Long> {
    Collection<Book> findAllByTitleContainsIgnoreCase(String query);
}
