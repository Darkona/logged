package io.github.darkona.logged;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.github.darkona.logged.plugins.logback.LogbackMarkerFilter;
import io.github.darkona.logged.plugins.slf4j.LoggedSlf4jProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static io.github.darkona.logged.plugins.logback.LoggedLogbackPlugin.findInAttachable;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = LogbackBootConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("logback")
class LogbackLoggedEngineTest {

    @Autowired
    private LogbackTestObject logbackTestObject;

    private ListAppender<ILoggingEvent> listAppender;
    private List<ILoggingEvent> logs;
    Logger logger = (Logger) LoggerFactory.getLogger(LogbackTestObject.class);

    @Autowired
    private ApplicationContext context;

    @Autowired
    private LoggedSlf4jProperties slof4JProps;

    @Autowired
    private LoggedProperties props;

    @BeforeEach
    synchronized void  setup() {
        listAppender = new ListAppender<>();
        listAppender.setContext(logger.getLoggerContext());
        listAppender.setName("ListAppender");
        listAppender.start();

        logger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(listAppender);
        listAppender.stop();
        logs = listAppender.list;
    }

    private boolean foundBean(String name) {
        try { //noinspection ConstantValue
            return context.getBean(name) != null;
        } catch (NoSuchBeanDefinitionException ex) {return false;}
    }

    @Test
    void shouldSeeLoggedAspectInContext() {
        String matchedBean = Arrays.stream(context.getBeanDefinitionNames())
                                   .filter(name -> name.toLowerCase().contains("loggedengine"))
                                   .findFirst()
                                   .orElse(null);
        Assertions.assertNotNull(matchedBean);
        Assertions.assertTrue(foundBean("loggedEngine"));
        Assertions.assertTrue(foundBean("springAspect"));

        System.out.println("Found LoggedAspect bean: " + matchedBean);
    }

    @Test
    void shouldSeeSljf4PluginInContext() {
        String matchedBean = Arrays.stream(context.getBeanDefinitionNames())
                                   .filter(name -> name.toLowerCase().contains("loggedslf4jplugin"))
                                   .findFirst()
                                   .orElse(null);
        Assertions.assertNotNull(matchedBean);
        Assertions.assertTrue(foundBean("loggedSlf4jPlugin"));

        System.out.println("Found Slf4j Plugin bean: " + matchedBean);
    }

    @Test
    void shouldSeeMDCPluginInContext() {
        String matchedBean = Arrays.stream(context.getBeanDefinitionNames())
                                   .filter(name -> name.toLowerCase().contains("loggedmdcplugin"))
                                   .findFirst()
                                   .orElse(null);
        Assertions.assertNotNull(matchedBean);
        Assertions.assertTrue(foundBean("loggedMdcPlugin"));

        System.out.println("Found MDC Plugin bean: " + matchedBean);
    }

    @Test
    void shouldSeeOtelPluginInContext() {
        String matchedBean = Arrays.stream(context.getBeanDefinitionNames())
                                   .filter(name -> name.toLowerCase().contains("loggedopentelemetryplugin"))
                                   .findFirst()
                                   .orElse(null);
        Assertions.assertNotNull(matchedBean);
        Assertions.assertTrue(foundBean("loggedOpenTelemetryPlugin"));

        System.out.println("Found Slf4j Plugin bean: " + matchedBean);
    }

    @Test
    void shouldSeeLogbackPluginInContext() {
        String matchedBean = Arrays.stream(context.getBeanDefinitionNames())
                                   .filter(name -> name.toLowerCase().contains("loggedlogbackplugin"))
                                   .findFirst()
                                   .orElse(null);
        Assertions.assertNotNull(matchedBean);
        Assertions.assertTrue(foundBean("loggedLogbackPlugin"));

        System.out.println("Found Slf4j Plugin bean: " + matchedBean);
    }
    @Test
    void shouldBeProxied() {
        System.out.println("TestObject class: " + logbackTestObject.getClass());
    }

    private void callAndAssert(String methodName, Runnable methodCall, Consumer<List<ILoggingEvent>> assertions) {
        methodCall.run();
        logs = listAppender.list;
        Assertions.assertFalse(logs.isEmpty(), methodName + ": logs should not be empty");
        assertions.accept(logs);
    }

    private boolean logsContain(String expected) {

        return logs.stream().anyMatch(e -> {

            if (e.getFormattedMessage() != null) {
                System.out.println("log message=" + e.getFormattedMessage());
                return e.getFormattedMessage().contains(expected);
            } else return e.getLevel() == Level.ERROR;
        });
    }

    private void assertMessageContains(String expected) {
        System.out.println("Expected: " + expected);
        Assertions.assertTrue(logsContain(expected), "Expected log message to contain: " + expected);
    }

    @Test
    void colorIsOn(){
        Assertions.assertTrue(props.isColor());
        Assertions.assertTrue(slof4JProps.isColor());
    }

    @Test
    void callWithArgs() {
        callAndAssert("callWithArgs", () -> logbackTestObject.methodWithArgs("hello", 42), logs -> {
            assertMessageContains("hello");
            assertMessageContains("42");
        });
    }

    @Test
    void callWithoutArgs() {
        callAndAssert("callWithoutArgs", logbackTestObject::methodWithoutArgs, logs -> Assertions.assertFalse(logsContain("argValues"), "Args should not be present in log"));
    }

    @Test
    void callWithExecutionTime() {
        callAndAssert("callWithExecutionTime", logbackTestObject::methodWithTime, logs -> assertMessageContains("Time taken"));
    }

    @Test
    void callWithoutExecutionTime() {
        callAndAssert("callWithoutExecutionTime", logbackTestObject::methodWithoutTime, logs -> Assertions.assertFalse(logsContain("Time taken"), "Execution time should not be logged"));
    }

    @Test
    void callWithNullReturnValue() {
        callAndAssert("callWithNullReturnValue", logbackTestObject::methodReturnsNull, logs -> assertMessageContains("returned with value: null"));
    }

    @Test
    void callWithoutReturnValue() {
        callAndAssert("callWithoutReturnValue", logbackTestObject::methodWithoutReturnLogging, logs -> Assertions.assertFalse(logsContain("returned with value"), "Return value should not be logged"));
    }

    @Test
    void callWithReturnValueNullOnly() {
        callAndAssert("callWithReturnValueNullOnly", logbackTestObject::methodReturnsNullOnlyWhenNull, logs -> assertMessageContains("returned with value: null"));

    }

    @Test
    void callWithReturnValueNullOnlyNonNull() {
        callAndAssert("callWithReturnValueNullOnlyNonNull", logbackTestObject::methodReturnsNonNullSuppressed, logs -> Assertions.assertFalse(logsContain("with value:"), "Return value should not be logged when non-null and returnValue = NULL"));
    }

    @Test
    void callWithArgValuesNullOnly() {
        callAndAssert("callWithArgValuesNullOnly", () -> logbackTestObject.methodWithNullArgValues("test", null), logs -> {
            Assertions.assertFalse(logsContain("test"), "Non-null argument should not be logged");
            Assertions.assertTrue(logsContain("null"), "Null argument should be logged");
        });
    }

    @Test
    void callWithException() {
        Exception ex = Assertions.assertThrows(RuntimeException.class, logbackTestObject::methodThatThrows);
        Assertions.assertTrue(ex.getMessage().contains("kaboom"));
        logs = listAppender.list;
        assertMessageContains("kaboom");
    }

    @Test
    void callWithExceptionAndStacktrace() {
        Exception ex = Assertions.assertThrows(RuntimeException.class, logbackTestObject::methodThatThrowsWithStacktrace);
        Assertions.assertTrue( ex.getMessage().contains("boom"));
        logs = listAppender.list;
        assertMessageContains("threw a");
        Assertions.assertTrue(logs.stream().anyMatch(e -> e.getFormattedMessage().contains("at")), "Expected stack trace in logs");
    }

    @Test
    void callWithExceptionWithoutLogging() {
        Exception ex = Assertions.assertThrows(RuntimeException.class, logbackTestObject::methodThatThrowsNoLogging);
        Assertions.assertEquals("silent fail", ex.getMessage());
        logs = listAppender.list;
        Assertions.assertFalse(logsContain("threw a"), "Exception should not be logged");
        System.out.println(logs);
    }

    @Test
    void callWithCustomMessages() {
        callAndAssert("callWithCustomMessages", logbackTestObject::methodWithCustomMessages, logs -> {
            assertMessageContains("🧪 calling method");
            assertMessageContains("✅ method done");
        });
    }

    @Test
    void callWithArgValuesNone() {
        callAndAssert("callWithArgValuesNone", () -> logbackTestObject.methodWithArgValuesNone("something"), logs -> Assertions.assertFalse(logsContain("something"), "Argument value should not be present"));
    }

    @Test
    void callWithDefaults() {
        callAndAssert("callWithDefaults", () -> logbackTestObject.methodWithDefaults("Elephant"), logs -> {
            assertMessageContains("called with args");
            assertMessageContains("[(String)stringArgument:Elephant]");
            assertMessageContains("returned with value");
            assertMessageContains("Time taken");
            assertMessageContains("returned with value: Elephant Time taken:");
        });


    }

    @Test
    void callWithCustomCallMsg() {
        callAndAssert("customOnCall", logbackTestObject::customOnCall, logs -> assertMessageContains("Entering customOnCall"));
    }

    @Test
    void callWithCustomReturnMsg() {
        callAndAssert("customOnReturn", logbackTestObject::customOnReturn, logs -> assertMessageContains("Returned from method customOnReturn"));
    }

    @Test
    void callWithCustomExceptionMsg() {
        Exception ex = Assertions.assertThrows(RuntimeException.class, logbackTestObject::customExceptionMsg);
        Assertions.assertTrue( ex.getMessage().contains("oh no"));
        logs = listAppender.list;
        Assertions.assertTrue(logsContain("Something bad happened: oh no"), "oh no");
    }

    @Test
    void callWithRedactedArgs() {
        callAndAssert("methodWithRedactedArgs",
                () -> logbackTestObject.methodWithRedactedArgs("Important Name", "Chicken", "Credit Card Number"),
                logs -> assertMessageContains("[(String)arg1:█████, (String)arg2:Chicken, (String)arg3:█████]"));
    }

    @Test
    void callWithMarker(){
        logbackTestObject.methodWithMarker();
        logs = listAppender.list;
        Assertions.assertFalse(logs.isEmpty(), "methodWithMarker: logs should not be empty");
        Assertions.assertTrue(logs.stream().anyMatch(log -> log.getMarkerList().stream().anyMatch(c -> c.contains("slf4j"))));
    }

    @Test
    void callWithMarkerNoConsole(){
        var rootLogger = (Logger) LoggerFactory.getLogger("ROOT");

        var console = findInAttachable(rootLogger, "CONSOLE");
        Assertions.assertNotNull(console);

        logbackTestObject.methodWithMarkerNoConsole("Wombat", 13);
        logs = listAppender.list;

        Assertions.assertTrue(logs.stream().anyMatch(log -> log.getMarkerList().stream().anyMatch(c -> c.contains("NO_CONSOLE"))));

        var consoleFiltersList = console.getCopyOfAttachedFiltersList();

        var mdcFilter = consoleFiltersList.stream().filter(filter -> filter instanceof LogbackMarkerFilter).findFirst();

        assertTrue(mdcFilter.isPresent());
    }


}
