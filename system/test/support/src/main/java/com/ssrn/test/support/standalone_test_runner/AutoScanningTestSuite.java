package com.ssrn.test.support.standalone_test_runner;

import org.junit.extensions.cpsuite.ClasspathSuite;
import org.junit.runner.RunWith;

@RunWith(ClasspathSuite.class)
@ClasspathSuite.IncludeJars(true)
@ClasspathSuite.ClassnameFilters({"com.ssrn.*"})
public class AutoScanningTestSuite {

}
