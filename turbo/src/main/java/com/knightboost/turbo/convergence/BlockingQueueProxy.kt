package com.knightboost.turbo.convergence

import java.util.concurrent.BlockingQueue
import java.util.concurrent.TimeUnit

public final class BlockingQueueProxy<E>(val delegate: BlockingQueue<E>) : BlockingQueue<E?> {

    companion object{

        fun <E> proxy(blockingQueue: BlockingQueue<E>?):BlockingQueue<E>?{
            if (blockingQueue == null
                || blockingQueue is BlockingQueueProxy<*>){
                return blockingQueue
            }
            return BlockingQueueProxy(blockingQueue)  as BlockingQueue<E>?
        }
    }

    override fun add(element: E?): Boolean {
        return delegate.add(element)
    }

    override fun addAll(elements: Collection<E?>): Boolean {
        return delegate.addAll(elements)
    }

    override fun clear() {
        delegate.clear()
    }

    override fun iterator(): MutableIterator<E?> {
        return delegate.iterator()
    }

    override fun remove(element: E?): Boolean {
        return delegate.remove(element)
    }

    override fun remove(): E? {
        return delegate.remove()
    }

    override fun removeAll(elements: Collection<E?>): Boolean {
        return delegate.removeAll(elements)
    }

    override fun retainAll(elements: Collection<E?>): Boolean {
        return delegate.retainAll(elements)
    }

    override fun contains(element: E?): Boolean {
        return delegate.contains(element)
    }

    override fun containsAll(elements: Collection<E?>): Boolean {
        return delegate.containsAll(elements)
    }

    override fun isEmpty(): Boolean {
        return delegate.isEmpty()
    }

    override fun offer(e: E?): Boolean {
        return delegate.offer(e)
    }

    override fun offer(e: E?, timeout: Long, unit: TimeUnit?): Boolean {
        return delegate.offer(e,timeout,unit)
    }

    override fun poll(timeout: Long, unit: TimeUnit?): E? {
        if (!SuperThreadPoolManager.enableBlockFetchStack) {
            return poll()
        }else{
            return this.delegate.poll(timeout,unit)
        }
    }

    override fun poll(): E? {
        return delegate.poll()
    }

    override fun element(): E? {
        return delegate.element()
    }

    override fun peek(): E? {
        return delegate.peek()
    }

    override fun put(e: E?) {
        delegate.put(e)
    }

    override fun take(): E? {
        //TODO
        //return !SuperThreadPoolManager.INSTANCE.getEnableBlockFetchTask() ? poll() : this.delegate.take();
        return delegate.take()
    }

    override fun remainingCapacity(): Int {
        return delegate.remainingCapacity()
    }

    override fun drainTo(c: MutableCollection<in E?>?): Int {
        return delegate.drainTo(c)
    }

    override fun drainTo(c: MutableCollection<in E?>?, maxElements: Int): Int {
        return delegate.drainTo(c,maxElements)
    }

    fun getDelegateType(): String {
        return delegate.javaClass.name
    }

    override val size: Int
        get() = delegate.size

}