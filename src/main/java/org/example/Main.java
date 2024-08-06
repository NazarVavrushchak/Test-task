package org.example;

import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        var documentManager = new DocumentManager();

        var author1 = DocumentManager.Author.builder().id("1").name("Taras").build();
        var author2 = DocumentManager.Author.builder().id("2").name("Larysa").build();

        var document1 = DocumentManager.Document.builder()
                .title("Document One")
                .content("lyrics")
                .author(author1)
                .build();

        var document2 = DocumentManager.Document.builder()
                .title("Document Two")
                .content("writings")
                .author(author2)
                .build();

        // Save documents
        documentManager.save(document1);
        documentManager.save(document2);

        // Find documents which match with request
        var searchRequest = DocumentManager.SearchRequest.builder()
                .titlePrefixes(List.of("Document"))
                .build();

        List<DocumentManager.Document> searchResults = documentManager.search(searchRequest);
        System.out.println("Search results for documents starting with 'Document':");
        searchResults.forEach(doc -> System.out.println(doc.getTitle()));

        // Search documents by content
        var contentSearchRequest = DocumentManager.SearchRequest.builder()
                .containsContents(Arrays.asList("lyrics", "writings"))
                .build();

        searchResults = documentManager.search(contentSearchRequest);
        System.out.println("\nSearch results for documents containing 'lyrics' or 'writings':");
        searchResults.forEach(doc -> System.out.println(doc.getTitle()));

        // Search documents by author ID
        var authorSearchRequest = DocumentManager.SearchRequest.builder()
                .authorIds(List.of("1"))
                .build();

        searchResults = documentManager.search(authorSearchRequest);
        System.out.println("\nSearch results for documents by Author One:");
        searchResults.forEach(doc -> System.out.println(doc.getTitle()));

        // Find document by ID
        var foundDocumentById = documentManager.findById(document1.getId());
        foundDocumentById.ifPresent(doc -> System.out.println("\nDocument found with ID " + document1.getId() + ": " + doc.getTitle()));
    }
}