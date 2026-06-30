package gupta.vishnu.graphql.books.store.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Integer rating;
    private Boolean verified;
    private String reviewerName;
    private String reviewerEmail;
    private String comment;
    @ManyToOne(fetch = FetchType.LAZY)
    private Book book;
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Integer getRating() {
        return rating;
    }

    public Boolean isVerified() {
        return verified;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public String getReviewerEmail() {
        return reviewerEmail;
    }

    public String getComment() {
        return comment;
    }

    public Book getBook() {
        return book;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public void setRating(Integer rating) {
        this.rating = rating;
    }
    public void setVerified(Boolean verified) {
        this.verified = verified;
    }
    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }
    public void setReviewerEmail(String reviewerEmail) {
        this.reviewerEmail = reviewerEmail;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
    public void setBook(Book book) {
        this.book = book;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
