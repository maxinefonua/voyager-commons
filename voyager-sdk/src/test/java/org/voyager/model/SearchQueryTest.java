package org.voyager.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class SearchQueryTest {

    @Test
    void builderQuery() {
        assertThrows(NullPointerException.class,()->SearchQuery.builder().withQuery(null).build());
        assertThrows(IllegalArgumentException.class,()->SearchQuery.builder().withQuery("").build());
        assertThrows(IllegalArgumentException.class,()->SearchQuery.builder().withQuery("  ").build());

        SearchQuery searchQuery = SearchQuery.builder().withQuery("test query").build();
        assertEquals("test query",searchQuery.getQuery());
        assertEquals("/search?q=test query",searchQuery.getRequestURL());
    }

    @Test
    void getRequestURL() {
        assertThrows(NullPointerException.class,()->SearchQuery.builder().build());

        SearchQuery searchQuery = SearchQuery.builder().withQuery("test query").withLimit(10)
                .withSkipRowCount(25).build();
        assertEquals("/search?q=test query&skipRowCount=25&limit=10",searchQuery.getRequestURL());
    }

    @Test
    void builderSkipRowCount() {
        assertThrows(NullPointerException.class,()->SearchQuery.builder().withSkipRowCount(null).build());
        assertThrows(IllegalArgumentException.class,()->SearchQuery.builder().withQuery("test query")
                .withSkipRowCount(0).build());

        SearchQuery searchQuery = SearchQuery.builder().withQuery("test query").withSkipRowCount(10).build();
        assertEquals(10,searchQuery.getSkipRowCount());
        assertEquals("/search?q=test query&skipRowCount=10",searchQuery.getRequestURL());
    }

    @Test
    void getLimit() {
        assertThrows(NullPointerException.class,()->SearchQuery.builder().withLimit(null).build());
        assertThrows(IllegalArgumentException.class,()->SearchQuery.builder().withQuery("test query")
                .withLimit(0).build());

        SearchQuery searchQuery = SearchQuery.builder().withQuery("test query").withLimit(10).build();
        assertEquals(10,searchQuery.getLimit());
        assertEquals("/search?q=test query&limit=10",searchQuery.getRequestURL());
    }
}