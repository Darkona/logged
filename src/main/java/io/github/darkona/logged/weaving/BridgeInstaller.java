package io.github.darkona.logged.weaving;

import io.github.darkona.logged.internals.LoggedEngine;
import org.springframework.beans.factory.DisposableBean;

public class BridgeInstaller implements DisposableBean {

    private final LoggedEngine engine;

    public BridgeInstaller(LoggedEngine loggedEngine) {
        this.engine = loggedEngine;
        LoggedBridge.install(loggedEngine);
    }

    @Override
    public void destroy() {
        LoggedBridge.uninstall(engine);
    }
}
