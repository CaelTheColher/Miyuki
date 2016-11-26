package br.com.brjdevs.miyuki.framework.entities;

import br.com.brjdevs.miyuki.framework.Module.Type;
import org.slf4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

public interface ModuleContainer extends Comparable<ModuleContainer> {
	Class<?> getModuleClass();

	Object getInstance();

	Object getRealInstance();

	Logger getLogger();

	String getID();

	String getName();

	Type[] getType();

	ModuleResourceManager getResourceManager();

	Set<Field> getFieldsForAnnotation(Class<? extends Annotation> annotation);

	Set<Method> getMethodsForAnnotation(Class<? extends Annotation> annotation);

	boolean isAnnotationPresent(Class<? extends Annotation> annotation);

	<A extends Annotation> A getAnnotation(Class<A> annotationClass);
}
