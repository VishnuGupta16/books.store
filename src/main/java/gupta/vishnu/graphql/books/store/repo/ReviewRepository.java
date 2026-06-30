package gupta.vishnu.graphql.books.store.repo;

import gupta.vishnu.graphql.books.store.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.graphql.data.GraphQlRepository;


//This give out of the box graphql resolver only in spring graphql
@GraphQlRepository
public interface ReviewRepository extends JpaRepository<Review, Long> { }
