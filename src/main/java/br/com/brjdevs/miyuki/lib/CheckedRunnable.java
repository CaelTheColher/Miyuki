package br.com.brjdevs.miyuki.lib;

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