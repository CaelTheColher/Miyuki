package br.com.brjdevs.miyuki.utils;

@FunctionalInterface
public interface CheckedRunnable<T, E extends Exception> {
	T run() throws E;

	default T runOrThrowRuntime() {
		try {
			return run();
		} catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}
}