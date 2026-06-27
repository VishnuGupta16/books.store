package gupta.vishnu.graphql.books.store.entity;

import gupta.vishnu.graphql.books.store.dto.AuthorRole;
import jakarta.persistence.*;

@Entity
public class BookAuthor {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Author author;

    @Enumerated(EnumType.STRING)
    private AuthorRole authorRole = AuthorRole.CO_AUTHOR;

    protected BookAuthor() { }

    public BookAuthor(Book book, Author author, AuthorRole authorRole) {
        this.book = book;
        this.author = author;
        this.authorRole = authorRole;
    }

    public long getId() { return id; }
    public Book getBook() { return book; }
    public Author getAuthor() { return author; }
    public AuthorRole getAuthorRole() { return authorRole; }

    //Not using equal and hash in entity as it may break for same object for multiple reasons.
//    @Override
//    public boolean equals(Object obj) {
//        if(this == obj) return true;
//        if(obj == null) return false;
//        if(getClass() != obj.getClass()) return false;
//        return id == ((BookAuthor) obj).getId();
//    }
//
//    @Override
//    public int hashCode() {
//        return Long.hashCode(id)+authorRole.hashCode();
//    }
}
