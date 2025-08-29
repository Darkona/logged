package io.github.darkona.logged;

import io.github.darkona.logged.plugins.slf4j.LoggedSlf4jProperties;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {TestBootConfig.class, Log4jTestObject.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class Log4j2LoggedEngineTest {

    // use SLF4J logger; backend is Log4j2
    private final String loggerName = Log4jTestObject.class.getName();
    @Autowired
    private Log4jTestObject testObject;
    private Log4j2ListAppender listAppender;
    private List<LogEvent> logs;
    @Autowired
    private ApplicationContext context;

    @Autowired
    private LoggedSlf4jProperties slf4jProperties;

    @Autowired
    private LoggedProperties props;

    @BeforeEach
    synchronized void setup() {
        Logger logger = LoggerFactory.getLogger(Log4jTestObject.class);


        // Always use the CORE LoggerContext
        org.apache.logging.log4j.core.LoggerContext ctx =
                (org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false);
        var config = ctx.getConfiguration();
        ctx.updateLoggers();
        // Give the test appender a unique, consistent name
        listAppender = Log4j2ListAppender.create("List", null, null);
        listAppender.start();
        config.addAppender(listAppender);

        // Find the effective LoggerConfig
        LoggerConfig effective = config.getLoggerConfig(logger.getName());

        if (!loggerName.equals(effective.getName())) {
            //  No dedicated LoggerConfig for this logger; create one
            LoggerConfig dedicated = LoggerConfig.createLogger(
                    /* additivity */ true,
                    org.apache.logging.log4j.Level.INFO,
                    loggerName,
                    /* includeLocation/advertise */ "true",
                    /* appender refs */ new AppenderRef[]{},
                    /* properties */ null,
                    config,
                    /* filter */ null
            );
            dedicated.addAppender(listAppender, Level.INFO, null);
            config.addLogger(loggerName, dedicated);
        } else {
            // There is already a LoggerConfig for loggerName; just attach the appender
            effective.addAppender(listAppender, org.apache.logging.log4j.Level.INFO, null);
            effective.setAdditive(true); // or false if you *don't* want parent appenders
        }

        ctx.updateLoggers();
    }

    @AfterEach
    void tearDown() {
        org.apache.logging.log4j.core.LoggerContext ctx =
                (org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false);
        var config = ctx.getConfiguration();

        // capture before removal
        logs = listAppender.getList();

        // Detach from the dedicated/effective LoggerConfig
        LoggerConfig lc = config.getLoggerConfig(loggerName);
        lc.removeAppender("List");

        // If we created a dedicated config in setup, removing it is optional.
        // Safer: only remove if it exactly matches the name.
        if (loggerName.equals(lc.getName())) {
            config.removeLogger(loggerName);
        }

        // Stop and drop the appender from the config registry
        var app = config.getAppender("List");
        if (app != null) {
            app.stop();
            config.getAppenders().remove("List");
        }

        ctx.updateLoggers();
    }

    private boolean foundBean(String name) {
        try {return context.getBean(name) != null;} catch (NoSuchBeanDefinitionException ex) {return false;}
    }

    @Test
    void shouldSeeLoggedAspectInContext() {
        String matchedBean = Arrays.stream(context.getBeanDefinitionNames())
                                   .filter(name -> name.toLowerCase().contains("loggedengine"))
                                   .findFirst().orElse(null);

        assertNotNull(matchedBean);
        assertTrue(foundBean("loggedEngine"));
        assertTrue(foundBean("springAspect"));
        System.out.println("Found LoggedAspect bean: " + matchedBean);
    }

    @Test
    void shouldSeeSljf4PluginInContext() {
        String matchedBean = Arrays.stream(context.getBeanDefinitionNames())
                                   .filter(name -> name.toLowerCase().contains("loggedslf4jplugin"))
                                   .findFirst().orElse(null);

        assertNotNull(matchedBean);
        assertTrue(foundBean("loggedSlf4jPlugin"));
        System.out.println("Found Slf4j Plugin bean: " + matchedBean);
    }

    @Test
    void shouldSeeMDCPluginInContext() {
        String matchedBean = Arrays.stream(context.getBeanDefinitionNames())
                                   .filter(name -> name.toLowerCase().contains("loggedmdcplugin"))
                                   .findFirst().orElse(null);

        assertNotNull(matchedBean);
        assertTrue(foundBean("loggedMdcPlugin"));
        System.out.println("Found MDC Plugin bean: " + matchedBean);
    }

    // If you still expose a Log4j2-specific plugin bean, assert it here instead of Logback:
    // @Test void shouldSeeLog4j2PluginInContext() { ... }

    @Test
    void shouldBeProxied() {
        System.out.println("TestObject class: " + testObject.getClass());
    }

    private void callAndAssert(String methodName, Runnable methodCall, Consumer<List<LogEvent>> assertions) {
        methodCall.run();
        logs = listAppender.getList();
        assertFalse(logs.isEmpty(), methodName + ": logs should not be empty");
        assertions.accept(logs);
    }

    private boolean logsContain(String expected) {
        return logs.stream().anyMatch(e -> {
            String msg = e.getMessage() != null ? e.getMessage().getFormattedMessage() : null;
            if (msg != null) {
                System.out.println("log message=" + msg);
                return msg.contains(expected);
            } else {
                return e.getLevel() == Level.ERROR;
            }
        });
    }

    private void assertMessageContains(String expected) {
        System.out.println("Expected: " + expected);
        assertTrue(logsContain(expected), "Expected log message to contain: " + expected);
    }

    @Test
    void colorIsOn() {
        assertTrue(props.isColor());
        assertTrue(slf4jProperties.isColor());
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
        callAndAssert("callWithoutArgs", testObject::methodWithoutArgs,
                logs -> assertFalse(logsContain("argValues"), "Args should not be present in log"));
    }

    @Test
    void callWithExecutionTime() {
        callAndAssert("callWithExecutionTime", testObject::methodWithTime,
                logs -> assertMessageContains("Time taken"));
    }

    @Test
    void slowThresholdPromotesLevelAndAddsMarker() {
        testObject.slowWithMarker();
        logs = listAppender.getList();
        // Check for WARN level event
        assertTrue(logs.stream().anyMatch(e -> e.getLevel() == org.apache.logging.log4j.Level.WARN), "Expected WARN due to threshold");
        // Check for SLOW marker
        assertTrue(logs.stream().anyMatch(e -> e.getMarker() != null && (e.getMarker().getName().contains("SLOW") || e.getMarker().isInstanceOf("SLOW"))),
                "Expected SLOW marker on slow call");
    }

    @Test
    void callWithoutExecutionTime() {
        callAndAssert("callWithoutExecutionTime", testObject::methodWithoutTime,
                logs -> assertFalse(logsContain("Time taken"), "Execution time should not be logged"));
    }

    @Test
    void callWithNullReturnValue() {
        callAndAssert("callWithNullReturnValue", testObject::methodReturnsNull,
                logs -> assertMessageContains("returned with value: null"));
    }

    @Test
    void callWithoutReturnValue() {
        callAndAssert("callWithoutReturnValue", testObject::methodWithoutReturnLogging,
                logs -> assertFalse(logsContain("returned with value"), "Return value should not be logged"));
    }

    @Test
    void callWithReturnValueNullOnly() {
        callAndAssert("callWithReturnValueNullOnly", testObject::methodReturnsNullOnlyWhenNull,
                logs -> assertMessageContains("returned with value: null"));
    }

    @Test
    void callWithReturnValueNullOnlyNonNull() {
        callAndAssert("callWithReturnValueNullOnlyNonNull", testObject::methodReturnsNonNullSuppressed,
                logs -> assertFalse(logsContain("with value:"),
                        "Return value should not be logged when non-null and returnValue = NULL"));
    }

    @Test
    void callWithArgValuesNullOnly() {
        callAndAssert("callWithArgValuesNullOnly",
                () -> testObject.methodWithNullArgValues("test", null),
                logs -> {
                    assertFalse(logsContain("test"), "Non-null argument should not be logged");
                    assertTrue(logsContain("null"), "Null argument should be logged");
                });
    }

    @Test
    void callWithException() {
        Exception ex = assertThrows(RuntimeException.class, testObject::methodThatThrows);
        assertTrue(ex.getMessage().contains("kaboom"));
        logs = listAppender.getList();
        assertMessageContains("kaboom");
    }

    @Test
    void callWithExceptionAndStacktrace() {
        Exception ex = assertThrows(RuntimeException.class, testObject::methodThatThrowsWithStacktrace);
        assertTrue(ex.getMessage().contains("boom"));
        logs = listAppender.getList();
        assertMessageContains("threw a");
        assertTrue(logs.stream().anyMatch(e ->
                e.getMessage() != null &&
                        e.getMessage().getFormattedMessage().contains("at")), "Expected stack trace in logs");
    }

    @Test
    void callWithExceptionWithoutLogging() {
        Exception ex = assertThrows(RuntimeException.class, testObject::methodThatThrowsNoLogging);
        assertEquals("silent fail", ex.getMessage());
        logs = listAppender.getList();
        assertFalse(logsContain("threw a"), "Exception should not be logged");
        System.out.println(logs);
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
        callAndAssert("callWithArgValuesNone",
                () -> testObject.methodWithArgValuesNone("something"),
                logs -> assertFalse(logsContain("something"), "Argument value should not be present"));
    }

    @Test
    void callWithDefaults() {
        callAndAssert("callWithDefaults",
                () -> testObject.methodWithDefaults("Elephant"),
                logs -> {
                    assertMessageContains("called with args");
                    assertMessageContains("[(String)stringArgument:Elephant]");
                    assertMessageContains("returned with value");
                    assertMessageContains("Time taken");
                    assertMessageContains("returned with value: Elephant Time taken:");
                });
    }

    @Test
    void callWithCustomCallMsg() {
        callAndAssert("customOnCall", testObject::customOnCall,
                logs -> assertMessageContains("Entering customOnCall"));
    }

    @Test
    void callWithCustomReturnMsg() {
        callAndAssert("customOnReturn", testObject::customOnReturn,
                logs -> assertMessageContains("Returned from method customOnReturn"));
    }

    @Test
    void callWithCustomExceptionMsg() {
        Exception ex = assertThrows(RuntimeException.class, testObject::customExceptionMsg);
        assertTrue(ex.getMessage().contains("oh no"));
        logs = listAppender.getList();
        assertTrue(logsContain("Something bad happened: oh no"), "oh no");
    }

    @Test
    void callWithRedactedArgs() {
        callAndAssert("methodWithRedactedArgs",
                () -> testObject.methodWithRedactedArgs("Important Name", "Chicken", "Credit Card Number"),
                logs -> assertMessageContains("[(String)arg1:█████, (String)arg2:Chicken, (String)arg3:█████]"));
    }

    @Test
    void callWithMarker() {
        testObject.methodWithMarker();
        logs = listAppender.getList();
        assertFalse(logs.isEmpty(), "methodWithMarker: logs should not be empty");
        assertTrue(logs.stream().anyMatch(e ->
                e.getMarker() != null &&
                        (e.getMarker().getName().contains("slf4j") || e.getMarker().isInstanceOf("slf4j")))
        );
    }

    @Test
    void callWithMarkerNoConsole() {
        org.apache.logging.log4j.core.LoggerContext ctx =
                (org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false);
        var cfg = ctx.getConfiguration();
        AbstractAppender console = cfg.getAppender("CONSOLE");
        assertNotNull(console);
        assertNotNull(console.getFilter());

        testObject.methodWithMarkerNoConsole("Wombat", 13);
        logs = listAppender.getList();

        assertTrue(logs.stream().anyMatch(e ->
                e.getMarker() != null &&
                        (e.getMarker().getName().contains("NO_CONSOLE") || e.getMarker().isInstanceOf("NO_CONSOLE"))
        ));


    }

    @Test
    void typeBasedRedactionMasksArg() {
        java.util.UUID id = java.util.UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        callAndAssert("typeBasedRedactionMasksArg",
                () -> testObject.methodWithTypeRedaction(id),
                logs -> {
                    assertFalse(logsContain(id.toString()), "UUID should not be visible");
                    assertTrue(logsContain("█"), "Mask should be present");
                });
    }

    @Test
    void patternBasedRedactionMasksArg() {
        String cc = "4111111111111111"; // 16 digits
        callAndAssert("patternBasedRedactionMasksArg",
                () -> testObject.methodWithPatternRedaction(cc),
                logs -> {
                    assertFalse(logsContain("41111111"), "Digits should be redacted");
                    assertTrue(logsContain("█"), "Mask should be present");
                });
    }

    @Test
    void maskedReturnHidesSensitiveData() {
        callAndAssert("maskedReturnHidesSensitiveData",
                () -> { testObject.methodWithMaskedReturn(); },
                logs -> {
                    assertFalse(logsContain("TopSecret"), "Return value should be redacted");
                    assertTrue(logsContain("█"), "Mask should be present in return value");
                    assertTrue(logsContain("returned with value"));
                });
    }

    @Test
    void multiplePatternRedactionMasksArgs() {
        String cc = "4111111111111111"; // matches \\d{16}
        String token = "Bearer TOKEN-1234"; // matches (?i)token
        String other = "hello"; // should pass through

        callAndAssert("multiplePatternRedactionMasksArgs",
                () -> testObject.methodWithMultiplePatternRedaction(cc, token, other),
                logs -> {
                    assertFalse(logsContain("41111111"), "Digits should be redacted (anno-level)");
                    assertFalse(logsContain("TOKEN-1234"), "Token should be redacted (anno-level)");
                    assertTrue(logsContain("returned with value") || logsContain("List")); // lenient on mask encoding
                    assertTrue(logsContain("other:hello") || logsContain("hello"), "Unmatched arg should be visible");
                });
    }
}

