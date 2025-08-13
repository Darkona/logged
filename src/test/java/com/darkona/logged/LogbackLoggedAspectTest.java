package com.darkona.logged;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.*;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestBootConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LogbackLoggedAspectTest {

    @Autowired
    private TestObject testObject;

    private ListAppender<ILoggingEvent> listAppender;
    private List<ILoggingEvent> logs;

    @BeforeEach
    void setup() {
        Logger logger = (Logger) LoggerFactory.getLogger(TestObject.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        listAppender.stop();
        logs = listAppender.list;
    }

    @Autowired
    private ApplicationContext context;

    @Test
    void shouldSeeLoggedAspectInContext() {
        String matchedBean = Arrays.stream(context.getBeanDefinitionNames())
                                   .filter(name -> name.toLowerCase().contains("loggedaspect"))
                                   .findFirst()
                                   .orElse(null);

        assertNotNull(matchedBean, "LoggedAspect bean should be present in the application context");

        System.out.println("Found LoggedAspect bean: " + matchedBean);
    }

    @Test
    void shouldBeProxied() {
        System.out.println("TestObject class: " + testObject.getClass());
    }

    private void callAndAssert(String methodName, Runnable methodCall, Consumer<List<ILoggingEvent>> assertions) {
        methodCall.run();
        logs = listAppender.list;
        assertFalse(logs.isEmpty(), methodName + ": logs should not be empty");
        assertions.accept(logs);
    }

    private boolean logsContain(String expected) {

        return logs.stream().anyMatch(e -> {

            if (e.getFormattedMessage()!=null) {
                System.out.println("log message=" + e.getFormattedMessage());
                return e.getFormattedMessage().contains(expected);
            }
            else return e.getLevel() == Level.ERROR;
        });
    }

    private void assertMessageContains(String expected) {
        assertTrue(logsContain(expected), "Expected log message to contain: " + expected);
    }



    @Test
    void callWithArgs() {
        callAndAssert("callWithArgs", () -> testObject.methodWithArgs("hello", 42), logs -> {
            assertMessageContains("hello");
            assertMessageContains("42");
        });
    }

    @Test
    void callWithoutArgs() {
        callAndAssert("callWithoutArgs", testObject::methodWithoutArgs, logs -> {
            assertFalse(logsContain("argValues"), "Args should not be present in log");
        });
    }

    @Test
    void callWithExecutionTime() {
        callAndAssert("callWithExecutionTime", testObject::methodWithTime, logs -> {
            assertMessageContains("Time taken");
        });
    }

    @Test
    void callWithoutExecutionTime() {
        callAndAssert("callWithoutExecutionTime", testObject::methodWithoutTime, logs -> {
            assertFalse(logsContain("Time taken"), "Execution time should not be logged");
        });
    }

    @Test
    void callWithNullReturnValue() {
        callAndAssert("callWithNullReturnValue", testObject::methodReturnsNull, logs -> {
            assertMessageContains("returned with value: null");
        });
    }

    @Test
    void callWithoutReturnValue() {
        callAndAssert("callWithoutReturnValue", testObject::methodWithoutReturnLogging, logs -> {
            assertFalse(logsContain("returned with value"), "Return value should not be logged");
        });
    }

    @Test
    void callWithReturnValueNullOnly() {
        callAndAssert("callWithReturnValueNullOnly", testObject::methodReturnsNullOnlyWhenNull, logs -> {
            assertMessageContains("returned with value: null");
        });

    }

    @Test
    void callWithReturnValueNullOnlyNonNull() {
        callAndAssert("callWithReturnValueNullOnlyNonNull", testObject::methodReturnsNonNullSuppressed, logs -> {
            assertFalse(logsContain("with value:"), "Return value should not be logged when non-null and returnValue = NULL");
        });
    }

    @Test
    void callWithArgValuesNullOnly() {
        callAndAssert("callWithArgValuesNullOnly", () -> testObject.methodWithNullArgValues("test", null), logs -> {
            assertFalse(logsContain("test"), "Non-null argument should not be logged");
            assertTrue(logsContain("null"), "Null argument should be logged");
        });
    }

    @Test
    void callWithException() {
        Exception ex = assertThrows(RuntimeException.class, testObject::methodThatThrows);
        assertEquals("kaboom", ex.getMessage());
        logs = listAppender.list;
        assertMessageContains("threw an");
    }

    @Test
    void callWithExceptionAndStacktrace() {
        Exception ex = assertThrows(RuntimeException.class, testObject::methodThatThrowsWithStacktrace);
        assertEquals("boom", ex.getMessage());
        logs = listAppender.list;
        assertMessageContains("threw a");
        assertTrue(logs.stream().anyMatch(e -> e.getFormattedMessage().contains("at")), "Expected stack trace in logs");
    }

    @Test
    void callWithExceptionWithoutLogging() {
        Exception ex = assertThrows(RuntimeException.class, testObject::methodThatThrowsNoLogging);
        assertEquals("silent fail", ex.getMessage());
        logs = listAppender.list;
        assertFalse(logsContain("threw a"), "Exception should not be logged");
    }

    @Test
    void callWithCustomMessages() {
        callAndAssert("callWithCustomMessages", testObject::methodWithCustomMessages, logs -> {
            assertMessageContains("🧪 calling method");
            assertMessageContains("✅ method done");
        });
    }

    @Test
    void callWithArgValuesNone() {
        callAndAssert("callWithArgValuesNone", () -> testObject.methodWithArgValuesNone("something"), logs -> {
            assertFalse(logsContain("something"), "Argument value should not be present");
        });
    }

    @Test
    void callWithDefaults() {
        callAndAssert("callWithDefaults", testObject::methodWithDefaults, logs -> {
            assertMessageContains("called with args");
            assertMessageContains("returned with value");
            assertMessageContains("Time taken");
        });
    }
}
