package com.ssrn.frontend.website.search;

import java.util.Arrays;
import java.util.function.Function;

public class SearchResultViewModel {
    private final BaseSearchResult searchResult;
    private final Function<BaseSearchResult, String> articlePageUrlMapper;
    private final Function<SearchResultAuthor, String> authorPageUrlMapper;
    private final Function<SearchResultAuthor, String> authorImageUrlMapper;

    SearchResultViewModel(BaseSearchResult searchResult, Function<BaseSearchResult, String> articlePageUrlMapper, Function<SearchResultAuthor, String> authorPageUrlMapper, Function<SearchResultAuthor, String> authorImageUrlMapper) {
        this.searchResult = searchResult;
        this.articlePageUrlMapper = articlePageUrlMapper;
        this.authorPageUrlMapper = authorPageUrlMapper;
        this.authorImageUrlMapper = authorImageUrlMapper;
    }

    public String getTitle() {
        return isSearchResultOfTypePaper() ? ((PaperSearchResult) searchResult).getTitle() : "";
    }

    public String getKeywords() {
        return isSearchResultOfTypePaper() ? ((PaperSearchResult) searchResult).getKeywords() : "";
    }

    public boolean isSearchResultOfTypePaper() {
        return (searchResult instanceof  PaperSearchResult);
    }

    public Object getArticlePageUrl() {
        return articlePageUrlMapper.apply(searchResult);
    }

    public SearchResultViewModelAuthor[] getAuthors() {
        return isSearchResultOfTypePaper() ?
                Arrays.stream(((PaperSearchResult) searchResult).getAuthors())
                        .map(author -> new SearchResultViewModelAuthor(author, authorPageUrlMapper))
                        .toArray(SearchResultViewModelAuthor[]::new) :
                new SearchResultViewModelAuthor[0];
    }

    public String getType() {
        return isSearchResultOfTypePaper() ? "paper" : "author";
    }

    public String getAuthorName() {
        return isSearchResultOfTypePaper() ? "" : ((AuthorSearchResult) searchResult).getName();
    }

    public String getAuthorImageUrl() {
        AuthorSearchResult authorSearchResult = ((AuthorSearchResult) searchResult);
        return authorImageUrlMapper.apply(new SearchResultAuthor(authorSearchResult.getId(), authorSearchResult.getName()));
    }

    public String getProfilePageUrl() {
        AuthorSearchResult authorSearchResult = ((AuthorSearchResult) searchResult);
        return authorPageUrlMapper.apply(new SearchResultAuthor(authorSearchResult.getId(), authorSearchResult.getName()));
    }

    public static class SearchResultViewModelAuthor {
        private final SearchResultAuthor author;
        private final Function<SearchResultAuthor, String> authorPageUrlMapper;

        SearchResultViewModelAuthor(SearchResultAuthor author, Function<SearchResultAuthor, String> authorPageUrlMapper) {
            this.author = author;
            this.authorPageUrlMapper = authorPageUrlMapper;
        }

        public String getAuthorName() {
            return author.getName() == null || "".equals(author.getName()) ? null : author.getName();
        }

        public String getProfilePageUrl() {
            return authorPageUrlMapper.apply(author);
        }

    }
}
