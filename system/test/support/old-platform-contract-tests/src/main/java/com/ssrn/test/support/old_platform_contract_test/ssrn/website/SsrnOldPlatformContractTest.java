package com.ssrn.test.support.old_platform_contract_test.ssrn.website;

import com.ssrn.test.support.browser.Browser;
import com.ssrn.test.support.browser.BrowserBasedTestWatcher;
import com.ssrn.test.support.old_platform_contract_test.configuration.Configuration;
import com.ssrn.test.support.old_platform_contract_test.ssrn.api.SsrnApi;
import com.ssrn.test.support.ssrn.SsrnOldPlatformWebsiteTest;
import com.ssrn.test.support.ssrn.website.BrowserBasedTestResources;
import com.ssrn.test.support.ssrn.website.pagemodel.PaperSubmissionPage;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;

import java.util.UUID;

public abstract class SsrnOldPlatformContractTest extends SsrnOldPlatformWebsiteTest {
    private static BrowserBasedTestResources BROWSER_BASED_TEST_RESOURCES = null;

    private final boolean resetBrowserBeforeEachTest;
    private SsrnApi ssrnApi;

    @BeforeClass
    public static void createBrowserBasedTestResources() {
        BROWSER_BASED_TEST_RESOURCES = new BrowserBasedTestResources(
                Configuration.browserBasedTesting().driverExecutableFilePath(),
                Configuration.browserBasedTesting().visible(),
                Configuration.browserBasedTesting().driverLogFilePath(),
                Configuration.browserBasedTesting().screenshotDirectoryPath(),
                Configuration.browserBasedTesting().pageLoadTimeoutSeconds()
        );
    }

    @AfterClass
    public static void shutDownService() {
        BROWSER_BASED_TEST_RESOURCES.browser().close();
    }

    @Rule
    public BrowserBasedTestWatcher browserBasedTestWatcher = BROWSER_BASED_TEST_RESOURCES.createBrowserBasedTestWatcher();

    @Before
    public void optionallyResetBrowser() {
        if (resetBrowserBeforeEachTest) {
            BROWSER_BASED_TEST_RESOURCES.browser().reset();
        }
    }

    public SsrnOldPlatformContractTest() {
        this(true);
    }

    public SsrnOldPlatformContractTest(boolean resetBrowserBeforeEachTest) {
        super(Configuration.httpClient(), Configuration.ssrn(), Configuration.browserBasedTesting().pageLoadTimeoutSeconds());
        this.resetBrowserBeforeEachTest = resetBrowserBeforeEachTest;
    }

    protected Browser browser() {
        return BROWSER_BASED_TEST_RESOURCES.browser();
    }

    protected SsrnApi ssrnApi() {
        if (ssrnApi == null) {
            ssrnApi = new SsrnApi(Configuration.ssrn().baseUrl(), httpClient(), ssrnBasicAuthenticationHeader());
        }

        return ssrnApi;
    }
}
