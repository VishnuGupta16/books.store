package gupta.vishnu.graphql.books.store.dto;

public record BookAuthorInput(Long bookId, Long authorId, AuthorRole authorRole) {
}
