package br.com.brjdevs.miyuki.utils;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ThreadBuilder {
	private Consumer<Thread> builder = thread -> {
	};

	public ThreadBuilder setPriority(int newPriority) {
		builder.andThen(thread -> thread.setPriority(newPriority));
		return this;
	}

	public ThreadBuilder setName(String name) {
		builder.andThen(thread -> thread.setName(name));
		return this;
	}

	public ThreadBuilder setDaemon(boolean on) {
		builder.andThen(thread -> thread.setDaemon(on));
		return this;
	}

	public ThreadBuilder setContextClassLoader(ClassLoader cl) {
		builder.andThen(thread -> thread.setContextClassLoader(cl));
		return this;
	}

	public ThreadBuilder setUncaughtExceptionHandler(Thread.UncaughtExceptionHandler eh) {
		builder.andThen(thread -> thread.setUncaughtExceptionHandler(eh));
		return this;
	}

	public Thread build() {
		return build(Thread::new);
	}

	public Thread build(Supplier<Thread> threadSupplier) {
		Thread thread = threadSupplier.get();
		builder.accept(thread);
		return thread;
	}
}
