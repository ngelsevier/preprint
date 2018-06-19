package com.ssrn.fake_old_platform;

import com.ssrn.test.support.browser.Browser;
import com.ssrn.test.support.browser.BrowserBasedTestWatcher;
import com.ssrn.test.support.ssrn.SsrnOldPlatformWebsiteTest;
import com.ssrn.test.support.ssrn.website.BrowserBasedTestResources;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;

public abstract class SsrnFakeOldPlatformTest extends SsrnOldPlatformWebsiteTest {

    private static final int PAGE_LOAD_TIMEOUT_SECONDS = 10;
    private static BrowserBasedTestResources BROWSER_BASED_TEST_RESOURCES = null;

    @BeforeClass
    public static void createBrowserBasedTestResources() {
        BROWSER_BASED_TEST_RESOURCES = new BrowserBasedTestResources(
                getEnvironmentVariableAsBoolean("BROWSER_BASED_TESTING_VISIBLE", false),
                System.getenv("BROWSER_BASED_TESTING_DRIVER_LOG_FILE_PATH"),
                System.getenv("BROWSER_BASED_TESTING_SCREENSHOT_DIRECTORY_PATH"),
                PAGE_LOAD_TIMEOUT_SECONDS
        );
    }

    @AfterClass
    public static void shutDownService() {
        BROWSER_BASED_TEST_RESOURCES.tearDown();
    }

    @Rule
    public BrowserBasedTestWatcher browserBasedTestWatcher = BROWSER_BASED_TEST_RESOURCES.createBrowserBasedTestWatcher();

    @Before
    public void resetBrowser() {
        browser().reset();
    }

    public SsrnFakeOldPlatformTest() {
        super(Service.BASE_URL, Service.BASIC_AUTH_USERNAME, Service.BASIC_AUTH_PASSWORD, Service.PAPER_EVENTS_FEED_OLDEST_PAGE_ID, Service.AUTHOR_EVENTS_FEED_OLDEST_PAGE_ID, PAGE_LOAD_TIMEOUT_SECONDS);
    }

    protected Browser browser() {
        return BROWSER_BASED_TEST_RESOURCES.browser();
    }

    private static boolean getEnvironmentVariableAsBoolean(String name, boolean defaultValue) {
        return System.getenv(name) == null ? defaultValue : Boolean.parseBoolean(System.getenv(name));
    }
}
