package com.ssrn.papers.shared;

import com.ssrn.papers.utils.ExceptionUtils;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ExceptionUtilsTest {
    @Test
    public void shouldRethrowUncheckedException() {
        RuntimeException uncheckedException = new RuntimeException("Exception message");
        try {
            ExceptionUtils.throwAsUncheckedException(uncheckedException);
            fail("Expected exception to be thrown");
        } catch (Throwable thrownException) {
            assertThat(thrownException, is(sameInstance(uncheckedException)));
        }
    }

    @Test
    public void shouldWrapCheckedExceptionInRuntimeExceptionBeforeThrowing() {
        IOException checkedException = new IOException();

        try {
            ExceptionUtils.throwAsUncheckedException(checkedException);
            fail("Expected exception to be thrown");
        } catch (Throwable thrownException) {
            assertThat(thrownException, is(instanceOf(RuntimeException.class)));
            assertThat(thrownException.getCause(), is(sameInstance(checkedException)));
        }
    }
}