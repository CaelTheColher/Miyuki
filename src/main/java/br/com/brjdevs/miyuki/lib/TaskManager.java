package br.com.brjdevs.miyuki.lib;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TaskManager {
	private static final ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 5);

	public static ExecutorService getThreadPool() {
		return threadPool;
	}

	public static void startAsyncTask(String task, Runnable scheduled, int everySeconds) {
		Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, task + "Executor")).scheduleAtFixedRate(scheduled, 0, everySeconds, TimeUnit.SECONDS);
	}
}
