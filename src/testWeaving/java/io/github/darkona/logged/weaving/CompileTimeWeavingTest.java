package io.github.darkona.logged.weaving;

import io.github.darkona.logged.internals.LoggedAspect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guards the compile-time weaving contract. This source set is woven by ajc with the
 * allow-list from META-INF/logged/logged-weaving.xml, which is how a consumer wires it.
 *
 * <p>Each failure here maps to a way CTW broke before: no aspectOf() factory, both aspects
 * woven at once, or Spring registering its proxy next to the woven aspect.</p>
 */
@SpringBootTest(classes = WeavingTestConfig.class)
@DisplayName("Tejido en tiempo de compilación (CTW)")
class CompileTimeWeavingTest {

    private static final String PUBLIC_METHOD = "publicMethod";
    private static final String PRIVATE_METHOD = "privateMethod";
    private static final String STATIC_METHOD = "staticMethod";
    private static final String FINAL_METHOD = "finalMethod";
    private static final String SELF_INVOKED = "selfInvoked";

    @Autowired
    WeavingProbeService service;

    @Autowired
    CountingPlugin counter;

    @Autowired
    ApplicationContext context;

    @BeforeEach
    void resetCounters() {
        counter.reset();
    }

    @Test
    @DisplayName("Registra el puente y no el aspecto de Spring AOP")
    void registersOnlyTheBridge() {
        assertThat(context.getBeansOfType(BridgeInstaller.class))
                .as("el instalador del puente debe existir en modo weaving")
                .hasSize(1);
        assertThat(context.getBeansOfType(LoggedAspect.class))
                .as("los dos modos son excluyentes: el aspecto de Spring no debe registrarse")
                .isEmpty();
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {PUBLIC_METHOD, PRIVATE_METHOD, STATIC_METHOD, FINAL_METHOD, SELF_INVOKED})
    @DisplayName("Instrumenta exactamente una vez cada forma de método")
    void instrumentsEachMethodShapeExactlyOnce(String method) {
        invoke(method);

        assertThat(counter.calls(method)).as("llamadas registradas para %s", method).isEqualTo(1);
        assertThat(counter.returns(method)).as("retornos registrados para %s", method).isEqualTo(1);
    }

    private void invoke(String method) {
        switch (method) {
            case PUBLIC_METHOD -> service.publicMethod();
            case PRIVATE_METHOD -> service.callPrivate();
            case STATIC_METHOD -> WeavingProbeService.staticMethod();
            case FINAL_METHOD -> service.finalMethod();
            case SELF_INVOKED -> service.selfInvoker();
            default -> throw new IllegalArgumentException(method);
        }
    }
}
