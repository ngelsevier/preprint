package com.ssrn.test.support.ssrn.website.pagemodel;

import com.ssrn.test.support.browser.Browser;
import com.ssrn.test.support.browser.BrowserElement;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.NoSuchElementException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SearchPage extends WebPageBase<SearchPage.Visit> {

    private final SsrnWebsite ssrnWebsite;
    private final int pageLoadTimeoutSeconds;

    SearchPage(String baseUrl, SsrnWebsite ssrnWebsite, int pageLoadTimeoutSeconds) {
        super(baseUrl, "/search");
        this.ssrnWebsite = ssrnWebsite;
        this.pageLoadTimeoutSeconds = pageLoadTimeoutSeconds;
    }

    @Override
    protected Visit createVisit(Browser browser) {
        return new Visit(browser, ssrnWebsite, pageLoadTimeoutSeconds);
    }

    public static class Visit {
        private final Browser browser;
        private final SsrnWebsite ssrnWebsite;
        private final int pageLoadTimeoutSeconds;

        private Visit(Browser browser, SsrnWebsite ssrnWebsite, int pageLoadTimeoutSeconds) {
            this.browser = browser;
            this.ssrnWebsite = ssrnWebsite;
            this.pageLoadTimeoutSeconds = pageLoadTimeoutSeconds;
        }

        public SearchPage.Visit searchForTitle(String title) {
            browser.enterTextInField("input[type=text]", title);
            browser.clickElement("button[type=submit]");
            return this;
        }

        public SearchPage.Visit clickNextLink() {
            browser.clickElement("a[data-direction='next']");
            return this;
        }

        public SearchPage.Visit clickPrevLink() {
            browser.clickElement("a[data-direction='prev']");
            return this;
        }

        public static String extractAuthorIdFromAuthorPageUrl(String articlePageUrl) {
            return articlePageUrl.split("author=")[1];
        }

        public List<SearchResult> searchResults() {
            List<BrowserElement> elements = browser.getElementsAt(".results .panel > ol li");

            return IntStream.range(0, elements.size())
                    .mapToObj(searchResultIndex -> {
                        BrowserElement browserElement = elements.get(searchResultIndex);
                        ArticlePage articlePage = null;
                        AuthorProfilePage[] authorProfilePages;
                        String[] authorIds = null;
                        SearchResult.Author[] paperAuthors = null;
                        SearchResult.Author author = null;

                        String keywords = "";

                        if (browserElement.containsCssClassValues("paper")) {
                            String abstractId = ArticlePage.extractAbstractIdFromArticlePageUrl(browserElement.findChild("a").getAttribute("href"));
                            articlePage = ssrnWebsite.articlePageForAbstract(abstractId);

                            paperAuthors = Arrays.stream(browserElement.findChildren(".author-list a")).
                                    map(element ->
                                            new SearchResult.Author(
                                                    extractAuthorIdFromAuthorPageUrl(element.getAttribute("href")),
                                                    StringUtils.isEmpty(element.text()) ? null : element.text())
                                    ).toArray(SearchResult.Author[]::new);

                            authorIds = Arrays.stream(paperAuthors).map(SearchResult.Author::getId).toArray(String[]::new);
                            authorProfilePages = Arrays.stream(authorIds).map(ssrnWebsite::authorProfilePageFor).toArray(AuthorProfilePage[]::new);

                            try {
                                keywords = browserElement.findChild(".keywords").text();
                            } catch (NoSuchElementException ignored) { }

                        } else {
                            String authorId = extractAuthorIdFromAuthorPageUrl(browserElement.findChild("a").getAttribute("href"));
                            String authorName = browserElement.findChild("span").text();
                            author = new SearchResult.Author(authorId, authorName);
                            authorProfilePages = new AuthorProfilePage[]{ssrnWebsite.authorProfilePageFor(authorId)};
                        }

                        return new SearchResult(browser,
                                browserElement.findChild("a").text(),
                                keywords, String.format("li:nth-of-type(%d)", searchResultIndex + 1),
                                articlePage,
                                authorProfilePages,
                                authorIds, paperAuthors, author, pageLoadTimeoutSeconds);
                    })
                    .collect(Collectors.toList());
        }

        public String numberOfSearchResults() {
            return browser.getTextContentOfElementAt("section.results .search-result-count span");
        }

        public String searchBoxText() {
            return browser.getElementAttributeAt("section.search input[name=query]", "value");
        }

        public String loggedInStatus() {
            return browser.getElementAttributeAt("header", "data-authenticated");
        }

        public BrowserElement nextLink() {
            List<BrowserElement> nextLinks = browser.getElementsAt("a[data-direction='next']");
            return nextLinks.size() > 0 ? nextLinks.get(0) : null;
        }

        public BrowserElement prevLink() {
            List<BrowserElement> prevLinks = browser.getElementsAt("a[data-direction='prev']");
            return prevLinks.size() > 0 ? prevLinks.get(0) : null;
        }
    }
}
