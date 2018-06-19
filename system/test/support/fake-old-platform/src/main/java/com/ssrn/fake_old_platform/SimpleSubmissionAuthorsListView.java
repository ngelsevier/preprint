package com.ssrn.fake_old_platform;

import io.dropwizard.views.View;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class SimpleSubmissionAuthorsListView extends View {

    private int abstractId;
    private String ssrnAccountId;
    private final String firstAdditionalAuthorAccountId;
    private final String secondAdditionalAuthorAccountId;
    private String thirdAdditionalAuthorAccountId;
    private String[] authorIds;

    protected SimpleSubmissionAuthorsListView(int abstractId, String ssrnAccountId, String firstAdditionalAuthorAccountId, String secondAdditionalAuthorAccountId, String thirdAdditionalAuthorAccountId, String[] authorIds) {
        super("authors-iframe.mustache");
        this.abstractId = abstractId;
        this.ssrnAccountId = ssrnAccountId;
        this.firstAdditionalAuthorAccountId = firstAdditionalAuthorAccountId;
        this.secondAdditionalAuthorAccountId = secondAdditionalAuthorAccountId;
        this.thirdAdditionalAuthorAccountId = thirdAdditionalAuthorAccountId;
        this.authorIds = authorIds;
    }

    public int getAbstractId() {
        return abstractId;
    }

    public String getSsrnAccountId() { return ssrnAccountId; }

    public String getFirstAdditionalAuthorAccountId() { return firstAdditionalAuthorAccountId; }

    public String getSecondAdditionalAuthorAccountId() {
        return secondAdditionalAuthorAccountId;
    }

    public String getThirdAdditionalAuthorAccountId() { return thirdAdditionalAuthorAccountId; }

    public String[] getAuthorIds() {
        return authorIds;
    }

    public List<Author> getAuthors() {
        ArrayList<Author> authors = new ArrayList<>();
        for (int i = 0; i < authorIds.length; i++) {
            authors.add(new Author(i, authorIds[i], (i == authorIds.length -1) && authorIds.length > 1));
        }
        return authors;
    }


    public static class Author {
        private int index;
        private String authorId;
        private final boolean isLast;

        public Author(int index, String authorId, boolean isLast) {
            this.index = index;
            this.authorId = authorId;
            this.isLast = isLast;
        }

        public String getAuthorId() {
            return authorId;
        }

        public int getIndex() {
            return index;
        }

        public boolean isFirst() {
            return index == 0;
        }

        public boolean isLast() {
            return isLast;
        }
    }
}

