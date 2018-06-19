package com.ssrn.papers.replicator.page_item_stream;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ssrn.papers.replicator.page_item_stream.LinkedPageBuilder.linkedPageNamed;
import static com.ssrn.papers.replicator.page_item_stream.LinkedPageItemBuilder.linkedPageItemNamed;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class LinkedPageItemStreamFactoryTest {
    private static final LinkedPage FIRST_PAGE = linkedPageNamed("First Page")
            .withNextPageName("Second Page")
            .withItems(linkedPageItemNamed("First Page Item 1"), linkedPageItemNamed("First Page Item 2"))
            .build();

    private static final List<LinkedPage> RANDOMLY_ORDERED_PAGES = asList(
            linkedPageNamed("Third Page")
                    .withNextPageName(null)
                    .withItems(linkedPageItemNamed("Third Page Item 1"), linkedPageItemNamed("Third Page Item 2"))
                    .build(),
            FIRST_PAGE,
            linkedPageNamed("Second Page")
                    .withNextPageName("Third Page")
                    .withItems(linkedPageItemNamed("Second Page Item 1"), linkedPageItemNamed("Second Page Item 2"))
                    .build()
    );

    @Test
    public void shouldProvideStreamOfGeneratedItemsFromPagesOrderedByRetrievalFunctionUpToAndIncludingPageSatisfyingFinalPageCondition() {
        // Given
        Function<LinkedPage, LinkedPage> nextPageRetriever = page -> {
            String nextPageName = page.getNextPageName();
            return RANDOMLY_ORDERED_PAGES.stream().filter(otherPage -> nextPageName.equals(otherPage.getName())).findFirst().get();
        };

        Predicate<LinkedPage> finalPageCondition = page -> page.getNextPageName() == null;

        Function<LinkedPage, Stream<? extends String>> pageItemStreamGenerator = page -> Arrays.stream(page.getItems()).map(item -> item.getName().toUpperCase());

        LinkedPageItemStreamFactory<LinkedPage, String> linkedPageItemStreamFactory = new LinkedPageItemStreamFactory<>(
                pageItemStreamGenerator, finalPageCondition, nextPageRetriever);

        // When
        Stream<String> pageItemStream = linkedPageItemStreamFactory.createPageItemStreamStartingFrom(FIRST_PAGE);

        // Then
        assertThat(pageItemStream.collect(Collectors.toList()), contains("FIRST PAGE ITEM 1", "FIRST PAGE ITEM 2", "SECOND PAGE ITEM 1", "SECOND PAGE ITEM 2", "THIRD PAGE ITEM 1", "THIRD PAGE ITEM 2"));
    }
}
