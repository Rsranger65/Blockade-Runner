package net.kopeph.ld31.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/** @author alexg */
public class ThreadPool implements AutoCloseable {
	private final ExecutorService pool;

	//this is like a thread-safe integer that blocks the
	//current thread if it goes below zero
	//release() increments, and acquire() decrements
	private final Semaphore s = new Semaphore(0);
	private int pendingTasks;

	/** Maximum number of jobs that will run at any given time (set by ctor) */
	public final int poolSize;

	/** Creates a new thread pool using one thread per core. */
	public ThreadPool() {
		this(Runtime.getRuntime().availableProcessors());
	}

	/** Creates a new thread pool using the specified number of threads. */
	public ThreadPool(int poolSize) {
		pool = Executors.newFixedThreadPool(poolSize);
		this.poolSize = poolSize;
	}

	/**
	 * Add a new task to the queue for the thread pool
	 * @param run  the Runnable whose run() method we run
	 */
	public synchronized void post(final Runnable run) {
		pool.execute(() -> { run.run(); s.release(); }); //execute run run
		pendingTasks++;
	}

	/**
	 * Block the current thread until all scheduled tasks have completed.
	 * @throws InterruptedException if the current thread receives an interrupt.
	 */
	public synchronized void sync() throws InterruptedException {
		s.acquire(pendingTasks);
		pendingTasks = 0;
	}

	/**
	 * Block the current thread until all scheduled tasks have completed.
	 * Interrupts to the current thread are ignored.
	 */
	public synchronized void forceSync() {
		while (true) {
			try {
				sync();
			} catch (InterruptedException e) {
				continue;
			}
			break;
		}
	}

	//Effectively a destructor, since ThreadPool won't exit on GC
	@Override
	public void close() throws Exception {
		pool.shutdown();
		pool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS); //A very long time
	}
}
