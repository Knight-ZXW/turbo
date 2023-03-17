package com.knightboost.turbo.core;

import com.knightboost.turbo.IThreadPoolProxy;
import com.knightboost.turbo.MonitorImpl;
import com.knightboost.turbo.Turbo;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class TurboThreadPoolProxy implements IThreadPoolProxy, TurboThreadCallback {

    public static final RuntimePermission shutdownPerm = new RuntimePermission("modifyThread");
    public boolean allowCoreThreadTimeOut;
    public final TurboCoreThreadPool bigThreadPool;
    public volatile int corePoolSize;
    public final AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0));
    private static final int COUNT_BITS = Integer.SIZE - 3;
    private static final int CAPACITY = (1 << COUNT_BITS) - 1;

    // runState is stored in the high-order bits
    private static final int RUNNING = -1 << COUNT_BITS;
    private static final int SHUTDOWN = 0 << COUNT_BITS;
    private static final int STOP = 1 << COUNT_BITS;
    private static final int TIDYING = 2 << COUNT_BITS;
    private static final int TERMINATED = 3 << COUNT_BITS;

    // Packing and unpacking ctl
    private static int runStateOf(int c) {
        return c & ~CAPACITY;
    }

    private static int workerCountOf(int c) {
        return c & CAPACITY;
    }

    private static int ctlOf(int rs, int wc) {
        return rs | wc;
    }

    /*
     * Bit field accessors that don't require unpacking ctl.
     * These depend on the bit layout and on workerCount being never negative.
     */

    private static boolean runStateLessThan(int c, int s) {
        return c < s;
    }

    private static boolean runStateAtLeast(int c, int s) {
        return c >= s;
    }

    private static boolean isRunning(int c) {
        return c < SHUTDOWN;
    }

    /**
     * Attempts to CAS-increment the workerCount field of ctl.
     */
    private boolean compareAndIncrementWorkerCount(int expect) {
        return ctl.compareAndSet(expect, expect + 1);
    }

    /**
     * Attempts to CAS-decrement the workerCount field of ctl.
     */
    private boolean compareAndDecrementWorkerCount(int expect) {
        return ctl.compareAndSet(expect, expect - 1);
    }

    /**
     * Decrements the workerCount field of ctl. This is called only on
     * abrupt termination of a thread (see processWorkerExit). Other
     * decrements are performed within getTask.
     */
    private void decrementWorkerCount() {
        do {
        } while (!compareAndDecrementWorkerCount(ctl.get()));
    }

    public volatile RejectedExecutionHandler handler;
    public final ThreadPoolExecutor holder;
    public volatile long keepAliveTime;
    public int largestPoolSize;
    public final ReentrantLock mainLock;
    public volatile int maximumPoolSize;
    public final Condition termination;
    public volatile ThreadFactory threadFactory;
    public volatile String threadName;
    public final BlockingQueue<Runnable> workQueue;

    public TurboThreadPoolProxy(int corePoolSIze, int maximumPoolSize, long keepAliveTime, TimeUnit timeUnit, BlockingQueue<Runnable> blockingQueue, ThreadFactory threadFactory, RejectedExecutionHandler rejectedExecutionHandler, ThreadPoolExecutor threadPoolExecutor) {
        this.mainLock = new ReentrantLock();
        this.termination = mainLock.newCondition();
        if (corePoolSIze < 0 || maximumPoolSize <= 0 || maximumPoolSize < corePoolSIze || keepAliveTime < 0) {
            throw new IllegalArgumentException();
        }

        if (blockingQueue == null) {
            throw new IllegalArgumentException("blockingQueue can't be null");
        }
        if (threadFactory == null) {
            throw new IllegalArgumentException("threadFactory can't be null");
        }
        if (rejectedExecutionHandler == null) {
            throw new IllegalArgumentException("rejectedExecutionHandler can't be null");
        }
        this.corePoolSize = corePoolSIze;
        this.maximumPoolSize = maximumPoolSize;
        this.workQueue = blockingQueue;
        this.keepAliveTime = timeUnit.toNanos(keepAliveTime);
        this.threadFactory = threadFactory;
        this.handler = rejectedExecutionHandler;

        this.bigThreadPool = Turbo.INSTANCE.getCoreThreadPool();
        this.holder = threadPoolExecutor;
        Turbo.INSTANCE.getMonitor().onThreadPoolCreate(corePoolSIze, maximumPoolSize, keepAliveTime, timeUnit, blockingQueue, threadFactory, rejectedExecutionHandler);

    }

    /**
     * Checks if a new worker can be added with respect to current
     * pool state and the given bound (either core or maximum). If so,
     * the worker count is adjusted accordingly, and, if possible, a
     * new worker is created and started, running firstTask as its
     * first task. This method returns false if the pool is stopped or
     * eligible to shut down. It also returns false if the thread
     * factory fails to create a thread when asked.  If the thread
     * creation fails, either due to the thread factory returning
     * null, or due to an exception (typically OutOfMemoryError in
     * Thread.start()), we roll back cleanly.
     *
     * @param firstTask the task the new thread should run first (or
     *                  null if none). Workers are created with an initial first task
     *                  (in method execute()) to bypass queuing when there are fewer
     *                  than corePoolSize threads (in which case we always start one),
     *                  or when the queue is full (in which case we must bypass queue).
     *                  Initially idle threads are usually created via
     *                  prestartCoreThread or to replace other dying workers.
     * @param core      if true use corePoolSize as bound, else
     *                  maximumPoolSize. (A boolean indicator is used here rather than a
     *                  value to ensure reads of fresh values after checking other pool
     *                  state).
     * @return true if successful
     */
    private boolean addWorker(Runnable firstTask, boolean core) {
        retry:
        for (; ; ) {
            int c = ctl.get();
            int rs = runStateOf(c);

            // Check if queue empty only if necessary.
            if (rs >= SHUTDOWN && !(rs == SHUTDOWN && firstTask == null && !workQueue.isEmpty())) return false;

            for (; ; ) {
                int wc = workerCountOf(c);
                if (wc >= CAPACITY || wc >= (core ? corePoolSize : maximumPoolSize)) return false;
                if (compareAndIncrementWorkerCount(c)) break retry;
                c = ctl.get();  // Re-read ctl
                if (runStateOf(c) != rs) continue retry;
                // else CAS failed due to workerCount change; retry inner loop
            }
        }
        //
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            boolean workerStarted = false;
            int rs = runStateOf(ctl.get());
            if ((rs < SHUTDOWN || (rs == SHUTDOWN && firstTask == null))
                    && this.bigThreadPool.execute(firstTask, this, core)) {
                workerStarted = true;
                int workCount = workerCountOf(ctl.get());
                if (workCount > largestPoolSize) {
                    this.largestPoolSize = workCount;
                }
            }

            if (!workerStarted) {
                addWorkerFailed();
            }
            return workerStarted;
        } finally {
            mainLock.unlock();
        }

    }

    /**
     * Rolls back the worker thread creation.
     * - removes worker from workers, if present
     * - decrements worker count
     * - rechecks for termination, in case the existence of this
     * worker was holding up termination
     */
    private void addWorkerFailed() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            decrementWorkerCount();
            tryTerminate();
        } finally {
            mainLock.unlock();
        }

    }

    /*
     * Methods for setting control state
     */

    /**
     * Transitions runState to given target, or leaves it alone if
     * already at least the given target.
     *
     * @param targetState the desired state, either SHUTDOWN or STOP
     *                    (but not TIDYING or TERMINATED -- use tryTerminate for that)
     */
    private void advanceRunState(int targetState) {
        // assert targetState == SHUTDOWN || targetState == STOP;
        for (; ; ) {
            int c = ctl.get();
            if (runStateAtLeast(c, targetState) || ctl.compareAndSet(c, ctlOf(targetState, workerCountOf(c)))) break;
        }
    }

    @Override
    public void allowCoreThreadTimeOut(boolean value) {
        this.allowCoreThreadTimeOut = true;
    }

    @Override
    public boolean allowsCoreThreadTimeOut() {
        return this.allowCoreThreadTimeOut;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            while (!runStateAtLeast(ctl.get(), TERMINATED)) {
                if (nanos <= 0L) return false;
                nanos = termination.awaitNanos(nanos);
            }
            return true;
        } finally {
            mainLock.unlock();
        }
    }

    @Override
    public void execute(Runnable runnable) {
        Turbo turbo = Turbo.INSTANCE;
        if (turbo.getEnableAsyncExecute()) {
            turbo.getSchedulerHandler().post(new Runnable() {
                @Override
                public void run() {
                    TurboThreadPoolProxy.this.doExecute(runnable);
                }
            });
        } else {
            this.doExecute(runnable);
        }
    }

    private void doExecute(Runnable runnable) {
        Objects.requireNonNull(runnable);
        checkThreadName(runnable);
        executeInternal(new TurboRunnable(runnable, this));
    }

    private void executeInternal(TurboRunnable turboRunnable) {
        Turbo turbo = Turbo.INSTANCE;
        TurboThreadMonitor monitor = turbo.getMonitor();

        if (workerCountOf(ctl.get()) < this.corePoolSize) {
            if (addWorker(turboRunnable, true)) {
                monitor.onThreadPoolCallExecute(turboRunnable.getActualRunnable(), 1001);
                return;
            }
        }
        if (workerCountOf(ctl.get()) == 0) {
            if (addWorker(turboRunnable, false)) {
                monitor.onThreadPoolCallExecute(turboRunnable.getActualRunnable(), 1003);
                return;
            }
        }

        int c = ctl.get();
        if (!isRunning(c) || !this.workQueue.offer(turboRunnable)) {
            if (workerCountOf(c) < this.maximumPoolSize && addWorker(turboRunnable, false)) {
                monitor.onThreadPoolCallExecute(turboRunnable.getActualRunnable(), 1003);
                return;
            }
            reject(turboRunnable.actualRunnable);
            monitor.onThreadPoolCallExecute(turboRunnable.getActualRunnable(), 1004);

        } else if (isRunning(ctl.get()) || !remove(turboRunnable)) {
            monitor.onThreadPoolCallExecute(turboRunnable.getActualRunnable(), 1002);
        } else {
            reject(turboRunnable.actualRunnable);
            monitor.onThreadPoolCallExecute(turboRunnable.getActualRunnable(), 1004);
        }

    }

    private void checkThreadName(Runnable runnable) {
        if (runnable == null) {
            runnable = TurboRunnable.wrapEmptyRunnable(this);
        }
        this.threadName = this.threadFactory.newThread(runnable).getName();

    }

    @Override
    protected void finalize() throws Throwable {
        shutdown();
    }

    /**
     * Returns the approximate number of threads that are actively
     * executing tasks.
     *
     * @return the number of threads
     */
    public int getActiveCount() {
        return workerCountOf(ctl.get());
    }

    @Override
    public long getCompletedTaskCount() {
        return 0;
    }

    @Override
    public int getCorePoolSize() {
        return this.corePoolSize;
    }

    /**
     * Returns the thread keep-alive time, which is the amount of time
     * that threads may remain idle before being terminated.
     * Threads that wait this amount of time without processing a
     * task will be terminated if there are more than the core
     * number of threads currently in the pool, or if this pool
     * {@linkplain #allowsCoreThreadTimeOut() allows core thread timeout}.
     *
     * @param unit the desired time unit of the result
     * @return the time limit
     * @see #setKeepAliveTime(long, TimeUnit)
     */
    public long getKeepAliveTime(TimeUnit unit) {
        return unit.convert(keepAliveTime, TimeUnit.NANOSECONDS);
    }

    @Override
    public int getLargestPoolSize() {
        return 0;
    }

    @Override
    public int getMaximumPoolSize() {
        return this.maximumPoolSize;
    }

    /**
     * Returns the current number of threads in the pool.
     *
     * @return the number of threads
     */
    public int getPoolSize() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            // Remove rare and surprising possibility of
            // isTerminated() && getPoolSize() > 0
            return runStateAtLeast(ctl.get(), TIDYING) ? 0 : workerCountOf(ctl.get());
        } finally {
            mainLock.unlock();
        }
    }

    @Override
    public BlockingQueue<Runnable> getQueue() {
        return null;
    }

    @Override
    public RejectedExecutionHandler getRejectedExecutionHandler() {
        return null;
    }

    public String getRunnableName(Runnable runnable) {
        return this.threadFactory.newThread(runnable).getName();
    }

    public Runnable getTaskAfterExecute() {
        Runnable poll = this.workQueue.poll();
        return poll;
    }

    @Override
    public long getTaskCount() {
        return 0;
    }

    @Override
    public ThreadFactory getThreadFactory() {
        return threadFactory;
    }

    public boolean isShutdown() {
        return !isRunning(ctl.get());
    }

    public boolean isTerminated() {
        return runStateAtLeast(ctl.get(), TERMINATED);
    }

    /**
     * Returns true if this executor is in the process of terminating
     * after {@link #shutdown} or {@link #shutdownNow} but has not
     * completely terminated.  This method may be useful for
     * debugging. A return of {@code true} reported a sufficient
     * period after shutdown may indicate that submitted tasks have
     * ignored or suppressed interruption, causing this executor not
     * to properly terminate.
     *
     * @return {@code true} if terminating but not yet terminated
     */
    public boolean isTerminating() {
        int c = ctl.get();
        return !isRunning(c) && runStateLessThan(c, TERMINATED);
    }

    /**
     * Performs any further cleanup following run state transition on
     * invocation of shutdown.  A no-op here, but used by
     * ScheduledThreadPoolExecutor to cancel delayed tasks.
     */
    void onShutdown() {
    }

    @Override
    public int prestartAllCoreThreads() {
        return 0;
    }

    @Override
    public boolean prestartCoreThread() {
        return false;
    }

    public void processAfterExecute(Runnable runnable) {
        decrementWorkerCount();
    }

    /**
     * Tries to remove from the work queue all {@link Future}
     * tasks that have been cancelled. This method can be useful as a
     * storage reclamation operation, that has no other impact on
     * functionality. Cancelled tasks are never executed, but may
     * accumulate in work queues until worker threads can actively
     * remove them. Invoking this method instead tries to remove them now.
     * However, this method may fail to remove tasks in
     * the presence of interference by other threads.
     */
    public void purge() {
        final BlockingQueue<Runnable> q = workQueue;
        try {
            Iterator<Runnable> it = q.iterator();
            while (it.hasNext()) {
                Runnable r = it.next();
                if (r instanceof Future<?> && ((Future<?>) r).isCancelled()) it.remove();
            }
        } catch (ConcurrentModificationException fallThrough) {
            // Take slow path if we encounter interference during traversal.
            // Make copy for traversal and call remove for cancelled entries.
            // The slow path is more likely to be O(N*N).
            for (Object r : q.toArray())
                if (r instanceof Future<?> && ((Future<?>) r).isCancelled()) q.remove(r);
        }

        tryTerminate(); // In case SHUTDOWN and now empty
    }

    /**
     * Invokes the rejected execution handler for the given command.
     * Package-protected for use by ScheduledThreadPoolExecutor.
     */
    final void reject(Runnable command) {
        handler.rejectedExecution(command, this.holder);
    }

    /**
     * Removes this task from the executor's internal queue if it is
     * present, thus causing it not to be run if it has not already
     * started.
     *
     * <p>This method may be useful as one part of a cancellation
     * scheme.  It may fail to remove tasks that have been converted
     * into other forms before being placed on the internal queue.
     * For example, a task entered using {@code submit} might be
     * converted into a form that maintains {@code Future} status.
     * However, in such cases, method {@link #purge} may be used to
     * remove those Futures that have been cancelled.
     *
     * @param task the task to remove
     * @return {@code true} if the task was removed
     */
    public boolean remove(Runnable task) {
        boolean removed = workQueue.remove(task);
        tryTerminate(); // In case SHUTDOWN and now empty
        return removed;
    }

    @Override
    public void setCorePoolSize(int i) {
    }

    /**
     * Sets the thread keep-alive time, which is the amount of time
     * that threads may remain idle before being terminated.
     * Threads that wait this amount of time without processing a
     * task will be terminated if there are more than the core
     * number of threads currently in the pool, or if this pool
     * {@linkplain #allowsCoreThreadTimeOut() allows core thread timeout}.
     * This overrides any value set in the constructor.
     *
     * @param time the time to wait.  A time value of zero will cause
     *             excess threads to terminate immediately after executing tasks.
     * @param unit the time unit of the {@code time} argument
     * @throws IllegalArgumentException if {@code time} less than zero or
     *                                  if {@code time} is zero and {@code allowsCoreThreadTimeOut}
     * @see #getKeepAliveTime(TimeUnit)
     */
    public void setKeepAliveTime(long time, TimeUnit unit) {
        if (time < 0) throw new IllegalArgumentException();
        if (time == 0 && allowsCoreThreadTimeOut()) throw new IllegalArgumentException("Core threads must have nonzero keep alive times");
        long keepAliveTime = unit.toNanos(time);
        long delta = keepAliveTime - this.keepAliveTime;
        this.keepAliveTime = keepAliveTime;
        if (delta < 0) interruptIdleWorkers();
    }

    /**
     * Sets the maximum allowed number of threads. This overrides any
     * value set in the constructor. If the new value is smaller than
     * the current value, excess existing threads will be
     * terminated when they next become idle.
     *
     * @param maximumPoolSize the new maximum
     * @throws IllegalArgumentException if the new maximum is
     *                                  less than or equal to zero, or
     *                                  less than the {@linkplain #getCorePoolSize core pool size}
     * @see #getMaximumPoolSize
     */
    public void setMaximumPoolSize(int maximumPoolSize) {
        if (maximumPoolSize <= 0 || maximumPoolSize < corePoolSize) throw new IllegalArgumentException();
        this.maximumPoolSize = maximumPoolSize;
        if (workerCountOf(ctl.get()) > maximumPoolSize) interruptIdleWorkers();
    }

    @Override
    public void setRejectedExecutionHandler(RejectedExecutionHandler rejectedExecutionHandler) {
        if (handler == null) throw new NullPointerException();
        this.handler = handler;
    }

    /**
     * Sets the thread factory used to create new threads.
     *
     * @param threadFactory the new thread factory
     * @throws NullPointerException if threadFactory is null
     * @see #getThreadFactory
     */
    @Override
    public void setThreadFactory(ThreadFactory threadFactory) {
        if (threadFactory == null) throw new NullPointerException();
        this.threadFactory = threadFactory;
    }

    @Override
    public void shutdown() {

    }

    @Override
    public List<Runnable> shutdownNow() {
        List<Runnable> tasks;
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            advanceRunState(STOP);
            tasks = drainQueue();
        } finally {
            mainLock.unlock();
        }
        tryTerminate();
        return tasks;
    }

    /**
     * Drains the task queue into a new list, normally using
     * drainTo. But if the queue is a DelayQueue or any other kind of
     * queue for which poll or drainTo may fail to remove some
     * elements, it deletes them one by one.
     */
    private List<Runnable> drainQueue() {
        BlockingQueue<Runnable> q = workQueue;
        ArrayList<Runnable> taskList = new ArrayList<>();
        q.drainTo(taskList);
        if (!q.isEmpty()) {
            for (Runnable r : q.toArray(new Runnable[0])) {
                if (q.remove(r)) taskList.add(r);
            }
        }
        return taskList;
    }

    public void terminated() {
    }

    @Override
    public void afterExecute(Runnable runnable) {

    }

    private void interruptIdleWorkers() {
        interruptIdleWorkers(false);
    }

    /**
     * Interrupts threads that might be waiting for tasks (as
     * indicated by not being locked) so they can check for
     * termination or configuration changes. Ignores
     * SecurityExceptions (in which case some threads may remain
     * uninterrupted).
     *
     * @param onlyOne If true, interrupt at most one worker. This is
     *                called only from tryTerminate when termination is otherwise
     *                enabled but there are still other workers.  In this case, at
     *                most one waiting worker is interrupted to propagate shutdown
     *                signals in case all threads are currently waiting.
     *                Interrupting any arbitrary thread ensures that newly arriving
     *                workers since shutdown began will also eventually exit.
     *                To guarantee eventual termination, it suffices to always
     *                interrupt only one idle worker, but shutdown() interrupts all
     *                idle workers so that redundant workers exit promptly, not
     *                waiting for a straggler task to finish.
     */
    private void interruptIdleWorkers(boolean onlyOne) {
    }

    private static final boolean ONLY_ONE = true;

    /**
     * Transitions to TERMINATED state if either (SHUTDOWN and pool
     * and queue empty) or (STOP and pool empty).  If otherwise
     * eligible to terminate but workerCount is nonzero, interrupts an
     * idle worker to ensure that shutdown signals propagate. This
     * method must be called following any action that might make
     * termination possible -- reducing worker count or removing tasks
     * from the queue during shutdown. The method is non-private to
     * allow access from ScheduledThreadPoolExecutor.
     */
    final void tryTerminate() {
        for (; ; ) {
            int c = ctl.get();
            if (isRunning(c) || runStateAtLeast(c, TIDYING) || (runStateOf(c) == SHUTDOWN && !workQueue.isEmpty())) return;
            if (workerCountOf(c) != 0) { // Eligible to terminate
                interruptIdleWorkers(ONLY_ONE);
                return;
            }

            final ReentrantLock mainLock = this.mainLock;
            mainLock.lock();
            try {
                if (ctl.compareAndSet(c, ctlOf(TIDYING, 0))) {
                    try {
                        terminated();
                    } finally {
                        ctl.set(ctlOf(TERMINATED, 0));
                        termination.signalAll();
                    }
                    return;
                }
            } finally {
                mainLock.unlock();
            }
            // else retry on failed CAS
        }
    }
}
