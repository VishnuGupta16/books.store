package gupta.vishnu.graphql.books.store.dto;

import gupta.vishnu.graphql.books.store.entity.AuthorRole;

public record BookAuthorInput(Long bookId, Long authorId, AuthorRole authorRole) {
}
