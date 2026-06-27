package gupta.vishnu.graphql.books.store.repo;

import gupta.vishnu.graphql.books.store.entity.BookAuthor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BookAuthorRepository extends JpaRepository<BookAuthor, Long> {
    List<BookAuthor> findByBookId(Long bookId);
    List<BookAuthor> findByAuthorId(Long authorId);
    Optional<BookAuthor> findByBookIdAndAuthorId(Long bookId, Long authorId);
    //This is join query but writen JPQL ( select ba1_0.id,a1_0.id,a1_0.email,a1_0.first_name,a1_0.last_name,a1_0.phone_number,ba1_0.author_role,ba1_0.book_id from book_author ba1_0 join author a1_0 on a1_0.id=ba1_0.author_id where ba1_0.book_id in (?,?,?,?,?,?,?,?,?,?))
    @Query("select ba from BookAuthor ba join fetch ba.author where ba.book.id in :bookIds")
    List<BookAuthor> findByBookIdIn(List<Long> bookIds);
    @Query("select ba from BookAuthor ba join fetch ba.book where ba.author.id in :authorIds")
    List<BookAuthor> findByAuthorIdIn(List<Long> authorIds);
}
