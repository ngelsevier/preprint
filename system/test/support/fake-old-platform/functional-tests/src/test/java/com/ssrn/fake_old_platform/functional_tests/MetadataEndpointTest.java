package com.ssrn.fake_old_platform.functional_tests;

import com.ssrn.fake_old_platform.FakeOldPlatform;
import com.ssrn.fake_old_platform.Paper;
import com.ssrn.fake_old_platform.SsrnFakeOldPlatformTest;
import org.junit.Test;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class MetadataEndpointTest extends SsrnFakeOldPlatformTest {

    @Test
    public void shouldAssignEachHistoricPaperAConsecutivelyIncreasingID() {
        // Given
        FakeOldPlatform fakeOldPlatform = new FakeOldPlatform();

        // When
        int firstAbstractId = fakeOldPlatform.hasPaperThatWasCreatedBeforeEventFeedExisted("First Paper " + UUID.randomUUID(), null, new String[]{"1"}, false, false, false, "SUBMITTED").getId();
        int secondAbstractId = fakeOldPlatform.hasPaperThatWasCreatedBeforeEventFeedExisted("Second Paper " + UUID.randomUUID(), null, new String[]{"1"}, false, false, false, "SUBMITTED").getId();
        int thirdAbstractId = fakeOldPlatform.hasPaperThatWasCreatedBeforeEventFeedExisted("Third Paper " + UUID.randomUUID(), null, new String[]{"1"}, false, false, false, "SUBMITTED").getId();

        // Then
        assertThat(secondAbstractId, is(equalTo(firstAbstractId + 1)));
        assertThat(thirdAbstractId, is(equalTo(secondAbstractId + 1)));
    }

    @Test
    public void shouldProvideCountOfPapersInEntityFeedAfterSpecifiedPaperId(){
        FakeOldPlatform fakeOldPlatform = new FakeOldPlatform();

        String uniqueString = UUID.randomUUID().toString();
        fakeOldPlatform.hasPaperThatWasCreatedBeforeEventFeedExisted(String.format("Paper %s 1", uniqueString), null, new String[0], false, false, false, "SUBMITTED");
        Paper secondPaper = fakeOldPlatform.hasPaperThatWasCreatedBeforeEventFeedExisted(String.format("Paper %s 2", uniqueString), null, new String[0], false, false, false, "SUBMITTED");
        fakeOldPlatform.hasPaperThatWasCreatedBeforeEventFeedExisted(String.format("Paper %s 3", uniqueString), null, new String[0], false, false, false, "SUBMITTED");
        fakeOldPlatform.hasPaperThatWasCreatedBeforeEventFeedExisted(String.format("Paper %s 4", uniqueString), null, new String[0], false, false, false, "SUBMITTED");
        Paper lastPaper = fakeOldPlatform.hasPaperThatWasCreatedBeforeEventFeedExisted(String.format("Paper %s 5", uniqueString), null, new String[0], false, false, false, "SUBMITTED");

        assertThat(fakeOldPlatform.getNumberOfPapersInEntityFeedAfterPaperId(Integer.toString(secondPaper.getId())), is(equalTo(3)));
        assertThat(fakeOldPlatform.getNumberOfPapersInEntityFeedAfterPaperId(Integer.toString(lastPaper.getId())), is(equalTo(0)));
    }
}
