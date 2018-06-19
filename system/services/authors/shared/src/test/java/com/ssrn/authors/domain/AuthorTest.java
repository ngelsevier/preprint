package com.ssrn.authors.domain;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;

import static com.ssrn.authors.shared.test_support.entity.AuthorBuilder.anAuthor;
import static com.ssrn.authors.shared.test_support.event.EventBuilder.anEvent;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class AuthorTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test(expected = JsonMappingException.class)
    public void shouldRequireIdWhenDeserializingFromJson() throws IOException {
        objectMapper.readValue("{ \"version\": 1, \"removed\" : false }", Author.class);
    }

    @Test(expected = JsonMappingException.class)
    public void shouldRequireVersionWhenDeserializingFromJson() throws IOException {
        objectMapper.readValue("{ \"id\": 1, \"removed\" : false }", Author.class);
    }

    @Test(expected = JsonMappingException.class)
    public void shouldRequireRemovalFlagWhenDeserializingFromJson() throws IOException {
        objectMapper.readValue("{ \"id\": 1, \"version\": 1 }", Author.class);
    }

    @Test
    public void shouldUpdateNameWhenRegisteredEventApplied() {
        // Given
        Author author = anAuthor().withName("initial name").withVersion(4).withRemoval(false).build();

        Event registeredEvent = anEvent()
                .withType("REGISTERED")
                .withEntityVersion(5)
                .withData(new JSONObject().put("name", "new name"))
                .build();

        // When
        author.apply(registeredEvent);

        // Then
        assertThat(author.getName(), is(equalTo("new name")));
        assertThat(author.isRemoved(), is(equalTo(false)));
    }

    @Test
    public void shouldUpdateNameWhenNameChangedEventApplied() {
        // Given
        Author author = anAuthor().withName("initial name").withVersion(1).withRemoval(false).build();

        Event nameChangedEvent = anEvent()
                .withType("NAME CHANGED")
                .withEntityVersion(2)
                .withData(new JSONObject().put("name", "new name"))
                .build();

        // When
        author.apply(nameChangedEvent);

        // Then
        assertThat(author.getName(), is(equalTo("new name")));
        assertThat(author.isRemoved(), is(equalTo(false)));
    }

    @Test
    public void shouldUpdateVersionToEventEntityVersionWhenEventApplied() {
        // Given
        Author author = anAuthor().withName("initial name").withVersion(1).withRemoval(false).build();

        Event nameChangedEvent = anEvent()
                .withType("NAME CHANGED")
                .withEntityVersion(2)
                .withData(new JSONObject().put("name", "new name"))
                .build();

        // When
        author.apply(nameChangedEvent);

        // Then
        assertThat(author.getName(), is(equalTo("new name")));
        assertThat(author.getVersion(), is(equalTo(2)));
        assertThat(author.isRemoved(), is(equalTo(false)));
    }

    @Test
    public void shouldUpdateRemovalFlagWhenUnregisteredEventApplied() {
        // Given
        Author author = anAuthor().withName("author name").withVersion(1).withRemoval(false).build();

        Event unregisteredEvent = anEvent()
                .withType("UNREGISTERED")
                .withEntityVersion(2)
                .build();

        // When
        author.apply(unregisteredEvent);

        // Then
        assertThat(author.getVersion(), is(equalTo(2)));
        assertThat(author.isRemoved(), is(equalTo(true)));
    }

    @Test
    public void shouldUpdateVersionToEventEntityVersionEvenWhenUnsupportedEventApplied() {
        Author author = anAuthor().withName("initial name").withVersion(1).withRemoval(false).build();

        Event unsupportedEvent = anEvent()
                .withType("UNSUPPORTED TYPE")
                .withEntityVersion(2)
                .withNoData()
                .build();

        // When
        author.apply(unsupportedEvent);

        // Then
        assertThat(author.getVersion(), is(equalTo(2)));
        assertThat(author.isRemoved(), is(equalTo(false)));
    }

    @Test
    public void shouldNotThrowExceptionWhenUnrecognisedEventApplied() {
        // Given
        Author author = anAuthor().withVersion(1).build();

        Event eventWithUnrecognisedType = anEvent()
                .withType("UNRECOGNISED TYPE")
                .withEntityVersion(2)
                .withNoData()
                .build();

        // When
        author.apply(eventWithUnrecognisedType);

        // Then should not throw an exception
    }

    @Test
    public void shouldNotApplyAnEventForAnOlderVersion() {
        // Given
        Author author = anAuthor().withName("new name").withVersion(3).withRemoval(false).build();

        Event nameChangedEvent = anEvent()
                .withType("NAME CHANGED")
                .withEntityVersion(2)
                .withData(new JSONObject().put("name", "previous name"))
                .build();

        // When
        try {
            author.apply(nameChangedEvent);
        } catch (UnexpectedEntityVersionEventAppliedException e) {
            assertThat(e.getEvent(), is(equalTo(nameChangedEvent)));
            assertThat(e.getCurrentVersion(), is(equalTo(3)));
        }

        // Then
        assertThat(author.getName(), is(equalTo("new name")));
        assertThat(author.getVersion(), is(equalTo(3)));
        assertThat(author.isRemoved(), is(equalTo(false)));
    }

    @Test
    public void shouldNotApplyAnEventForTheCurrentVersion() {
        // Given
        Author author = anAuthor().withName("initial name").withVersion(3).withRemoval(false).build();

        Event nameChangedEvent = anEvent()
                .withType("NAME CHANGED")
                .withEntityVersion(3)
                .withData(new JSONObject().put("name", "new name"))
                .build();

        // When
        try {
            author.apply(nameChangedEvent);
        } catch (UnexpectedEntityVersionEventAppliedException e) {
            assertThat(e.getEvent(), is(equalTo(nameChangedEvent)));
            assertThat(e.getCurrentVersion(), is(equalTo(3)));
        }

        // Then
        assertThat(author.getName(), is(equalTo("initial name")));
        assertThat(author.getVersion(), is(equalTo(3)));
        assertThat(author.isRemoved(), is(equalTo(false)));
    }

    @Test
    public void shouldNotApplyAnEventThatWouldIncreaseTheVersionByMoreThanOne() {
        // Given
        Author author = anAuthor().withName("initial name").withVersion(3).withRemoval(false).build();

        Event nameChangedEvent = anEvent()
                .withType("NAME CHANGED")
                .withEntityVersion(5)
                .withData(new JSONObject().put("name", "new name"))
                .build();

        // When
        try {
            author.apply(nameChangedEvent);
        } catch (UnexpectedEntityVersionEventAppliedException e) {
            assertThat(e.getEvent(), is(equalTo(nameChangedEvent)));
            assertThat(e.getCurrentVersion(), is(equalTo(3)));
        }

        // Then
        assertThat(author.getName(), is(equalTo("initial name")));
        assertThat(author.getVersion(), is(equalTo(3)));
        assertThat(author.isRemoved(), is(equalTo(false)));
    }
}