package org.voyager.service.impl;

import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.voyager.error.ServiceError;
import org.voyager.model.SearchQuery;
import org.voyager.model.response.SearchResult;
import org.voyager.model.result.LookupAttribution;
import org.voyager.model.result.ResultSearch;
import org.voyager.model.result.ResultSearchFull;
import org.voyager.service.SearchService;
import org.voyager.service.TestServiceRegistry;
import org.voyager.service.utils.ServiceUtilsTestFactory;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SearchServiceImplTest {
    SearchService searchService;

    @BeforeEach
    void setUp() {
        TestServiceRegistry testServiceRegistry = TestServiceRegistry.getInstance();
        testServiceRegistry.registerTestImplementation(
                SearchService.class,SearchServiceImpl.class,ServiceUtilsTestFactory.getInstance());
        searchService = testServiceRegistry.get(SearchService.class);
        assertNotNull(searchService);
    }

    @Test
    void search() {
        assertThrows(NullPointerException.class,()->searchService.search(null));
        Either<ServiceError, SearchResult<ResultSearch>> either = searchService.search(SearchQuery.builder().withQuery("test").build());
        assertNotNull(either);
        assertTrue(either.isRight());
    }

    @Test
    void attribution() {
        Either<ServiceError, LookupAttribution> either = searchService.attribution();
        assertNotNull(either);
        assertTrue(either.isRight());
        assertNotNull(either.get());
    }

    @Test
    void fetchResultSearchFull() {
        Either<ServiceError, ResultSearchFull> either = searchService.fetchResultSearchFull("test-source-id");
        assertNotNull(either);
        assertTrue(either.isRight());
        assertNotNull(either.get());
    }
}