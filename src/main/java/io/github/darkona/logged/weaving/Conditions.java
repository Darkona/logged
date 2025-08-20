package io.github.darkona.logged.weaving;

import jakarta.annotation.Nonnull;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.lang.management.ManagementFactory;

public class Conditions {

    public static boolean weavingEnabled(ConditionContext context, AnnotatedTypeMetadata metadata) {
        var args = ManagementFactory.getRuntimeMXBean().getInputArguments();

        boolean hasWeaving = args.stream()
                                 .anyMatch(a -> a.startsWith("-javaagent:") &&
                                         (a.contains("aspectjweaver") || a.contains("spring-instrument"))
                                 );
        if (!hasWeaving) return false;

        var xml = (context.getResourceLoader().getResource("classpath:META-INF/aop.xml"));
        if (xml.exists() && xml.isFile()) {
            return XmlStringFinder.check(xml, WeavedAspect.class.getName());
        }
        return false;
    }

    public static class OnAspectJWeaving implements Condition {
        @Override
        public boolean matches(@Nonnull ConditionContext context, @Nonnull AnnotatedTypeMetadata metadata) {
            return weavingEnabled(context, metadata);
        }
    }

    public static class OnNoAspectJWeaving implements Condition {
        @Override
        public boolean matches(@Nonnull ConditionContext context, @Nonnull AnnotatedTypeMetadata metadata) {
            return !weavingEnabled(context, metadata);
        }
    }
}
