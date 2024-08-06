import org.example.DocumentManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DocumentManagerTest {

    private DocumentManager documentManager;

    @BeforeEach
    void setUp() {
        documentManager = new DocumentManager();
    }

    @Test
    void testSaveNewDocument() {
        DocumentManager.Author author = DocumentManager.Author.builder().id("1").name("Taras").build();
        DocumentManager.Document document = DocumentManager.Document.builder()
                .title("Document One")
                .content("Lyrics")
                .author(author)
                .build();

        DocumentManager.Document savedDocument = documentManager.save(document);

        assertThat(savedDocument).isNotNull();
        assertThat(savedDocument.getId()).isNotNull();
        assertThat(savedDocument.getCreated()).isNotNull();
        assertThat(savedDocument.getTitle()).isEqualTo("Document One");
    }

    @Test
    void testSaveExistingDocumentThrowsException() {
        DocumentManager.Author author = DocumentManager.Author.builder().id("1").name("Taras").build();
        DocumentManager.Document document = DocumentManager.Document.builder()
                .id("1")
                .title("Document One")
                .content("Lyrics")
                .author(author)
                .build();

        documentManager.save(document);

        assertThatThrownBy(() -> documentManager.save(document))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Document already exists");
    }

    @Test
    void testSearchWithVariousRequests() {
        DocumentManager.Author author1 = DocumentManager.Author.builder().id("1").name("Taras").build();
        DocumentManager.Author author2 = DocumentManager.Author.builder().id("2").name("Larysa").build();

        DocumentManager.Document document1 = DocumentManager.Document.builder()
                .title("Document One")
                .content("Lyrics")
                .author(author1)
                .created(Instant.now().minusSeconds(3600))
                .build();

        DocumentManager.Document document2 = DocumentManager.Document.builder()
                .title("Document Two")
                .content("Writings")
                .author(author2)
                .created(Instant.now())
                .build();

        documentManager.save(document1);
        documentManager.save(document2);

        // Test title prefix search
        DocumentManager.SearchRequest titleRequest = DocumentManager.SearchRequest.builder()
                .titlePrefixes(Arrays.asList("Document"))
                .build();

        List<DocumentManager.Document> results = documentManager.search(titleRequest);
        assertThat(results).hasSize(2);
        assertThat(results).extracting("title").containsExactlyInAnyOrder("Document One", "Document Two");

        // Test content search
        DocumentManager.SearchRequest contentRequest = DocumentManager.SearchRequest.builder()
                .containsContents(Arrays.asList("Lyrics", "Writings"))
                .build();

        results = documentManager.search(contentRequest);
        assertThat(results).hasSize(2);
        assertThat(results).extracting("title").containsExactlyInAnyOrder("Document One", "Document Two");

        // Test author search
        DocumentManager.SearchRequest authorRequest = DocumentManager.SearchRequest.builder()
                .authorIds(Arrays.asList("1"))
                .build();

        results = documentManager.search(authorRequest);
        assertThat(results).hasSize(1);
        assertThat(results).extracting("title").containsExactly("Document One");

        // Test date range search
        Instant now = Instant.now();
        DocumentManager.SearchRequest dateRequest = DocumentManager.SearchRequest.builder()
                .createdFrom(now.minusSeconds(7200))
                .createdTo(now.plusSeconds(3600))
                .build();

        results = documentManager.search(dateRequest);
        assertThat(results).hasSize(2);
        assertThat(results).extracting("title").containsExactlyInAnyOrder("Document One", "Document Two");
    }

    @Test
    void testFindById() {
        DocumentManager.Author author = DocumentManager.Author.builder().id("1").name("Taras").build();
        DocumentManager.Document document = DocumentManager.Document.builder()
                .title("Document One")
                .content("Lyrics")
                .author(author)
                .build();

        DocumentManager.Document savedDocument = documentManager.save(document);
        Optional<DocumentManager.Document> foundDocument = documentManager.findById(savedDocument.getId());

        assertThat(foundDocument).isPresent();
        assertThat(foundDocument.get().getTitle()).isEqualTo("Document One");
    }
}