package io.github.darkona.logged.internals;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.CodeSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;

import java.lang.reflect.Method;

class ParameterNames {

    private static final ParameterNameDiscoverer NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

    private ParameterNames() {}

    public static String[] resolve(ProceedingJoinPoint pjp) {
        Signature sig = pjp.getSignature();
        if (sig instanceof CodeSignature cs) {
            String[] names = cs.getParameterNames();
            if (names != null && names.length == pjp.getArgs().length) {
                return names;
            }
        }

        Method method = AspectUtils.toMethod(pjp);
        if (method != null) {
            String[] discovered = NAME_DISCOVERER.getParameterNames(method);
            if (discovered != null && discovered.length == pjp.getArgs().length) {
                return discovered;
            }
        }

        int n = pjp.getArgs() == null ? 0 : pjp.getArgs().length;
        String[] positional = new String[n];
        for (int i = 0; i < n; i++) positional[i] = "arg" + i;
        return positional;
    }

    static final class AspectUtils {
        static Method toMethod(ProceedingJoinPoint pjp) {
            try {
                String methodName = pjp.getSignature().getName();
                Class<?> targetClass = pjp.getTarget().getClass();
                Class<?>[] paramTypes = ((CodeSignature) pjp.getSignature()).getParameterTypes();
                return targetClass.getMethod(methodName, paramTypes);
            } catch (Throwable ignored) {
                return null;
            }
        }
    }
}
