package br.com.brjdevs.miyuki.loader.entities;

import br.com.brjdevs.miyuki.loader.Module.Type;
import org.apache.logging.log4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface ModuleContainer {
	Class<?> getModuleClass();

	Object getInstance();

	Logger getLogger();

	String getName();

	Type[] getType();

	ModuleResourceManager getResourceManager();

	default Set<Field> getFieldsForAnnotation(Class<? extends Annotation> annotation) {
		return Stream.of(getModuleClass().getDeclaredFields()).filter(f -> f.isAnnotationPresent(annotation)).collect(Collectors.toSet());
	}

	default Set<Method> getMethodsForAnnotation(Class<? extends Annotation> annotation) {
		return Stream.of(getModuleClass().getDeclaredMethods()).filter(f -> f.isAnnotationPresent(annotation)).collect(Collectors.toSet());
	}

	default boolean isAnnotationPresent(Class<? extends Annotation> annotation) {
		return getModuleClass().isAnnotationPresent(annotation);
	}

	default <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
		return getModuleClass().getAnnotation(annotationClass);
	}
}
