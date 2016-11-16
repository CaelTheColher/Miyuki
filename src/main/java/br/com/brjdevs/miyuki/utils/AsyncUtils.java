package br.com.brjdevs.miyuki.utils;

public class AsyncUtils {
	public static Runnable async(final Runnable doAsync) {
		return new Thread(doAsync)::start;
	}

	public static Runnable async(final String name, final Runnable doAsync) {
		return new Thread(doAsync, name)::start;
	}

	public static void sleep(int milis) {
		try {
			Thread.sleep(milis);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static Runnable asyncSleepThen(final int milis, final Runnable doAfter) {
		return async(() -> {
			sleep(milis);
			if (doAfter != null) doAfter.run();
		});
	}
}
