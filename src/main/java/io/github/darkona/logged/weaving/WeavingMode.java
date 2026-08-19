package io.github.darkona.logged.weaving;

/**
 * How Logged decides between the Spring AOP aspect and the woven aspect.
 *
 * <p>Set through the {@code logged.weaving} property. The two modes are mutually exclusive:
 * exactly one of {@code LoggedAspect} and {@code BridgeInstaller} is registered, so an annotated
 * method is instrumented once.</p>
 */
public enum WeavingMode {

    /**
     * Detect load-time weaving: a weaving agent on the JVM command line plus an {@code aop.xml}
     * naming {@link WeavedAspect}. This is the default and the historical behaviour.
     *
     * <p>Compile-time weaving cannot be detected this way, because it leaves no agent behind.
     * Use {@link #ENABLED} for CTW.</p>
     */
    AUTO,

    /**
     * Always use the woven aspect. Required for compile-time weaving, where no agent exists for
     * {@link #AUTO} to find.
     */
    ENABLED,

    /**
     * Always use the Spring AOP aspect, whatever the JVM arguments say. Escape hatch for a JVM
     * that carries a weaving agent for some other library.
     */
    DISABLED
}
