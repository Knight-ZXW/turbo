package com.knightboost.turbo.demo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class TestThread extends Thread{

    public TestThread(@Nullable ThreadGroup group, @Nullable Runnable target, @NonNull String name) {
        super(group, target, name, - (512* 1024));
    }
}
