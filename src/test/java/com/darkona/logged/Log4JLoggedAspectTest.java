package com.darkona.logged;

import com.darkona.toolset.objects.TestObject;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class Log4JLoggedAspectTest {

//
//    private LoggedAspect unit;
//    private ProceedingJoinPoint point;
//    private Logged logged;
//    private MethodSignature methodSignature;
//
//
//    @BeforeEach
//    public void setup() {
//        unit = new LoggedAspect();
//        point = mock(ProceedingJoinPoint.class);
//        logged = mock(Logged.class);
//        methodSignature = mock(MethodSignature.class);
//
//
//        when(logged.args()).thenReturn(true);
//        when(logged.argValues()).thenReturn(Logged.Values.ALL);
//        when(logged.onCall()).thenReturn(true);
//        when(logged.onException()).thenReturn(true);
//        when(logged.onReturn()).thenReturn(true);
//        when(logged.returnValue()).thenReturn(Logged.Values.ALL);
//        when(logged.time()).thenReturn(true);
//        when(logged.level()).thenReturn("INFO");
//        when(logged.callMsg()).thenReturn("");
//        when(logged.returnMsg()).thenReturn("");
//        when(logged.exceptionMsg()).thenReturn("");
//
//    }
//
//    @Test
//    void testLoggingPrintsMethodLogs() throws Throwable{
//
//        var methodName = "doSomething";
//
//
//
//
//        Logger logger = LoggerFactory.getLogger(this.getClass());
//
//
//        when(methodSignature.getName()).thenReturn(methodName);
//        when(methodSignature.getMethod()).thenReturn(TestObject.class.getMethod(methodName, String.class, String.class, String.class));
//        when(methodSignature.getParameterNames()).thenReturn(new String[]{"a", "b", "c"});
//        when(methodSignature.getParameterTypes()).thenReturn(new Class<?>[]{String.class, String.class, String.class});
//        when(methodSignature.getExceptionTypes()).thenReturn(new Class<?>[]{IllegalArgumentException.class});
//
//        when(methodSignature.getDeclaringType()).thenReturn(this.getClass());
//
//
//        when (point.getArgs()).thenReturn(new Object[]{"Hello ", "World", "!"});
//
//        when(point.getSignature()).thenReturn(methodSignature);
//
//        unit.logMethod(point, logged);
//
////        assertTrue(loggedAppender.list.stream().anyMatch(event -> event.getFormattedMessage().contains(methodName)));
////        assertTrue(loggedAppender.list.stream().anyMatch(event -> event.getLevel() == Level.INFO));
////        assertTrue(loggedAppender.list.stream().anyMatch(event -> !event.getMessage().isEmpty()));
////
////
////        loggedAppender.stop();
////        logger.detachAppender(loggedAppender);
//    }
//
//
//    @Test
//    void shouldLogExceptionsIfTheyAreRaised()
//    throws Throwable {
//
//        var methodName = "doSomething";
//
////        ListAppender<ILoggingEvent> loggedAppender = new ListAppender<>();
////        Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());
////        loggedAppender.start();
////        logger.addAppender(loggedAppender);
//
//        when(methodSignature.getName()).thenReturn(methodName);
//        when(methodSignature.getMethod()).thenReturn(TestObject.class.getMethod(methodName, String.class, String.class, String.class));
//        when(methodSignature.getParameterNames()).thenReturn(new String[]{"a", "b", "c"});
//        when(methodSignature.getParameterTypes()).thenReturn(new Class<?>[]{String.class, String.class, String.class});
//        when(methodSignature.getExceptionTypes()).thenReturn(new Class<?>[]{IllegalArgumentException.class});
//
//        when(methodSignature.getDeclaringType()).thenReturn(this.getClass());
//
//
//        when (point.getArgs()).thenReturn(new Object[]{"Hello ", "World", "!"});
//
//        when(point.getSignature()).thenReturn(methodSignature);
//
//        doThrow(new NullPointerException()).when(point).proceed();
//
//
//
//
//        assertThrows(NullPointerException.class, () -> unit.logMethod(point, logged));
//
////        assertTrue(loggedAppender.list.stream().anyMatch(event -> event.getFormattedMessage().contains(methodName)));
////        assertTrue(loggedAppender.list.stream().anyMatch(event -> event.getLevel() == Level.ERROR));
////
////        loggedAppender.stop();
////        logger.detachAppender(loggedAppender);
//
//    }
//
//
//    @Test
//    void shouldLogReturnValuesIfTheyAreReturned(){
//
//
//
//    }
//
//    @Test
//    void shouldLogTimeTakenIfTimeIsEnabled(){
//
//    }
//
//    @Test
//    void shouldLogArgumentsIfEnabled(){
//
//    }
//
//    @Test
//    void shouldLogCustomMessageIfEnabled(){}



}