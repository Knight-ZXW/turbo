package com.knightboost.turbo

import android.os.Build
import com.knightboost.turbo.PthreadUtil.getSystemThreadGroup
import com.knightboost.turbo.convergence.BlockingQueueProxy
import com.knightboost.turbo.convergence.ThreadFactoryProxy
import com.knightboost.turbo.proxy.PThreadPoolExecutor
import com.knightboost.turbo.rename.base.DefaultThreadFactory
import java.lang.ref.SoftReference
import java.util.concurrent.*

object PThreadThreadPoolCache {

    val workPool: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor { r ->
        val thread = Thread(r)
        thread.name = "pthread_work_pool"
        thread
    }

    private val mCache = mutableListOf<SoftReference<PThreadPoolExecutor>>()

    var onTrimAllListener: OnTrimAllListener? = null

    interface OnTrimAllListener {
        fun onTrimAll(beforeTrimThreadCount:Int,afterTrimThreadCount:Int,poolSizeInfo: Map<String, Int>
        )
    }

    interface OnPthreadOomListener {
        fun onOom(
            poolSizeInfo: Map<String, Int>,
            sameThreadNameNoneActiveCountMap: Map<String, Int>,
            activeCount: Int,
            nonWaitingThreadCount: Int,
            name: String
        );
    }

    var onPthreadOomListener: OnPthreadOomListener? = null

    init {
        workPool.schedule(
            {
                PthreadUtil.log("PThreadThreadPoolCache", "init workPool success")
            }, 1L, TimeUnit.MINUTES
        )
    }

    @Synchronized
    fun getHasFreePoolList(
        queueClass: String?,
        onlyFirst: Boolean,
        needSort: Boolean
    ): MutableList<PThreadPoolExecutor> {
        PthreadUtil.log(
            "PThreadThreadPoolCache",
            "getHasFreePoolList queueClass ${queueClass}, onlyFirst= ${onlyFirst}, needSort=${needSort}"
        )

        val arrayList = mutableListOf<PThreadPoolExecutor>()
        val it = mCache.iterator()
        while (it.hasNext()) {
            val pThreadPoolExecutor: PThreadPoolExecutor? = it.next().get()
            pThreadPoolExecutor?.let {
                var queue = it.queue
                if (queue is BlockingQueueProxy<*>) {
                    queue = queue.delegate as BlockingQueue<Runnable>?
                }

                val isPriorityBlockingQueue = queue is PriorityBlockingQueue

                if (!pThreadPoolExecutor.isShutdown && !pThreadPoolExecutor.isTerminated && !pThreadPoolExecutor.isTerminated && pThreadPoolExecutor.isWorkQueueEmpty() && (queueClass == null || !isPriorityBlockingQueue)) {
                    arrayList.add(pThreadPoolExecutor)
                }

            }

        }

        if (needSort && !onlyFirst) {
            arrayList.sortWith(object : Comparator<PThreadPoolExecutor> {
                override fun compare(o1: PThreadPoolExecutor?, o2: PThreadPoolExecutor?): Int {
                    val o1Size = o1?.poolSize ?: 0
                    val o2Size = o2?.poolSize ?: 0
                    if (o1Size == o2Size) {
                        return 0
                    }
                    return if (o1Size > o2Size) 1 else -1
                }
            })
        }
        return arrayList

    }

    @Synchronized
    fun addCache(pThreadPoolExecutor: PThreadPoolExecutor) {
        removeCache(pThreadPoolExecutor)
        mCache.add(SoftReference<PThreadPoolExecutor>(pThreadPoolExecutor))
    }

    @Synchronized
    fun findFreeExecutor(str: String, str2: String): PThreadPoolExecutor? {
        val hasFreePoolList = getHasFreePoolList(str2, onlyFirst = false, needSort = false)
        //TODO 这是做什么？
        onTriggerOom(hasFreePoolList, str)
        val firstPThreadPoolExecutor = hasFreePoolList.getOrNull(0)
        trimAllThreadPool()
        return firstPThreadPoolExecutor
    }

    @Synchronized
    fun getAllPoolActiveSize():Int{
        var count = 0
        for (softReference in mCache) {
            val pThreadPoolExecutor = softReference.get()
            if (pThreadPoolExecutor!=null){
                count +=pThreadPoolExecutor.activeCount
            }
        }
        return count
    }

    @Synchronized
    fun getAllPoolSize():Int{
        var count = 0
        for (softReference in mCache) {
            val pThreadPoolExecutor = softReference.get()
            if (pThreadPoolExecutor!=null){
                count +=pThreadPoolExecutor.poolSize
            }
        }
        return count
    }


    fun getQueueType(blockingQueue: BlockingQueue<*>): String {
        if (blockingQueue is BlockingQueueProxy<*>) {
            return blockingQueue.getDelegateType()
        }
        return blockingQueue::javaClass.name
    }

    @Synchronized
    fun removeCache(pThreadPoolExecutor: PThreadPoolExecutor) {
        val iterator = mCache.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            if (item.get() == pThreadPoolExecutor) {
                iterator.remove()
            }
        }
    }

    fun trimAllThreadPool() {
        if (PthreadUtil.enableTrimThreadWhenOom) {
            workPool.schedule({
                var allThreadCountBeforeTrim = 0
                if (onTrimAllListener!=null){
                    allThreadCountBeforeTrim = PthreadUtil.getJavaThreadCount()
                }
                var name:String? =null

                val map = mutableMapOf<String,Int>()
                val poolList = getHasFreePoolList(null, onlyFirst = false, needSort = true)
                for (pThreadPoolExecutor in poolList) {
                    val sb = StringBuilder()

                    val threadFactory = pThreadPoolExecutor.threadFactory
                    if (threadFactory is DefaultThreadFactory){
                        name = threadFactory.namePrefix
                    }else if (threadFactory is ThreadFactoryProxy){
                        name = threadFactory.getDelegateType()
                    }else{
                        name = pThreadPoolExecutor.threadFactory.javaClass.name
                    }

                    sb.append(name)
                    sb.append("_")
                    sb.append(pThreadPoolExecutor.hashCode())
                    map.put(sb.toString(),pThreadPoolExecutor.poolSize)
                    trimThreadPool(pThreadPoolExecutor)
                }
                val curOnTrimALlListener = onTrimAllListener ?: return@schedule

                val javaThreadCount = PthreadUtil.getJavaThreadCount()
                curOnTrimALlListener.onTrimAll(allThreadCountBeforeTrim,javaThreadCount,map)
            }, 100L, TimeUnit.MINUTES)

        }
    }

    fun trimFirstEmptyPool(name: String) {
        val freePoolList = getHasFreePoolList(name, onlyFirst = true, needSort = true)
        if (freePoolList.isNullOrEmpty()){
            return
        }

        val pThreadPoolExecutor = freePoolList.getOrNull(0)
        if (pThreadPoolExecutor!=null){
            trimThreadPool(pThreadPoolExecutor)
        }
        trimAllThreadPool()
    }



    fun trimThreadPool(pThreadPoolExecutor: PThreadPoolExecutor){
        val keepAliveTime = pThreadPoolExecutor.getKeepAliveTime(TimeUnit.NANOSECONDS)
        val allowsCoreThreadTimeOut = pThreadPoolExecutor.allowsCoreThreadTimeOut()
        pThreadPoolExecutor.setKeepAliveTime(1L,TimeUnit.NANOSECONDS)

        //TODO 解释 为什么 先 false 再true
        if (allowsCoreThreadTimeOut){
            allowCoreThreadTimeOut(pThreadPoolExecutor,false)
        }
        allowCoreThreadTimeOut(pThreadPoolExecutor,true)

        workPool.schedule({
            val legalKeepAliveTime = if (keepAliveTime == 0L) 60 else keepAliveTime
            pThreadPoolExecutor.setKeepAliveTime(legalKeepAliveTime,TimeUnit.SECONDS)
            allowCoreThreadTimeOut(pThreadPoolExecutor,allowsCoreThreadTimeOut)
        },10L,TimeUnit.MILLISECONDS)

    }

    private fun allowCoreThreadTimeOut(threadPoolExecutor: ThreadPoolExecutor, allowCoreThreadTimeOut:Boolean){
        try {
            if (Build.VERSION.SDK_INT <=23){
                try {
                    threadPoolExecutor.allowCoreThreadTimeOut(allowCoreThreadTimeOut)
                    return
                }catch (e:Exception){
                    if (e !is ClassCastException){
                        throw e
                    }
                    return
                }
            }
            threadPoolExecutor.allowCoreThreadTimeOut(allowCoreThreadTimeOut)
        }catch (e2:Exception){
            if (e2 is IllegalArgumentException){
                return
            }
            throw e2
        }
    }



    private fun onTriggerOom(pthreadPoolExecutors: List<PThreadPoolExecutor>, where: String) {
        var name: String

        //poolName->PoolSize
        val poolThreadSizeMap = LinkedHashMap<String, Int>()

        var sb: StringBuilder
        for (pThreadPoolExecutor in pthreadPoolExecutors) {
            if (pThreadPoolExecutor.threadFactory is DefaultThreadFactory) {
                sb = java.lang.StringBuilder()
                val threadFactory = pThreadPoolExecutor.threadFactory
                name = (threadFactory as DefaultThreadFactory).namePrefix
            } else if (pThreadPoolExecutor.threadFactory is ThreadFactoryProxy) {
                sb = java.lang.StringBuilder()
                name = (pThreadPoolExecutor.threadFactory as ThreadFactoryProxy).getDelegateType()
            } else {
                sb = java.lang.StringBuilder()
                name = pThreadPoolExecutor.threadFactory.javaClass.name
            }
            sb.append(name)
            sb.append("_")
            sb.append(pThreadPoolExecutor.hashCode())
            poolThreadSizeMap[sb.toString()] = Integer.valueOf(pThreadPoolExecutor.poolSize)
        }
        val sameThreadNameNoneActiveCount = LinkedHashMap<String, Int>()
        val systemThreadGroup = getSystemThreadGroup()
        val activeCount = systemThreadGroup.activeCount()

        //copy所有active 的线程
        val threadArr = arrayOfNulls<Thread>(activeCount + activeCount / 2)
        val totalThreadCount = systemThreadGroup.enumerate(threadArr)

        var waitThreadCount = 0
        var state: Thread.State
        for (index in 0 until totalThreadCount) {
            val thread = threadArr[index]
            if (thread != null && (thread.state.also {
                    state = it
                } == Thread.State.BLOCKED || state == Thread.State.WAITING || state == Thread.State.TIMED_WAITING)) {
                waitThreadCount++
                val regex = Regex("[0-9]")
                val replace = regex.replace(thread.name, "")
                sameThreadNameNoneActiveCount[replace] =
                    (sameThreadNameNoneActiveCount[replace] ?: 1) + 1
            }
        }
        onPthreadOomListener?.onOom(
            poolThreadSizeMap,
            sameThreadNameNoneActiveCount,
            totalThreadCount,
            waitThreadCount,
            where
        )
    }

}