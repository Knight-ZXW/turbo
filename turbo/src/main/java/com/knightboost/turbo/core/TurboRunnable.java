package com.knightboost.turbo.core;

import android.os.Process;

public class TurboRunnable implements Comparable<TurboRunnable>, Runnable {

    public final Runnable actualRunnable;
    public final TurboThreadPoolProxy litThreadPool;
    public final String runnableName;

    public TurboRunnable(Runnable runnable, TurboThreadPoolProxy turboThreadPoolProxy) {
        this.actualRunnable = runnable;
        this.litThreadPool = turboThreadPoolProxy;
        this.runnableName = turboThreadPoolProxy.getRunnableName(runnable);
    }

    private void afterExecute() {
        this.litThreadPool.afterExecute(this);
    }

    public static TurboRunnable wrapEmptyRunnable(TurboThreadPoolProxy turboThreadPoolProxy) {
        return new TurboRunnable(new Runnable() { // from class: com.ss.android.ugc.bytex.pthread.base.core.TurboRunnable.1
            @Override // java.lang.Runnable
            public final void run() {
            }
        }, turboThreadPoolProxy);
    }

    @Override
    public void run() {
        Thread currentThread = Thread.currentThread();
        String name = currentThread.getName();
        new StringBuilder();
        StringBuilder sb = new StringBuilder();
        String newName = sb.append(name).append("::").append(this.runnableName).toString();
        currentThread.setName(newName);

        Thread currentThread2 = Thread.currentThread();
        int threadPriority = Process.getThreadPriority(Process.myTid());
        int priority = currentThread2.getPriority();
        this.actualRunnable.run();
        currentThread.setName(name);
        Process.setThreadPriority(threadPriority);
        currentThread2.setPriority(priority);
        afterExecute();
    }

    public Runnable getActualRunnable() {
        return this.actualRunnable;
    }

    @Override
    public int compareTo(TurboRunnable turboRunnable) {
        Class<?> cls = this.actualRunnable.getClass();
        Class<?> cls2 = turboRunnable.actualRunnable.getClass();
        if (cls.isAssignableFrom(cls2) || cls2.isAssignableFrom(cls)) {
            Runnable runnable = this.actualRunnable;
            if (runnable instanceof Comparable) {
                Runnable runnable2 = turboRunnable.actualRunnable;
                if (runnable2 instanceof Comparable) {
                    return ((Comparable) runnable).compareTo(runnable2);
                }
            }
        }
        return 0;
    }
}
