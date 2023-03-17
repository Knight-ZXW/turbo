
## 替换逻辑
### 线程池替换
- 所有直接继承自 ThreadPoolExecutor的子类，都被替换为继承 PThreadPoolExecutor
-  所有直接调用 new ThreadPollExecutor(xx,..)实例的代码 都被替换为 new PThreadPoolExecutor(xx,...)
### Thread 替换
- 所有直接继承自 Thread 的子类，都被替换为继承 PThread
- 所有调用 new Thread(...)的操作 都被替换为 new PThread(...)
### ThreadTimer 替换
- 所有直接继承自 ThreadTimer，都被替换为继承 PThreadTimer
- 所有 new ThreadTimer(...)的操作都被替换为 new PThreadTimer(...)
### AsyncTask 替换
- 所有直接继承自 AsyncTask 的子类 都被替换为 继承PthreadAsyncTask
- 所有 new AsyncTask(...)的函数调用都被替换为 new PthreadAsyncTask(...)

### 未支持
1. HandlerThread 比较特殊，因此未支持线程收敛。原因: 由于每个HandlerThread都需要分配独立的Looper,如果将所有HandlerThread收敛到同一个线程，
则同时只有一个Looper，这样会影响原程序的逻辑。