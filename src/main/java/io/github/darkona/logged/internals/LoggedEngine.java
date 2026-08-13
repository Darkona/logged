package io.github.darkona.logged.internals;


import io.github.darkona.logged.Logged;
import io.github.darkona.logged.LoggedProperties;
import io.github.darkona.logged.api.Data;
import io.github.darkona.logged.api.LogDecorator;
import io.github.darkona.logged.api.LoggedPlugin;
import io.github.darkona.logged.colors.Orange;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import lombok.Setter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotationUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Orchestrates the around-advice flow: resolves {@code @Logged} options,
 * maintains the per-thread call stack, and coordinates its collaborators —
 * {@link TokenAssembler} builds the token data, {@link MaskingPolicy} decides
 * what gets masked, and {@link PluginDispatcher} isolates plugin callbacks.
 */
public class LoggedEngine {

    public static final String NULL = "null";
    private static final Logger log = LoggerFactory.getLogger(LoggedEngine.class);
    private static final ThreadLocal<Deque<Data>> STACK = ThreadLocal.withInitial(ArrayDeque::new);
    private final LoggedProperties props;
    private final LogDecorator deco;
    private final MaskingPolicy masking;
    private final TokenAssembler assembler;
    private final PluginDispatcher dispatcher;
    @Setter
    private boolean woven;


    public LoggedEngine(LoggedProperties props, LogDecorator deco, List<LoggedPlugin> plugins) {
        this.props = props;
        this.deco = deco;
        this.masking = new MaskingPolicy(props);
        this.assembler = new TokenAssembler(props, masking);
        this.dispatcher = new PluginDispatcher(plugins);
    }

    private static void pop() {
        Deque<Data> s = STACK.get();
        if (!s.isEmpty()) s.pop();
        if (s.isEmpty()) STACK.remove();
    }

    @PostConstruct
    void init() {
        var msg = "@Logged engine initialized";
        if (props.isUseUtf8() && System.out.charset() != StandardCharsets.UTF_8) {
            Utf8Installer.install();
            msg += ", output UTF-8 enabled";
        }

        msg += woven ? ", and aspect weaving detected." : ".";

        log.info(deco.custom(Orange.DARK_ORANGE, msg));

        dispatcher.loadAll(props.isAnnounceLoad());

        masking.init();
    }

    public Object logMethod(ProceedingJoinPoint pjp)
    throws Throwable {
        final var options = getLoggedOptions(pjp);
        if (options == null) return pjp.proceed();

        final Data data;
        try {
            data = assembler.assembleCallData(pjp, options, STACK.get().size());
        } catch (Throwable t) {
            log.warn("@Logged could not assemble call data for {}: {}", pjp.getSignature(), t.toString());
            return pjp.proceed();
        }

        // Everything besides pjp.proceed() is isolated: a broken plugin must never
        // prevent the business method from running, replace its result, or swallow
        // its exception.
        STACK.get().push(data);
        try {
            dispatcher.dispatch("onCall", p -> p.onCall(pjp, data, options));
            final var result = pjp.proceed();
            afterSuccess(pjp, data, options, result);
            return result;
        } catch (Throwable ex) {
            afterFailure(pjp, data, options, ex);
            throw ex;
        } finally {
            pop();
            dispatcher.dispatch("afterMethod", LoggedPlugin::afterMethod);
        }
    }

    private void afterSuccess(ProceedingJoinPoint pjp, Data data, Logged options, Object result) {
        try {
            assembler.putDuration(data);
            assembler.assembleReturnData(data, result, options);
        } catch (Throwable t) {
            log.warn("@Logged could not assemble return data for {}: {}", pjp.getSignature(), t.toString());
        }
        dispatcher.dispatch("onReturn", p -> p.onReturn(pjp, data, options));
    }

    private void afterFailure(ProceedingJoinPoint pjp, Data data, Logged options, Throwable ex) {
        try {
            assembler.putDuration(data);
            assembler.assembleExceptionData(ex, data);
        } catch (Throwable t) {
            log.warn("@Logged could not assemble exception data for {}: {}", pjp.getSignature(), t.toString());
        }
        dispatcher.dispatch("onException", p -> p.onException(pjp, data, options, ex));
    }

    @Nullable
    private Logged getLoggedOptions(ProceedingJoinPoint pjp) {
        var method = ((MethodSignature) pjp.getSignature()).getMethod();
        // With interface-based proxies the signature method is the interface method,
        // which lacks the annotation; resolve the implementation method first.
        // getTarget() is null for static methods under load-time weaving.
        var target = pjp.getTarget();
        var targetClass = target != null ? AopUtils.getTargetClass(target) : method.getDeclaringClass();
        var specificMethod = AopUtils.getMostSpecificMethod(method, targetClass);

        var onMethod = AnnotationUtils.findAnnotation(specificMethod, Logged.class);
        return onMethod != null ? onMethod : AnnotationUtils.findAnnotation(targetClass, Logged.class);
    }

}
