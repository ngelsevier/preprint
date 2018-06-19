package com.ssrn.authors.replicator.http_old_platform_author_entities_feed;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.ssrn.fake_old_platform.FakeOldPlatform;
import com.ssrn.fake_old_platform.Service;
import org.junit.Test;

import javax.ws.rs.client.ClientBuilder;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class AuthorsStreamSourceTest {

    private final FakeOldPlatform fakeOldPlatform = new FakeOldPlatform();

    @Test
    public void shouldProvideStreamOfAuthorsRetrievedFromOldPlatformAuthorsHttpFeed() {
        // Given
        String uniqueString = UUID.randomUUID().toString();
        List<Integer> authorIds = IntStream.range(0, Service.ENTITIES_FEED_ITEMS_PER_PAGE + 1)
                .mapToObj(i -> fakeOldPlatform.hasAuthorThatWasCreatedBeforeEventFeedExisted(String.format("Author %d Name %s", i + 1, uniqueString)))
                .collect(Collectors.toList());

        AuthorsStreamSource authorSource = new AuthorsStreamSource(
                Service.BASE_URL,
                Service.BASIC_AUTH_USERNAME,
                Service.BASIC_AUTH_PASSWORD,
                ClientBuilder.newClient().register(JacksonJsonProvider.class), 3, Level.INFO
        );

        int numberOfAuthorsInEntityFeedAfterAuthorId = fakeOldPlatform.getNumberOfAuthorsInEntityFeedAfterAuthorId("0");

        // When
        Stream<com.ssrn.authors.domain.Author> authorsStream = authorSource.getAuthorsStream();

        // Then
        List<com.ssrn.authors.domain.Author> retrievedAuthors = authorsStream
                .limit(numberOfAuthorsInEntityFeedAfterAuthorId)
                .collect(Collectors.toList());

        String[] authorIdsInAscendingOrder = ascendingStreamSortOf(authorIds).map(i -> Integer.toString(i)).toArray(String[]::new);
        assertThat(authorIdsIn(retrievedAuthors), containsInRelativeOrder(authorIdsInAscendingOrder));

        com.ssrn.authors.domain.Author firstAuthor = retrievedAuthors.stream().filter(author -> author.getId().equals(authorIdsInAscendingOrder[0])).findFirst().get();
        assertThat(firstAuthor.getName(), is(equalTo(String.format("Author 1 Name %s", uniqueString))));
        assertThat(firstAuthor.getVersion(), is(greaterThan(0)));
    }

    @Test
    public void shouldProvideStreamOfAuthorsAfterSpecifiedAuthorIdRetrievedFromOldPlatformAuthorsHttpFeed() {
        // Given
        String uniqueString = UUID.randomUUID().toString();
        List<Integer> authorIds = IntStream.range(0, Service.ENTITIES_FEED_ITEMS_PER_PAGE + 1)
                .mapToObj(i -> fakeOldPlatform.hasAuthorThatWasCreatedBeforeEventFeedExisted(String.format("Author %d Name %s", i + 1, uniqueString)))
                .collect(Collectors.toList());

        AuthorsStreamSource authorSource = new AuthorsStreamSource(
                Service.BASE_URL,
                Service.BASIC_AUTH_USERNAME,
                Service.BASIC_AUTH_PASSWORD,
                ClientBuilder.newClient().register(JacksonJsonProvider.class), 3, Level.INFO
        );

        int numberOfAuthorsInEntityFeedAfterAuthorId = fakeOldPlatform.getNumberOfAuthorsInEntityFeedAfterAuthorId("0");

        // When
        Stream<com.ssrn.authors.domain.Author> authorsStream = authorSource.getAuthorsStreamAfterId(Integer.toString(authorIds.get(0)));

        // Then
        List<com.ssrn.authors.domain.Author> retrievedAuthors = authorsStream
                .limit(numberOfAuthorsInEntityFeedAfterAuthorId - 1)
                .collect(Collectors.toList());

        String[] authorIdsInAscendingOrder = ascendingStreamSortOf(authorIds.subList(1, authorIds.size())).map(i -> Integer.toString(i)).toArray(String[]::new);
        List<String> retrievedAuthorIds = authorIdsIn(retrievedAuthors);
        assertThat(retrievedAuthorIds, containsInRelativeOrder(authorIdsInAscendingOrder));
        assertThat(retrievedAuthorIds, not(hasItem(Integer.toString(authorIds.get(0)))));

        com.ssrn.authors.domain.Author firstAuthor = retrievedAuthors.stream().filter(author -> author.getId().equals(authorIdsInAscendingOrder[0])).findFirst().get();
        assertThat(firstAuthor.getName(), is(equalTo(String.format("Author 2 Name %s", uniqueString))));
        assertThat(firstAuthor.getVersion(), is(greaterThan(0)));
    }

    private static List<String> authorIdsIn(List<com.ssrn.authors.domain.Author> retrievedAuthors) {
        return retrievedAuthors.stream().map(author -> String.valueOf(author.getId())).collect(Collectors.toList());
    }

    private static Stream<Integer> ascendingStreamSortOf(List<Integer> integers) {
        return integers.stream().sorted();
    }
}
