package com.ssrn.frontend.website.old_platform_contract_test;

import com.ssrn.test.support.old_platform_contract_test.ssrn.website.SsrnOldPlatformContractTest;
import com.ssrn.test.support.ssrn.website.pagemodel.AuthorProfilePage;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static com.ssrn.test.support.golden_data.RealSsrnUsers.USER_3;
import static com.ssrn.test.support.golden_data.RealSsrnUsers.USER_54;
import static com.ssrn.test.support.golden_data.SsrnPapers.PAPER_45;
import static com.ssrn.test.support.http.HttpClient.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class WebsitePagesContract extends SsrnOldPlatformContractTest {

    @Test
    public void expectArticlePageToExistAtPredicatableUrlBasedOnAbstractId() {
        // Given
        String existingPaperId = PAPER_45.getId();
        String existingPaperTitle = PAPER_45.getTitle();

        // When
        String paperTitleOnArticlePage = browser()
                .visit(ssrnWebsite().articlePageForAbstract(existingPaperId))
                .title();

        // Then
        assertThat(paperTitleOnArticlePage, is(equalTo(existingPaperTitle)));
    }

    @Test
    public void expectAuthorProfilePageToExistAtPredicatableUrlBasedOnAuthorId() {
        // Given
        String existingAuthorId = USER_54.getId();

        // When
        AuthorProfilePage.Visit authorProfilePageVisit = browser()
                .visit(ssrnWebsite().authorProfilePageFor(existingAuthorId));

        // Then
        assertThat(authorProfilePageVisit.authorName(), is(equalTo(USER_54.getPublicDisplayName())));
    }

    @Test
    public void expectAuthorImageUrlToReturnTheImageAndAnHttpStatusOfOK() {
        // Given
        String existingAuthorIdForAuthorWithPictureUpload = USER_3.getId();

        // Given, When
        Response response =
                httpClient().get(
                        ssrnWebsite().authorImageBaseUrl(),
                        "/sol3/cf_dev/AuthorProfilePicture.cfm",
                        headers(),
                        queryParameters(queryParameter("per_id", existingAuthorIdForAuthorWithPictureUpload))
                );

        // Then
        assertThat(response.getStatusInfo(), is(equalTo(Response.Status.OK)));
        assertThat(response.getMediaType().toString(), containsString("image/png"));
    }

    @Test
    public void expectWhoAmIUrlToReturnStatusOfOK() {
        // Given, When
        Response response =
                httpClient().get(
                        ssrnWebsite().authBaseUrl(),
                        "/rest/user/whoami");

        // Then
        assertThat("/rest/user/whoami is not returning Status of OK. This is often fixed by restarting the " +
                        "service by making a call to https://preprodstatic.ssrn.com/cfc/restfulUser/restartREST.cfm",
                response.getStatusInfo(), is(equalTo(Response.Status.OK)));
    }
}
