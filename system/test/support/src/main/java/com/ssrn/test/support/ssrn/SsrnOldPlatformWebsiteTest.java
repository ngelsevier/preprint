package com.ssrn.test.support.ssrn;

import com.ssrn.test.support.golden_data.FakeSsrnUsers;
import com.ssrn.test.support.http.HttpClient;
import com.ssrn.test.support.http.HttpClientConfiguration;
import com.ssrn.test.support.ssrn.api.SsrnOldPlatformApiTest;
import com.ssrn.test.support.ssrn.website.pagemodel.SsrnWebsite;
import org.junit.Before;

import static com.ssrn.test.support.utils.WaitForConditionFluentSyntax.waitUntil;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public abstract class SsrnOldPlatformWebsiteTest extends SsrnOldPlatformApiTest {

    private final SsrnWebsite ssrnWebsite;

    @Before
    public void ensureSsrnWebsiteAvailable() {
        waitUntil(ssrnWebsite::isAvailable)
                .checkingEvery(100, MILLISECONDS)
                .forNoMoreThan(30, SECONDS);
    }

    public SsrnOldPlatformWebsiteTest(String baseUrl, String username, String password, String firstPapersEventFeedPageId, String firstAuthorsEventFeedPageId, int pageLoadTimeoutSeconds) {
        this(HttpClient.defaultConfiguration(), ssrnConfigurationWith(baseUrl, username, password, firstPapersEventFeedPageId, baseUrl, baseUrl, firstAuthorsEventFeedPageId, baseUrl, baseUrl), pageLoadTimeoutSeconds);
    }

    protected SsrnOldPlatformWebsiteTest(HttpClientConfiguration httpClientConfiguration, SsrnConfiguration ssrnConfiguration, int pageLoadTimeoutSeconds) {
        super(httpClientConfiguration, ssrnConfiguration);

        ssrnWebsite = new SsrnWebsite(
                ssrnConfiguration.baseUrl(),
                ssrnConfiguration.getArticlePageBaseUrl(),
                ssrnConfiguration.getAuthorProfileBaseUrl(),
                ssrnConfiguration.authorImageBaseUrl(),
                ssrnConfiguration.authBaseUrl(),
                ssrnConfiguration.accountUsername(),
                ssrnConfiguration.accountPassword(),
                ssrnConfiguration.accountId(),
                ssrnConfiguration.accountName(),
                ssrnConfiguration.adminAccountUsername(),
                ssrnConfiguration.adminAccountPassword(),
                ssrnConfiguration.firstAdditionalAuthorAccountId(),
                ssrnConfiguration.firstAdditionalAuthorName(),
                ssrnConfiguration.firstAdditionalAuthorEmail(),
                ssrnConfiguration.firstAdditionalAuthorPassword(),
                ssrnConfiguration.secondAdditionalAuthorAccountId(),
                ssrnConfiguration.secondAdditionalAuthorName(),
                ssrnConfiguration.secondAdditionalAuthorEmail(),
                ssrnConfiguration.thirdAddtionalAuthorAccountId(),
                ssrnConfiguration.thirdAdditionalAuthorName(),
                ssrnConfiguration.thirdAdditionalAuthorEmail(),
                ssrnConfiguration.thirdAdditionalAuthorUsername(),
                ssrnConfiguration.thirdAdditionalAuthorPassword(),
                ssrnConfiguration.authorForNameChangeAccountId(),
                ssrnConfiguration.authorForNameChangeAccountEmail(),
                pageLoadTimeoutSeconds
        );
    }


    protected SsrnWebsite ssrnWebsite() {
        return ssrnWebsite;
    }

    private static SsrnConfiguration ssrnConfigurationWith(final String baseUrl, final String authenticationUsername, final String authenticationPassword, final String firstPapersEventFeedPageId, final String articlePageBaseUrl, String authorProfileBaseUrl, final String firstAuthorsEventFeedPageId, String authorImageBaseUrl, String authBaseUrl) {
        return new SsrnConfiguration() {

            @Override
            public String baseUrl() {
                return baseUrl;
            }

            @Override
            public String accountUsername() {
                return FakeSsrnUsers.JOHN_DOE.getUsername();
            }

            @Override
            public String accountPassword() {
                return "password";
            }

            @Override
            public String accountId() {
                return FakeSsrnUsers.JOHN_DOE.getId();
            }

            @Override
            public String accountName() {
                return FakeSsrnUsers.JOHN_DOE.getPublicDisplayName();
            }

            @Override
            public String firstAdditionalAuthorAccountId() {
                return FakeSsrnUsers.JAMES_BROWN.getId();
            }

            @Override
            public String firstAdditionalAuthorName() {
                return FakeSsrnUsers.JAMES_BROWN.getPublicDisplayName();
            }

            @Override
            public String firstAdditionalAuthorEmail() {
                return FakeSsrnUsers.JAMES_BROWN.getEmailAddress();
            }

            @Override
            public String firstAdditionalAuthorPassword() {
                return "password";
            }

            @Override
            public String secondAdditionalAuthorAccountId() {
                return FakeSsrnUsers.JOE_BLACK.getId();
            }

            @Override
            public String secondAdditionalAuthorName() {
                return FakeSsrnUsers.JOE_BLACK.getPublicDisplayName();
            }

            @Override
            public String thirdAddtionalAuthorAccountId() {
                return FakeSsrnUsers.TIM_DUNCAN.getId();
            }

            @Override
            public String thirdAdditionalAuthorName() {
                return FakeSsrnUsers.TIM_DUNCAN.getPublicDisplayName();
            }

            @Override
            public String thirdAdditionalAuthorEmail() {
                return FakeSsrnUsers.TIM_DUNCAN.getEmailAddress();
            }

            @Override
            public String thirdAdditionalAuthorUsername() {
                return FakeSsrnUsers.TIM_DUNCAN.getUsername();
            }

            @Override
            public String thirdAdditionalAuthorPassword() {
                return "password";
            }

            @Override
            public String authorForNameChangeAccountId() {
                return FakeSsrnUsers.CHANGE_ME.getId();
            }

            @Override
            public String authorForNameChangeAccountEmail() {
                return FakeSsrnUsers.CHANGE_ME.getEmailAddress();
            }

            @Override
            public String firstAuthorsEventFeedPageId() {
                return firstAuthorsEventFeedPageId;
            }

            @Override
            public String authorImageBaseUrl() { return authorImageBaseUrl; }

            @Override
            public String adminAccountUsername() { return FakeSsrnUsers.AN_ADMIN.getId(); }

            @Override
            public String adminAccountPassword() { return "password"; }

            @Override
            public String secondAdditionalAuthorEmail() {
                return FakeSsrnUsers.JOE_BLACK.getEmailAddress();
            }

            @Override
            public String firstPapersEventFeedPageId() {
                return firstPapersEventFeedPageId;
            }

            @Override
            public String getAuthenticationUsername() {
                return authenticationUsername;
            }

            @Override
            public String getAuthenticationPassword() {
                return authenticationPassword;
            }

            @Override
            public String getArticlePageBaseUrl() {
                return articlePageBaseUrl;
            }

            @Override
            public String getAuthorProfileBaseUrl() {
                return authorProfileBaseUrl;
            }

            @Override
            public String authBaseUrl() {
                return authBaseUrl;
            }
        };
    }

}
