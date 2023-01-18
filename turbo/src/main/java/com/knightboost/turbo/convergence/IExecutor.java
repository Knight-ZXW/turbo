package com.knightboost.turbo.convergence;

import androidx.annotation.NonNull;

public interface IExecutor {
    void execute(@NonNull Runnable runnable, int i);
}
