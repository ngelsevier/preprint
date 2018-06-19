package com.ssrn.test.support.old_platform_contract_test.configuration;

import com.ssrn.test.support.golden_data.FakeSsrnUsers;
import com.ssrn.test.support.standalone_test_runner.configuration.ConfigurationBase;
import com.ssrn.test.support.ssrn.SsrnConfiguration;

import static java.util.function.Function.identity;

public class CommandLineSsrnConfiguration extends ConfigurationBase implements SsrnConfiguration {

    CommandLineSsrnConfiguration(String commandLineArgumentPrefix) {
        super(commandLineArgumentPrefix);

        addParameter("base-url", identity(), "http://localhq.ssrn.com", "URL", "base url for SSRN website and API");
        addParameter("old-platform-article-page-base-url", identity(), "http://localhq.ssrn.com", "ARTICLE PAGE BASE URL", "base url for SSRN article page");
        addParameter("old-platform-author-profile-page-base-url", identity(), "http://localhq.ssrn.com", "AUTHOR PROFILE PAGE BASE URL", "base url for SSRN author profile page");
        addParameter("old-platform-author-image-base-url", identity(), "http://localhq.ssrn.com", "AUTHOR IMAGE BASE URL", "base url for SSRN author image");
        addParameter("old-platform-auth-base-url", identity(), "http://localhq.ssrn.com", "AUTH BASE URL", "base url for SSRN whoami rest endpoint");
        addParameter("account-username", identity(), FakeSsrnUsers.JOHN_DOE.getUsername(), "USERNAME", "username for account that will be used to log in to SSRN");
        addParameter("account-password", identity(), "", "PASSWORD", "password for account that will be used to log in to SSRN");
        addParameter("admin-account-username", identity(), "testadmin1@ssrn.com", "ADMIN USERNAME", "username for admin account that will be used to log in to SSRN");
        addParameter("admin-account-password", identity(), "", "ADMIN PASSWORD", "password for admin account that will be used to log in to SSRN");
        addParameter("account-id", identity(), FakeSsrnUsers.JOHN_DOE.getId(), "ACCOUNT ID", "ID of account used to log in to SSRN");
        addParameter("account-name", identity(), FakeSsrnUsers.JOHN_DOE.getPublicDisplayName(), "ACCOUNT NAME", "Name of account used to log in to SSRN");
        addParameter("first-additional-author-account-id", identity(), FakeSsrnUsers.JAMES_BROWN.getId(), "FIRST ADDITIONAL AUTHOR ACCOUNT ID", "ID of account used as additional author added to paper");
        addParameter("first-additional-author-name", identity(), FakeSsrnUsers.JAMES_BROWN.getPublicDisplayName(), "FIRST ADDITIONAL AUTHOR EMAIL", "Email of account used as additional author added to paper");
        addParameter("first-additional-author-email", identity(), FakeSsrnUsers.JAMES_BROWN.getEmailAddress(), "FIRST ADDITIONAL AUTHOR NAME", "Name of account used as additional author added to paper");
        addParameter("first-additional-author-password", identity(), "", "FIRST ADDITIONAL AUTHOR PASSWORD", "Password of account used as additional author added to paper");
        addParameter("second-additional-author-account-id", identity(), FakeSsrnUsers.JOE_BLACK.getId(), "SECOND ADDITIONAL AUTHOR ACCOUNT ID", "ID of account used as additional author added to paper");
        addParameter("second-additional-author-name", identity(), FakeSsrnUsers.JOE_BLACK.getPublicDisplayName(), "SECOND ADDITIONAL AUTHOR NAME", "Name of account used as additional author added to paper");
        addParameter("second-additional-author-email", identity(), FakeSsrnUsers.JOE_BLACK.getEmailAddress(), "SECOND ADDITIONAL AUTHOR EMAIL", "Email of account used as additional author added to paper");
        addParameter("third-additional-author-account-id", identity(), FakeSsrnUsers.TIM_DUNCAN.getId(), "THIRD ADDITIONAL AUTHOR ACCOUNT ID", "ID of account used as additional author added to paper");
        addParameter("third-additional-author-name", identity(), FakeSsrnUsers.TIM_DUNCAN.getPublicDisplayName(), "THIRD ADDITIONAL AUTHOR NAME", "Name of account used as additional author added to paper");
        addParameter("third-additional-author-email", identity(), FakeSsrnUsers.TIM_DUNCAN.getEmailAddress(), "THIRD ADDITIONAL AUTHOR EMAIL", "Email of account used as additional author added to paper");
        addParameter("third-additional-author-username", identity(), FakeSsrnUsers.TIM_DUNCAN.getUsername(), "THIRD ADDITIONAL AUTHOR USERNAME", "Username for third author account that will be used to log in to SSRN");
        addParameter("third-additional-author-password", identity(), "123456", "THIRD ADDITIONAL AUTHOR PASSWORD", "Password for third author account that will be used to log in to SSRN");
        addParameter("author-for-name-change-account-id", identity(), FakeSsrnUsers.CHANGE_ME.getId(), "ACCOUNT ID OF AUTHOR FOR NAME CHANGE", "Account ID of author whose name will be changed for tests");
        addParameter("author-for-name-change-account-email", identity(), FakeSsrnUsers.CHANGE_ME.getEmailAddress(), "ACCOUNT EMAIL OF AUTHOR FOR NAME CHANGE", "Account Email of author whose name will be changed for tests");
        addParameter("first-papers-event-feed-page-id", identity(), "00000-00000-00000-00000-00000", "EVENT PAGE ID", "id of earliest page of events in the SSRN papers event feed");
        addParameter("first-authors-event-feed-page-id", identity(), "00000-00000-00000-00000-00000", "EVENT PAGE ID", "id of earliest page of events in the SSRN authors event feed");
        addParameter("authentication-username", identity(), "username", "AUTHENTICATION USERNAME", "username for paper events feed");
        addParameter("authentication-password", identity(), "password", "AUTHENTICATION PASSWORD", "password for paper events feed");
    }

    @Override
    public String baseUrl() {
        return getValueOf("base-url");
    }

    @Override
    public String accountId() {
        return getValueOf("account-id");
    }

    @Override
    public String firstAdditionalAuthorAccountId() {
        return getValueOf("first-additional-author-account-id");
    }

    @Override
    public String firstAdditionalAuthorEmail() {
        return getValueOf("first-additional-author-email");
    }

    @Override
    public String firstAdditionalAuthorPassword() {
        return getValueOf("first-additional-author-password");
    }

    @Override
    public String secondAdditionalAuthorAccountId() {
        return getValueOf("second-additional-author-account-id");
    }

    @Override
    public String secondAdditionalAuthorEmail() {
        return getValueOf("second-additional-author-email");
    }

    @Override
    public String accountName() {
        return getValueOf("account-name");
    }

    @Override
    public String firstAdditionalAuthorName() {
        return getValueOf("first-additional-author-name");
    }

    @Override
    public String secondAdditionalAuthorName() {
        return getValueOf("second-additional-author-name");
    }

    @Override
    public String thirdAddtionalAuthorAccountId() {
        return getValueOf("third-additional-author-account-id");
    }

    @Override
    public String thirdAdditionalAuthorName() {
        return getValueOf("third-additional-author-name");
    }

    @Override
    public String thirdAdditionalAuthorEmail() {
        return getValueOf("third-additional-author-email");
    }

    @Override
    public String thirdAdditionalAuthorUsername() {
        return getValueOf("third-additional-author-username");
    }

    @Override
    public String thirdAdditionalAuthorPassword() {
        return getValueOf("third-additional-author-password");
    }

    @Override
    public String authorForNameChangeAccountId() {
        return getValueOf("author-for-name-change-account-id");
    }

    @Override
    public String authorForNameChangeAccountEmail() {
        return getValueOf("author-for-name-change-account-email");
    }

    @Override
    public String firstAuthorsEventFeedPageId() {
        return getValueOf("first-authors-event-feed-page-id");
    }

    @Override
    public String authorImageBaseUrl() { return getValueOf("old-platform-author-image-base-url"); }

    @Override
    public String adminAccountUsername() {
        return getValueOf("admin-account-username");
    }

    @Override
    public String adminAccountPassword() {
        return getValueOf("admin-account-password");
    }

    @Override
    public String accountUsername() {
        return getValueOf("account-username");
    }

    @Override
    public String accountPassword() {
        return getValueOf("account-password");
    }

    @Override
    public String firstPapersEventFeedPageId() {
        return getValueOf("first-papers-event-feed-page-id");
    }

    @Override
    public String getAuthenticationUsername() {
        return getValueOf("authentication-username");
    }

    @Override
    public String getAuthenticationPassword() {
        return getValueOf("authentication-password");
    }

    @Override
    public String getArticlePageBaseUrl() {
        return getValueOf("old-platform-article-page-base-url");
    }

    @Override
    public String getAuthorProfileBaseUrl() {
        return getValueOf("old-platform-author-profile-page-base-url");
    }

    @Override
    public String authBaseUrl() {
        return getValueOf("old-platform-auth-base-url");
    }
}
