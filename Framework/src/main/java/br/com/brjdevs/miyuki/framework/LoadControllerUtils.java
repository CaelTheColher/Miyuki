package br.com.brjdevs.miyuki.framework;

import br.com.brjdevs.miyuki.framework.Module.Type;
import br.com.brjdevs.miyuki.framework.entities.ModuleContainer;
import org.slf4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import static br.com.brjdevs.miyuki.lib.StringUtils.limit;
import static br.com.brjdevs.miyuki.lib.log.LogUtils.logger;

public class LoadControllerUtils {
	private static final Logger logger = logger("LoadController");

	public static void setFields(ModuleContainer module, Class<? extends Annotation> annotation, Object object) {
		module.getFieldsForAnnotation(annotation).forEach(field -> {
			try {
				field.setAccessible(true);
				field.set(module.getRealInstance(), object);
			} catch (Exception e) {
				logger.error("Error while injecting " + limit(object.toString(), 100) + " into " + field + ":", e);
			}
		});
	}

	public static <A extends Annotation> void setFields(ModuleContainer module, Class<A> annotation, Function<A, Object> objectCreator) {
		module.getFieldsForAnnotation(annotation).forEach(field -> {
			Object object = objectCreator.apply(field.getAnnotation(annotation));
			try {
				field.setAccessible(true);
				field.set(module.getRealInstance(), object);
			} catch (Exception e) {
				logger.error("Error while injecting " + object + " into " + field + ":", e);
			}
		});
	}

	public static Object makeInstance(Class<?> clazz) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		//Creates a Set from the ModuleType[]
		Set<Type> moduleTypes = new HashSet<>();
		Collections.addAll(moduleTypes, clazz.getAnnotation(Module.class).type());

		//Instanciates a new Instance, leave it null or return
		if (moduleTypes.contains(Type.INSTANCE)) {
			Constructor<?> constructor = clazz.getDeclaredConstructor();
			constructor.setAccessible(true);
			return constructor.newInstance();
		}
		if (moduleTypes.contains(Type.STATIC)) {
			return clazz;
		}

		return null;
	}

	public static void fireEvents(Set<ModuleContainer> modules, Class<? extends Annotation> annotation) {
		for (ModuleContainer module : modules) fireEventsFor(module, annotation);
	}

	public static void fireEventsFor(ModuleContainer module, Class<? extends Annotation> annotation) {
		module.getMethodsForAnnotation(annotation).stream().filter(m -> m.getParameterCount() == 0).forEach(m -> {
			try {
				m.setAccessible(true);
				m.invoke(module.getRealInstance());
			} catch (Exception e) {
				logger.error("Error while firing event \"" + annotation + "\" from " + m + ":", e);
			}
		});
	}
}
