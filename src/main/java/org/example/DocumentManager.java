package org.example;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DocumentManager {

    private final Map<String, Document> documentStorage = new HashMap<>();
    private static final AtomicInteger count = new AtomicInteger(0);

    public Document save(Document document) {
        if (document.getId() != null && documentStorage.containsKey(document.getId())) {
            throw new IllegalArgumentException("Document already exists");
        }

        String documentId = document.getId() == null
                ? String.valueOf(count.incrementAndGet())
                : document.getId();

        document.setId(documentId);
        document.setCreated(Instant.now());
        documentStorage.put(documentId, document);
        return document;
    }

    public List<Document> search(SearchRequest request) {
        return documentStorage.values().stream()
                .filter(doc -> Stream.of(
                                isTitleMatch(request, doc),
                                isContentMatch(request, doc),
                                isAuthorMatch(request, doc),
                                isCreatedMatch(request, doc)
                        ).allMatch(v -> v)
                )
                .collect(Collectors.toList());
    }

    private static boolean isCreatedMatch(SearchRequest request, Document doc) {
        boolean createdFromMatch = request.getCreatedFrom() == null || !doc.getCreated().isBefore(request.getCreatedFrom());
        boolean createdToMatch = request.getCreatedTo() == null || !doc.getCreated().isAfter(request.getCreatedTo());
        return createdFromMatch && createdToMatch;
    }

    private static boolean isAuthorMatch(SearchRequest request, Document doc) {
        return request.getAuthorIds() == null || request.getAuthorIds().stream()
                .anyMatch(authorId -> doc.getAuthor().getId().equals(authorId));
    }

    private static boolean isContentMatch(SearchRequest request, Document doc) {
        return request.getContainsContents() == null || request.getContainsContents().stream()
                .anyMatch(content -> doc.getContent().contains(content));
    }

    private static boolean isTitleMatch(SearchRequest request, Document doc) {
        return request.getTitlePrefixes() == null ||
                request.getTitlePrefixes().stream()
                        .anyMatch(prefix -> doc.getTitle().startsWith(prefix));
    }

    public Optional<Document> findById(String id) {
        return Optional.ofNullable(documentStorage.get(id));
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }

}