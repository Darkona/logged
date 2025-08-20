package io.github.darkona.logged.weaving;

import io.github.darkona.logged.internals.LoggedEngine;

public class BridgeInstaller {
    public BridgeInstaller(LoggedEngine loggedEngine) {
        LoggedBridge.install(loggedEngine);
    }
}
