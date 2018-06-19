package com.ssrn.test.support.ssrn.website.pagemodel;

import com.ssrn.test.support.browser.Browser;

import java.util.Arrays;

public class SearchResult {
    private final Browser browser;
    private final String title;
    private final String cssSelector;
    private final ArticlePage articlePage;
    private final AuthorProfilePage[] authorProfilePages;
    private final int pageLoadTimeoutSeconds;
    private final String keywords;
    private String[] authorIds;
    private Author[] authors;
    private Author author;

    SearchResult(Browser browser, String title, String keywords, String cssSelector, ArticlePage articlePage, AuthorProfilePage[] authorProfilePages, String[] authorIds, Author[] authors, Author author, int pageLoadTimeoutSeconds) {
        this.browser = browser;
        this.title = title;
        this.keywords = keywords;
        this.cssSelector = cssSelector;
        this.articlePage = articlePage;
        this.authorProfilePages = authorProfilePages;
        this.authorIds = authorIds;
        this.authors = authors;
        this.author = author;
        this.pageLoadTimeoutSeconds = pageLoadTimeoutSeconds;
    }

    @Override
    public String toString() {
        return "SearchResult{" +
                "title='" + title + '\'' +
                ", keywords='" + keywords + '\'' +
                ", authorIds=" + Arrays.toString(authorIds) +
                ", authors=" + Arrays.toString(authors) +
                '}';
    }

    public String title() {
        return title;
    }
    public ArticlePage.Visit click() {
        return new CssSelectorLocatedHyperlink<>(String.format("%s a.primary", cssSelector), articlePage, pageLoadTimeoutSeconds).clickWith(browser);
    }

    public AuthorProfilePage.Visit clickPaperAuthor(int authorIndex) {
        return new CssSelectorLocatedHyperlink<>(String.format(".author-list span:nth-of-type(%d) a", authorIndex + 1), authorProfilePages[authorIndex], pageLoadTimeoutSeconds).clickWith(browser);
    }

    public AuthorProfilePage.Visit clickAuthor() {
        return new CssSelectorLocatedHyperlink<>(("li.author a.primary"), authorProfilePages[0], pageLoadTimeoutSeconds).clickWith(browser);
    }

    public String[] getAuthorIds() {
        return authorIds;
    }

    public Author[] getAuthors() {
        return authors;
    }

    public Author getAuthor() {
        return author;
    }

    public String getKeywords() {
        return keywords;
    }

    public static class Author {
        private String id;
        private String name;

        public Author(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "Author{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    '}';
        }
    }
}
