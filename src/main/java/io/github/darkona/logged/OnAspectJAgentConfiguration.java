package io.github.darkona.logged;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class OnAspectJAgentConfiguration implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        var args = java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments();
        boolean hasAjWeaver = args.stream().anyMatch(a -> a.startsWith("-javaagent:") && a.contains("aspectjweaver"));
        boolean hasSpringInstr = args.stream().anyMatch(a -> a.startsWith("-javaagent:") && a.contains("spring-instrument"));
        boolean hasAopXml = context.getResourceLoader().getResource("classpath:META-INF/aop.xml").exists();
        return !(hasAjWeaver || hasSpringInstr || hasAopXml);
    }
}
