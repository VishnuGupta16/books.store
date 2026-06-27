package gupta.vishnu.graphql.books.store.repo;

import gupta.vishnu.graphql.books.store.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface AuthorRepository extends JpaRepository<Author, Long> {
    Collection<Author> findAllByFirstNameContainsIgnoreCase(String query);
}
