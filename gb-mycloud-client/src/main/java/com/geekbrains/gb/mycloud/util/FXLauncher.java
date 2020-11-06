package com.geekbrains.gb.mycloud.util;

import javafx.application.Platform;

public class FXLauncher {
    public static void fxThreadRun(Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            Platform.runLater(() -> {
                runnable.run();
            });
        }
    }
}
