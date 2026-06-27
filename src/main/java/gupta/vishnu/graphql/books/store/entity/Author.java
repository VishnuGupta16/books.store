package gupta.vishnu.graphql.books.store.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String firstName="";
    private String lastName="";
    private String email="";
    private String phoneNumber="";

    @OneToMany(mappedBy = "author")
    private List<BookAuthor> bookAuthors = new ArrayList<>();

    protected Author() { }

    public long getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public List<BookAuthor> getBookAuthors() { return bookAuthors; }
    public void setBookAuthors(List<BookAuthor> bookAuthors) { this.bookAuthors = bookAuthors; }

//    @Override
//    public boolean equals(Object obj) {
//        if(this == obj) return true;
//        if(obj == null) return false;
//        if(getClass() != obj.getClass()) return false;
//        if(id != ((Author)obj).id) return false;
//        if(!Objects.equals(firstName, ((Author) obj).firstName)) return false;
//        if(!Objects.equals(lastName, ((Author) obj).lastName)) return false;
//        if(!Objects.equals(email, ((Author) obj).email)) return false;
//        if(!Objects.equals(phoneNumber, ((Author) obj).phoneNumber)) return false;
//        return bookAuthors.equals(((Author)obj).bookAuthors);
//    }
//
//    @Override
//    public int hashCode() {
//        return Long.hashCode(id) + firstName.hashCode() + lastName.hashCode() + email.hashCode() + phoneNumber.hashCode();
//    }

}
