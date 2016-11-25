package br.com.brjdevs.miyuki.lib.data;

import java.util.function.Consumer;

public interface Commitable<T> {
	static <T> Commitable<T> bake(T object, Consumer<T> onPushChanges) {
		return new Commitable<T>() {
			public boolean c = false;

			@Override
			public T get() {
				if (c) throw new IllegalStateException("Already Pushed Changes.");
				return object;
			}

			@Override
			public void pushChanges() {
				if (c) throw new IllegalStateException("Already Pushed Changes.");
				c = true;
				onPushChanges.accept(object);
			}
		};
	}

	T get();

	void pushChanges();
}
