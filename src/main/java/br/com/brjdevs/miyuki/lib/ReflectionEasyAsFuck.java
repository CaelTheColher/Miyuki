package br.com.brjdevs.miyuki.lib;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ReflectionEasyAsFuck {
	public static class Static {
		public static <R> Supplier<R> getField(Class clazz, String fieldName, Class<R> type) {
			Field field = Internal.field(clazz, fieldName);
			return () -> Internal.get(field, null, type);
		}

		public static <R> Consumer<R> setField(Class clazz, String fieldName, Class<R> type) {
			Field field = Internal.field(clazz, fieldName);
			return r -> Internal.set(field, null, r);
		}

		public static <R> Consumer<R> setFinalField(Class clazz, String fieldName, Class<R> type) {
			Field field = Internal.field(clazz, fieldName);
			return r -> Internal.set(field, null, r);
		}
	}

	public static class Virtual {
		public static <T, R> Function<T, R> getField(Class<T> clazz, String fieldName, Class<R> type) {
			Field field = Internal.field(clazz, fieldName);
			return t -> Internal.get(field, t, type);
		}

		public static <T, R> BiConsumer<T, R> setField(Class<T> clazz, String fieldName, Class<R> type) {
			Field field = Internal.field(clazz, fieldName);
			return (t, r) -> Internal.set(field, t, r);
		}

		public static <T, R> BiConsumer<T, R> setFinalField(Class<T> clazz, String fieldName, Class<R> type) {
			Field field = Internal.field(clazz, fieldName);
			return (t, r) -> Internal.set(field, t, r);
		}
	}


	@SuppressWarnings("unchecked")
	private static class Internal {
		public static Field field(Class c, String f) {
			try {
				return c.getDeclaredField(f);
			} catch (NoSuchFieldException e) {
				throw new RuntimeException(e);
			}
		}

		public static <R> R get(Field field, Object object, Class<R> returnType) {
			try {
				field.setAccessible(true);
				R r = (R) field.get(object);
				field.setAccessible(false);
				return r;
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

		public static void set(Field field, Object object, Object set) {
			try {
				field.setAccessible(true);
				field.set(object, set);
				field.setAccessible(false);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

		public static void setFinal(Field field, Object object, Object set) {
			set(field(Field.class, "modifiers"), field, field.getModifiers() & ~Modifier.FINAL);
			set(field, object, set);
		}
	}
}
