package com.ssrn.test.support.old_platform_contract_test.ssrn.website;

import com.ssrn.test.support.journey.AssumePreviousTestPassedWatcher;
import com.ssrn.test.support.journey.PreviousTestResult;
import org.junit.Rule;

public abstract class SsrnOldPlatformSequentialContractTest extends SsrnOldPlatformContractTest {
    private static PreviousTestResult previousTestResult;

    static {
        previousTestResult = PreviousTestResult.noPreviousTest();
    }

    @Rule
    public AssumePreviousTestPassedWatcher previousTestPassedWatcher = new AssumePreviousTestPassedWatcher(previousTestResult);

    public SsrnOldPlatformSequentialContractTest() {
        super(false);
    }
}
