package io.github.darkona.logged.weaving;

import jakarta.annotation.Nonnull;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.lang.management.ManagementFactory;

public class Conditions {

    /**
     * Read straight from the Environment, not from LoggedProperties: conditions are evaluated
     * before any bean exists. LoggedProperties declares the same key so it reaches the IDE and
     * the configuration metadata.
     */
    static final String WEAVING_MODE_PROPERTY = "logged.weaving";

    public static boolean weavingEnabled(ConditionContext context) {
        var mode = context.getEnvironment().getProperty(WEAVING_MODE_PROPERTY, WeavingMode.class, WeavingMode.AUTO);
        return switch (mode) {
            case ENABLED -> true;
            case DISABLED -> false;
            case AUTO -> agentWeavingDetected(context);
        };
    }

    /**
     * Load-time weaving leaves two traces: the agent on the command line and an aop.xml naming
     * the aspect. Compile-time weaving leaves neither, which is why it needs
     * {@link WeavingMode#ENABLED}.
     */
    private static boolean agentWeavingDetected(ConditionContext context) {
        var args = ManagementFactory.getRuntimeMXBean().getInputArguments();

        boolean hasWeaving = args.stream()
                                 .anyMatch(a -> a.startsWith("-javaagent:") &&
                                         (a.contains("aspectjweaver") || a.contains("spring-instrument"))
                                 );
        if (!hasWeaving) return false;

        // isFile() is false for resources inside a jar, so only exists() can be checked here
        var xml = (context.getResourceLoader().getResource("classpath:META-INF/aop.xml"));
        if (xml.exists()) {
            return XmlStringFinder.check(xml, WeavedAspect.class.getName());
        }
        return false;
    }

    public static class OnAspectJWeaving implements Condition {
        @Override
        public boolean matches(@Nonnull ConditionContext context, @Nonnull AnnotatedTypeMetadata metadata) {
            return weavingEnabled(context);
        }
    }

    public static class OnNoAspectJWeaving implements Condition {
        @Override
        public boolean matches(@Nonnull ConditionContext context, @Nonnull AnnotatedTypeMetadata metadata) {
            return !weavingEnabled(context);
        }
    }
}
