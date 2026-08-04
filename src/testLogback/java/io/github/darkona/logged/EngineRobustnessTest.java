package io.github.darkona.logged;

import io.github.darkona.logged.api.Data;
import io.github.darkona.logged.api.LoggedPlugin;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Regression tests for engine robustness:
 * - a plugin that throws in every callback must never affect the business call
 * - @Logged on the implementation method must be found through an interface proxy
 * - the OTel plugin must restore the outer span_name in MDC after a nested call
 */
@SpringBootTest(classes = {LogbackBootConfig.class, EngineRobustnessTest.RobustnessConfig.class})
@ActiveProfiles("logback")
class EngineRobustnessTest {

    @Autowired
    private GreetingService greetingService;

    @Autowired
    private ExplodingService explodingService;

    @Autowired
    private OuterService outerService;

    @Test
    void businessResultSurvivesThrowingPlugin() {
        assertEquals("hello world", greetingService.greet("world"));
    }

    @Test
    void businessExceptionSurvivesThrowingPlugin() {
        var ex = assertThrows(IllegalStateException.class, () -> explodingService.boom());
        assertEquals("business boom", ex.getMessage());
    }

    @Test
    void mdcSpanNameRestoredAfterNestedCall() {
        assertEquals("Logged: OuterService#outer()", outerService.outer());
    }

    interface GreetingService {
        String greet(String name);
    }

    static class GreetingServiceImpl implements GreetingService {
        @Logged
        @Override
        public String greet(String name) {
            return "hello " + name;
        }
    }

    static class ExplodingService {
        @Logged
        public void boom() {
            throw new IllegalStateException("business boom");
        }
    }

    static class OuterService {
        private final InnerService inner;

        OuterService(InnerService inner) {
            this.inner = inner;
        }

        @Logged
        public String outer() {
            inner.inner();
            // After the nested @Logged call, MDC must hold the OUTER span name again
            return MDC.get("span_name");
        }
    }

    static class InnerService {
        @Logged
        public String inner() {
            return "x";
        }
    }

    static class ThrowingPlugin implements LoggedPlugin {
        @Override
        public void onCall(ProceedingJoinPoint pjp, Data data, Logged options) {
            throw new RuntimeException("plugin failure in onCall");
        }

        @Override
        public void onReturn(ProceedingJoinPoint pjp, Data data, Logged options) {
            throw new RuntimeException("plugin failure in onReturn");
        }

        @Override
        public void onException(ProceedingJoinPoint pjp, Data data, Logged options, Throwable exception) {
            throw new RuntimeException("plugin failure in onException");
        }

        @Override
        public String announceLoad() {
            return "";
        }

        @Override
        public void onLoad() {
            throw new RuntimeException("plugin failure in onLoad");
        }

        @Override
        public void afterMethod() {
            throw new RuntimeException("plugin failure in afterMethod");
        }
    }

    @Configuration
    static class RobustnessConfig {

        @Bean
        GreetingService greetingService() {
            return new GreetingServiceImpl();
        }

        @Bean
        ExplodingService explodingService() {
            return new ExplodingService();
        }

        @Bean
        InnerService innerService() {
            return new InnerService();
        }

        @Bean
        OuterService outerService(InnerService innerService) {
            return new OuterService(innerService);
        }

        @Bean
        LoggedPlugin throwingPlugin() {
            return new ThrowingPlugin();
        }
    }
}
