package gupta.vishnu.graphql.books.store.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String title;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    private List<BookAuthor> bookAuthors = new ArrayList<>();

    protected Book() { }

    public Book(String name, String title) {
        this.name = name;
        this.title = title;
    }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setTitle(String title) { this.title = title; }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getTitle() { return title; }
    public List<BookAuthor> getBookAuthors() { return bookAuthors; }
    public void setBookAuthors(List<BookAuthor> bookAuthors) { this.bookAuthors = bookAuthors; }

//    @Override
//    public boolean equals(Object obj) {
//        if(this == obj) return true;
//        if(obj == null) return false;
//        if(getClass() != obj.getClass()) return false;
//        if(id != ((Book)obj).id) return false;
//        if(name != ((Book)obj).name) return false;
//        if(title != ((Book)obj).title) return false;
//        return bookAuthors.equals(((Book)obj).bookAuthors);
//    }
//
//    @Override
//    public int hashCode() {
//        return Long.hashCode(id)+name.hashCode()+title.hashCode()+bookAuthors.hashCode();
//    }
}
