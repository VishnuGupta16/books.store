package gupta.vishnu.graphql.books.store.dto;

import gupta.vishnu.graphql.books.store.entity.AuthorRole;

public record AuthorInput(String firstName, String lastName, AuthorRole role) {
}
