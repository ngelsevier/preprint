package com.ssrn.test.support.ssrn.website.pagemodel;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

public class SsrnWebsite {

    private final String baseUrl;
    private final String articlePageBaseUrl;
    private final String authorProfilePageBaseUrl;
    private final String authorImageBaseUrl;
    private final String authBaseUrl;
    private final String accountPassword;
    private final String accountUsername;
    private final String adminAccountUsername;
    private final String adminAccountPassword;
    private final String accountId;
    private final String accountName;
    private final String firstAdditionalAuthorAccountId;
    private final String firstAdditionalAuthorEmail;
    private final String firstAdditionalAuthorName;
    private final String firstAuthorAccountPassword;
    private final String secondAdditionalAuthorAccountId;
    private final String secondAdditionalAuthorEmail;
    private final String secondAdditionalAuthorName;
    private final String thirdAdditonalAuthorAccountId;
    private final String thirdAdditionalAuthorEmail;
    private final String thirdAdditionalAuthorName;
    private final String thirdAuthorAccountUsername;
    private final String thirdAuthorAccountPassword;
    private final String authorForNameChangeAccountId;
    private final String authorForNameChangeAccountEmail;

    private final WebTarget healthcheckWebTarget;
    private final int pageLoadTimeoutSeconds;

    public SsrnWebsite(String baseUrl, String articlePageBaseUrl, String authorProfilePageBaseUrl, String authorImageBaseUrl, String authBaseUrl, String accountUsername, String accountPassword, String accountId, String accountName, String adminAccountUsername, String adminAccountPassword, String firstAdditionalAuthorAccountId, String firstAdditionalAuthorName, String firstAdditionalAuthorEmail, String firstAuthorAccountPassword, String secondAdditionalAuthorAccountId, String secondAdditionalAuthorName, String secondAdditionalAuthorEmail, String thirdAdditonalAuthorAccountId, String thirdAdditionalAuthorName, String thirdAdditionalAuthorEmail, String thirdAuthorAccountUsername, String thirdAuthorAccountPassword, String authorForNameChangeAccountId, String authorForNameChangeAccountEmail, int pageLoadTimeoutSeconds) {
        this.baseUrl = baseUrl;
        this.authorProfilePageBaseUrl = authorProfilePageBaseUrl;
        this.authorImageBaseUrl = authorImageBaseUrl;
        this.authBaseUrl = authBaseUrl;
        this.accountUsername = accountUsername;
        this.accountPassword = accountPassword;
        this.healthcheckWebTarget = ClientBuilder.newClient().target(baseUrl).path("/login/pubSignInJoin.cfm");
        this.articlePageBaseUrl = articlePageBaseUrl;
        this.accountId = accountId;
        this.accountName = accountName;
        this.adminAccountUsername = adminAccountUsername;
        this.adminAccountPassword = adminAccountPassword;
        this.firstAdditionalAuthorAccountId = firstAdditionalAuthorAccountId;
        this.firstAdditionalAuthorName = firstAdditionalAuthorName;
        this.firstAdditionalAuthorEmail = firstAdditionalAuthorEmail;
        this.firstAuthorAccountPassword = firstAuthorAccountPassword;
        this.secondAdditionalAuthorEmail = secondAdditionalAuthorEmail;
        this.secondAdditionalAuthorName = secondAdditionalAuthorName;
        this.secondAdditionalAuthorAccountId = secondAdditionalAuthorAccountId;
        this.thirdAdditonalAuthorAccountId = thirdAdditonalAuthorAccountId;
        this.thirdAdditionalAuthorName = thirdAdditionalAuthorName;
        this.thirdAdditionalAuthorEmail = thirdAdditionalAuthorEmail;
        this.thirdAuthorAccountUsername = thirdAuthorAccountUsername;
        this.thirdAuthorAccountPassword = thirdAuthorAccountPassword;
        this.authorForNameChangeAccountId = authorForNameChangeAccountId;
        this.authorForNameChangeAccountEmail = authorForNameChangeAccountEmail;
        this.pageLoadTimeoutSeconds = pageLoadTimeoutSeconds;
    }

    public MyPapersPage myPapersPage() {
        return new MyPapersPage(baseUrl, "/submissions/MyPapers.cfm", this, pageLoadTimeoutSeconds);
    }

    public String accountPassword() {
        return accountPassword;
    }

    public String accountUsername() {
        return accountUsername;
    }

    public String adminAccountUsername() {
        return adminAccountUsername;
    }

    public String adminAccountPassword() {
        return adminAccountPassword;
    }

    public String accountId() {
        return accountId;
    }

    public String firstAdditionalAuthorName() {
        return firstAdditionalAuthorName;
    }

    public String firstAdditionalAuthorAccountId() {
        return firstAdditionalAuthorAccountId;
    }

    public String firstAdditionalAuthorEmail() {
        return firstAdditionalAuthorEmail;
    }

    public String firstAuthorAccountPassword() {
        return firstAuthorAccountPassword;
    }

    public LoginPage loginPage() {
        return new LoginPage(baseUrl, userHomePage(), pageLoadTimeoutSeconds);
    }

    public UserHomePage userHomePage() {
        return new UserHomePage(baseUrl, this);
    }

    public PaperSubmissionPage paperSubmissionPage() {
        return new PaperSubmissionPage(baseUrl, pageLoadTimeoutSeconds);
    }

    public ModifySubmissionPopup modifySubmissionPopup() {
        return new ModifySubmissionPopup(baseUrl, pageLoadTimeoutSeconds);
    }

    public PersonalInformationPage personalInformationPage(String partId) {
        return new PersonalInformationPage(baseUrl, partId);
    }

    public String secondAdditionalAuthorEmail() {
        return secondAdditionalAuthorEmail;
    }

    public String secondAdditionalAuthorName() {
        return secondAdditionalAuthorName;
    }

    public String secondAdditionalAuthorAccountId() {
        return secondAdditionalAuthorAccountId;
    }

    public String thirdAdditionalAuthorAccountId() {
        return thirdAdditonalAuthorAccountId;
    }

    public String thirdAdditionalAuthorName() {
        return thirdAdditionalAuthorName;
    }

    public String thirdAdditionalAuthorEmail() {
        return thirdAdditionalAuthorEmail;
    }

    public NavigationBar navigationBar() {
        return new NavigationBar(this, pageLoadTimeoutSeconds);
    }

    public SideBar sideBar() { return new SideBar(this, pageLoadTimeoutSeconds);}

    public Boolean isAvailable() {
        try {
            return Response.Status.OK.getStatusCode() == healthcheckWebTarget.request().get().getStatus();
        } catch (ProcessingException e) {
            return false;
        }
    }

    public ArticlePage articlePageForAbstract(String abstractId) {
        return new ArticlePage(articlePageBaseUrl, abstractId);
    }

    public AuthorProfilePage authorProfilePageFor(String authorId) {
        return new AuthorProfilePage(authorProfilePageBaseUrl, authorId);
    }

    public StageOneClassificationPage stageOneClassificationPage() {
        return new StageOneClassificationPage(baseUrl, this);
    }

    public RevisionQueuePage revisionQueuePage() {
        return new RevisionQueuePage(baseUrl);
    }

    public RevisionDetailPage revisionDetailPage() {
        return new RevisionDetailPage(baseUrl);
    }

    public PaperRevisionAcceptedPage paperRevisionAcceptedPage() {
        return new PaperRevisionAcceptedPage(baseUrl);
    }

    public String accountName() {
        return accountName;
    }

    public String thirdAuthorAccountUsername() {
        return thirdAuthorAccountUsername;
    }

    public String thirdAuthorAccountPassword() {
        return thirdAuthorAccountPassword;
    }

    public String authorForNameChangeAccountId() {
        return authorForNameChangeAccountId;
    }

    public String authorForNameChangeEmail(){
        return authorForNameChangeAccountEmail;
    }

    public String authorImageBaseUrl() {
        return authorImageBaseUrl;
    }

    public String authBaseUrl() {
        return authBaseUrl;
    }
}
