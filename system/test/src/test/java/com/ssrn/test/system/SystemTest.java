package com.ssrn.test.system;

import com.ssrn.fake_old_platform.FakeOldPlatform;
import com.ssrn.fake_old_platform.SsrnFakeOldPlatformTest;
import com.ssrn.test.support.ssrn.website.pagemodel.*;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static com.ssrn.test.support.matchers.EventualMatcher.eventuallySatisfies;
import static com.ssrn.test.support.ssrn.website.pagemodel.matcher.SearchResultAuthorMatcher.anAuthor;
import static com.ssrn.test.support.ssrn.website.pagemodel.matcher.SearchResultAuthorWithNameMatcher.withName;
import static com.ssrn.test.support.ssrn.website.pagemodel.matcher.SearchResultMatcher.searchResultWith;
import static com.ssrn.test.support.ssrn.website.pagemodel.matcher.SearchResultWithAuthorMatcher.anAuthorSearchResultWith;
import static com.ssrn.test.support.ssrn.website.pagemodel.matcher.SearchResultWithAuthorNamesMatcher.authorNamesInOrder;
import static com.ssrn.test.support.ssrn.website.pagemodel.matcher.SearchResultWithTitleMatcher.title;
import static com.ssrn.test.support.utils.Interval.checkingEvery;
import static com.ssrn.test.support.utils.WaitForConditionFluentSyntax.waitUntil;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.core.Is.is;

public class SystemTest extends SsrnFakeOldPlatformTest {

    private FakeOldPlatform fakeOldPlatform = new FakeOldPlatform();
    private NewSsrnWebsite newSsrnWebsite;

    @Before
    public void before() {
        newSsrnWebsite = new NewSsrnWebsite(ssrnWebsite(), 3);

        waitUntil(() -> newSsrnWebsite.isAvailable())
                .checkingEvery(100, MILLISECONDS)
                .forNoMoreThan(180, SECONDS);
    }

    @Test
    public void searchForAuthorsAndSubmittedPapers() {
        // Given
        String uniqueString = UUID.randomUUID().toString();
        fakeOldPlatform.hasPaperThatWasCreatedBeforeEventFeedExisted(
                String.format("Historic Paper %s Title расширенный 符号", uniqueString),
                null,
                new String[]{ssrnWebsite().firstAdditionalAuthorAccountId()},
                false,
                false,
                false,
                "SUBMITTED"
        );

        browser()
                .visit(ssrnWebsite().loginPage())
                .logInAs(ssrnWebsite().accountUsername(), ssrnWebsite().accountPassword());

        String authorFullName = String.format("First %s", uniqueString);
        browser()
                .click(ssrnWebsite().navigationBar().profileDropDown(ssrnWebsite().accountId()).personalInfoLink())
                .enterPublicDisplayNameTo("First", uniqueString)
                .submitUpdates();

        browser()
                .click(ssrnWebsite().navigationBar().submitAPaperLink())
                .acceptTermsAndConditions()
                .changeTitleTo("New Paper Title")
                .addAuthorToPaper(ssrnWebsite().firstAdditionalAuthorEmail(), ssrnWebsite().firstAdditionalAuthorAccountId())
                .setAbstractTo(String.format("Some abstract content %s", UUID.randomUUID().toString()))
                .setClassificationToClassifyBySsrn()
                .submitPaper();

        SearchPage.Visit searchPageVisit = browser().visit(newSsrnWebsite().searchPage());

        assertThat(
                // When
                () -> searchPageVisit.searchForTitle(uniqueString).searchResults(),
                // Then
                eventuallySatisfies(hasItems(
                        searchResultWith(allOf(
                                title("New Paper Title"),
                                authorNamesInOrder(authorFullName, ssrnWebsite().firstAdditionalAuthorName()))),
                        searchResultWith(allOf(
                                title(String.format("Historic Paper %s Title расширенный 符号", uniqueString)),
                                authorNamesInOrder(ssrnWebsite().firstAdditionalAuthorName()))),
                        anAuthorSearchResultWith(anAuthor(withName(authorFullName)))

                )).within(180, SECONDS, checkingEvery(5, SECONDS))
        );

        // When
        SearchResult firstSearchResult = searchPageVisit.searchResults().get(1);
        ArticlePage.Visit anArticlePageVisit = firstSearchResult.click();

        // Then
        assertThat(anArticlePageVisit.title(), is(equalTo(firstSearchResult.title())));

        // When
        browser().clickBackButton();
        AuthorProfilePage.Visit authorProfilePageVisit = firstSearchResult.clickPaperAuthor(0);

        // Then
        assertThat(authorProfilePageVisit.authorName(), is(equalTo(firstSearchResult.getAuthors()[0].getName())));

    }

    private NewSsrnWebsite newSsrnWebsite() {
        return newSsrnWebsite;
    }
}
