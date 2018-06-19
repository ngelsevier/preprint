package com.ssrn.papers.replicator.page_item_stream;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.ssrn.papers.replicator.page_item_stream.LinkedPageBuilder.linkedPageNamed;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

public class LinkedPagesIteratorTest {
    private static final LinkedPage FIRST_PAGE = linkedPageNamed("First Page").withNextPageName("Second Page").build();

    private static final List<LinkedPage> RANDOMLY_ORDERED_PAGES = asList(
            linkedPageNamed("Third Page").withNextPageName(null).build(),
            FIRST_PAGE,
            linkedPageNamed("Second Page").withNextPageName("Third Page").build()
    );

    private LinkedPagesIterator<LinkedPage> linkedPagesIterator;

    @Before
    public void initializeLinkedPagesIterator() {
        linkedPagesIterator = new LinkedPagesIterator<>(
                FIRST_PAGE,
                page -> page.getNextPageName() == null,
                page -> {
                    String nextPageName = page.getNextPageName();
                    return RANDOMLY_ORDERED_PAGES.stream().filter(otherPage -> nextPageName.equals(otherPage.getName())).findFirst().get();
                }
        );
    }

    @Test
    public void shouldReturnInitialPageOnFirstIteration() {
        assertThat(linkedPagesIterator.hasNext(), is(equalTo(true)));
        assertThat(linkedPagesIterator.next().getName(), is(equalTo("First Page")));
    }

    @Test
    public void shouldReturnSubsequentPageOnSubsequentIteration() {
        assertThat(linkedPagesIterator.hasNext(), is(equalTo(true)));
        assertThat(linkedPagesIterator.next().getName(), is(equalTo("First Page")));

        assertThat(linkedPagesIterator.hasNext(), is(equalTo(true)));
        assertThat(linkedPagesIterator.next().getName(), is(equalTo("Second Page")));

        assertThat(linkedPagesIterator.hasNext(), is(equalTo(true)));
        assertThat(linkedPagesIterator.next().getName(), is(equalTo("Third Page")));
    }

    @Test
    public void shouldIndicateWhenThereAreNoMorePagesToIterateOver() {
        assertThat(linkedPagesIterator.hasNext(), is(equalTo(true)));
        assertThat(linkedPagesIterator.next().getName(), is(equalTo("First Page")));

        assertThat(linkedPagesIterator.hasNext(), is(equalTo(true)));
        assertThat(linkedPagesIterator.next().getName(), is(equalTo("Second Page")));

        assertThat(linkedPagesIterator.hasNext(), is(equalTo(true)));
        assertThat(linkedPagesIterator.next().getName(), is(equalTo("Third Page")));

        assertThat(linkedPagesIterator.hasNext(), is(equalTo(false)));
    }
}
