package com.knightboost.turbo.rename.base;

import com.knightboost.turbo.proxy.PthreadThread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultThreadFactory implements ThreadFactory {
    private final ThreadGroup group;
    private final String namePrefix;
    private final AtomicInteger threadNumber = new AtomicInteger(1);


    public DefaultThreadFactory(String prefix){
        SecurityManager securityManager = System.getSecurityManager();
        this.group = securityManager !=null? securityManager.getThreadGroup():
                Thread.currentThread().getThreadGroup();
        this.namePrefix = prefix;
    }

    public String getNamePrefix(){
        return namePrefix;
    }


    public Thread createThread(ThreadGroup threadGroup,Runnable runnable,String name,long stackSize){
        return new PthreadThread(threadGroup,runnable,name,stackSize);
    }


    //tod WHY?
    @Override
    public Thread newThread(Runnable r) {
        Thread createdThread = createThread(group, r,
                this.namePrefix + this.threadNumber.getAndIncrement(),
                0);

        if (createdThread.isDaemon()){
            createdThread.setDaemon( false);
        }

        if (createdThread.getPriority()!=5){
            createdThread.setPriority(5);
        }
        return createdThread;
    }
}
