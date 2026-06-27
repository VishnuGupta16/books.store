package gupta.vishnu.graphql.books.store.dto;

import java.util.List;

public record BookInput(String name, String title, List<BookAuthorInput> authors){}
