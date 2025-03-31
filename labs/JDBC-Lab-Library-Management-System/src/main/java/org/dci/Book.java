package org.dci;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class Book {
    private int bookId;;
    private String title;
    private String author;
    private int publicationYear;
    private boolean isAvailable;

    @Override
    public String toString() {
        return "Book{" +
                "ID = " + bookId +
                "| title = '" + title + '\'' +
                "| author = '" + author + '\'' +
                "| publicationYear = " + publicationYear +
                '}';
    }
}
