package com.ssrn.search.shared;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssrn.search.domain.Paper;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static com.ssrn.search.domain.SubmissionStage.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class PaperTest {

    private JSONObject paperJson;

    @Before
    public void before() {
        paperJson = new JSONObject()
                .put("id", "id")
                .put("title", "any")
                .put("authorIds", new String[]{"id"})
                .put("paperPrivate", true)
                .put("paperRestricted", false)
                .put("paperIrrelevant", false)
                .put("submissionStage", IN_DRAFT.getName());
    }

    @Test
    public void shouldRequireIdPropertyInSourceJson() {
        shouldRequirePropertyInSourceJson("id");
    }

    @Test
    public void shouldRequireTitlePropertyInSourceJson() {
        shouldRequirePropertyInSourceJson("title");
    }

    @Test
    public void shouldNotRequireAuthorIdsPropertyInSourceJson() throws IOException {
        // Given
        paperJson.remove("authorIds");
        String paperJsonMissingProperty = paperJson.toString();

        ObjectMapper mapper = new ObjectMapper();

        // When
        mapper.readValue(paperJsonMissingProperty.toString(), Paper.class);

        // Then no exceptions
    }

    @Test
    public void shouldRequirePaperPrivatePropertyInSourceJson() {
        shouldRequirePropertyInSourceJson("paperPrivate");
    }

    @Test
    public void shouldRequirePaperIrrelevantPropertyInSourceJson() {
        shouldRequirePropertyInSourceJson("paperIrrelevant");
    }

    @Test
    public void shouldRequirePaperRestrictedPropertyInSourceJson() {
        shouldRequirePropertyInSourceJson("paperRestricted");
    }

    @Test
    public void shouldRequireSubmissionStagePropertyInSourceJson() {
        shouldRequirePropertyInSourceJson("submissionStage");
    }

    @Test
    public void shouldDeserializePrivateFlagFromSourceJson() throws IOException {
        // Given
        String privatePaperJson = new JSONObject()
                .put("id", "any")
                .put("title", "any")
                .put("paperPrivate", true)
                .put("paperIrrelevant", false)
                .put("paperRestricted", false)
                .put("submissionStage", IN_DRAFT.getName())
                .toString();

        String publicPaperJson = new JSONObject()
                .put("id", "any")
                .put("title", "any")
                .put("paperPrivate", false)
                .put("paperIrrelevant", false)
                .put("paperRestricted", false)
                .put("submissionStage", IN_DRAFT.getName())
                .toString();

        ObjectMapper mapper = new ObjectMapper();

        // When, Then
        assertThat(mapper.readValue(privatePaperJson, Paper.class).isPaperPrivate(), equalTo(true));
        assertThat(mapper.readValue(publicPaperJson, Paper.class).isPaperPrivate(), equalTo(false));
    }

    @Test
    public void shouldOptionallyDeserializeAuthorIdsFromSourceJson() throws IOException {
        // Given
        String paperJsonWithAuthors = new JSONObject()
                .put("id", "any")
                .put("title", "any")
                .put("paperPrivate", false)
                .put("paperIrrelevant", false)
                .put("paperRestricted", false)
                .put("authorIds", new int[]{1, 2, 3})
                .put("submissionStage", IN_DRAFT.getName())
                .toString();

        ObjectMapper mapper = new ObjectMapper();
        String paperJsonMissingAuthors = new JSONObject().put("id", "any").put("title", "any").put("paperPrivate", false).put("paperIrrelevant", false).put("paperRestricted", false).put("submissionStage", IN_DRAFT.getName()).toString();
        mapper.readValue(paperJsonMissingAuthors, Paper.class);

        // When
        Paper paper = mapper.readValue(paperJsonWithAuthors, Paper.class);

        // Then
        assertThat(paper.getAuthorIds(), arrayContaining("1", "2", "3"));

    }

    @Test
    public void shouldOptionallyDeserializeKeywordsFromSourceJson() throws IOException {
        // Given
        String paperJsonWithKeywords = new JSONObject()
                .put("id", "any")
                .put("title", "any")
                .put("paperPrivate", false)
                .put("paperIrrelevant", false)
                .put("paperRestricted", false)
                .put("authorIds", new int[]{1, 2, 3})
                .put("submissionStage", IN_DRAFT.getName())
                .put("keywords", "any")
                .toString();

        ObjectMapper mapper = new ObjectMapper();
        String paperJsonMissingKeywords = new JSONObject()
                .put("id", "any")
                .put("title", "any")
                .put("paperPrivate", false)
                .put("paperIrrelevant", false)
                .put("paperRestricted", false)
                .put("authorIds", new int[]{1, 2, 3})
                .put("submissionStage", IN_DRAFT.getName()).toString();

        mapper.readValue(paperJsonMissingKeywords, Paper.class);

        // When
        Paper paper = mapper.readValue(paperJsonWithKeywords, Paper.class);

        // Then
        assertThat(paper.getAuthorIds(), arrayContaining("1", "2", "3"));

    }

    @Test
    public void shouldDeserializeIrrelevantFlagFromSourceJson() throws IOException {
        // Given
        String irrelvantPaperJson = new JSONObject()
                .put("id", "any")
                .put("title", "any")
                .put("paperPrivate", false)
                .put("paperIrrelevant", true)
                .put("paperRestricted", false)
                .put("submissionStage", IN_DRAFT.getName())
                .toString();

        String relevantPaperJson = new JSONObject()
                .put("id", "any")
                .put("title", "any")
                .put("paperPrivate", false)
                .put("paperIrrelevant", false)
                .put("paperRestricted", false)
                .put("submissionStage", IN_DRAFT.getName())
                .toString();

        ObjectMapper mapper = new ObjectMapper();

        // When, Then
        assertThat(mapper.readValue(irrelvantPaperJson, Paper.class).isPaperIrrelevant(), equalTo(true));
        assertThat(mapper.readValue(relevantPaperJson, Paper.class).isPaperIrrelevant(), equalTo(false));
    }

    @Test
    public void shouldDeserializeSubmissionStageFromSourceJson() throws IOException {
        // Given
        String paperJson = new JSONObject()
                .put("id", "any")
                .put("title", "any")
                .put("paperPrivate", false)
                .put("paperIrrelevant", false)
                .put("paperRestricted", false)
                .put("submissionStage", IN_DRAFT.getName())
                .toString();

        ObjectMapper mapper = new ObjectMapper();

        // When, Then
        assertThat(mapper.readValue(paperJson, Paper.class).getSubmissionStage(), equalTo(IN_DRAFT));
    }

    @Test
    public void shouldReturnPaperNotSearchableIfPaperIsPrivate() throws IOException {
        // Given
        String paperJson = new JSONObject()
                .put("id", "any")
                .put("title", "any")
                .put("paperPrivate", true)
                .put("paperIrrelevant", false)
                .put("paperRestricted", false)
                .put("submissionStage", SUBMITTED.getName())
                .toString();

        ObjectMapper mapper = new ObjectMapper();
        Paper paper = mapper.readValue(paperJson, Paper.class);
        // When, Then

        assertThat(paper.isPaperSearchable(), equalTo(false));
    }

    @Test
    public void shouldReturnPaperNotSearchableIfPaperIsIrrelevant() throws IOException {
        // Given
        String irrelevantPaperJson = new JSONObject()
                .put("id", "any")
                .put("title", "any")
                .put("paperPrivate", false)
                .put("paperIrrelevant", true)
                .put("paperRestricted", false)
                .put("submissionStage", SUBMITTED.getName())
                .toString();

        ObjectMapper mapper = new ObjectMapper();
        Paper irrelevantPaper = mapper.readValue(irrelevantPaperJson, Paper.class);
        // When, Then

        assertThat(irrelevantPaper.isPaperSearchable(), equalTo(false));
    }

    @Test
    public void shouldReturnPaperNotSearchableIfPaperIsRestricted() throws IOException {
        // Given
        String restrictedPaperJson = new JSONObject()
                .put("id", "any")
                .put("title", "any")
                .put("paperPrivate", false)
                .put("paperIrrelevant", false)
                .put("paperRestricted", true)
                .put("submissionStage", SUBMITTED.getName())
                .toString();

        ObjectMapper mapper = new ObjectMapper();
        Paper restrictedPaper = mapper.readValue(restrictedPaperJson, Paper.class);
        // When, Then

        assertThat(restrictedPaper.isPaperSearchable(), equalTo(false));
    }

    @Test
    public void shouldReturnPaperNotSearchableIfPaperIsNotInSearchableSubmissionStage() throws IOException {
        // Given
        String inDraftPaperJson = new JSONObject()
                .put("id", "any")
                .put("title", "any")
                .put("paperPrivate", false)
                .put("paperIrrelevant", false)
                .put("paperRestricted", false)
                .put("submissionStage", IN_DRAFT.getName())
                .toString();

        String deletedPaperJson = new JSONObject()
                .put("id", "any")
                .put("title", "any")
                .put("paperPrivate", false)
                .put("paperIrrelevant", false)
                .put("paperRestricted", false)
                .put("submissionStage", DELETED.getName())
                .toString();

        String rejectedPaperJson = new JSONObject()
                .put("id", "any")
                .put("title", "any")
                .put("paperPrivate", false)
                .put("paperIrrelevant", false)
                .put("paperRestricted", false)
                .put("submissionStage", REJECTED.getName())
                .toString();

        String approvedPaperJson = new JSONObject()
                .put("id", "any")
                .put("title", "any")
                .put("paperPrivate", false)
                .put("paperIrrelevant", false)
                .put("paperRestricted", false)
                .put("submissionStage", APPROVED.getName())
                .toString();

        ObjectMapper mapper = new ObjectMapper();
        Paper draftedPaper = mapper.readValue(inDraftPaperJson, Paper.class);
        Paper deletedPaper = mapper.readValue(deletedPaperJson, Paper.class);
        Paper rejectedPaper = mapper.readValue(rejectedPaperJson, Paper.class);
        Paper approvedPaper = mapper.readValue(approvedPaperJson, Paper.class);
        // When, Then

        assertThat(draftedPaper.isPaperSearchable(), equalTo(false));
        assertThat(deletedPaper.isPaperSearchable(), equalTo(false));
        assertThat(rejectedPaper.isPaperSearchable(), equalTo(false));
        assertThat(approvedPaper.isPaperSearchable(), equalTo(true));
    }

    private void shouldRequirePropertyInSourceJson(String property) {
        // Given
        paperJson.remove(property);
        String paperJsonMissingProperty = paperJson.toString();

        ObjectMapper mapper = new ObjectMapper();

        // When
        try {
            mapper.readValue(paperJsonMissingProperty.toString(), Paper.class);
            fail();
        } catch (Exception e) {
            // Then throws exception
            assertThat(e, instanceOf(JsonMappingException.class));
            assertThat(e.getMessage(), containsString("Missing required creator property '" + property + "'"));
        }
    }
}