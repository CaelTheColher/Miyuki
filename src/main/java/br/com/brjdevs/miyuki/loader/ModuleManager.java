package br.com.brjdevs.miyuki.loader;

import br.com.brjdevs.miyuki.commands.ICommand;
import br.com.brjdevs.miyuki.loader.Module.*;
import br.com.brjdevs.miyuki.loader.entities.ModuleContainer;
import br.com.brjdevs.miyuki.loader.entities.ModuleResourceManager;
import br.com.brjdevs.miyuki.loader.entities.impl.ModuleContainerImpl;
import br.com.brjdevs.miyuki.loader.entities.impl.ModuleResourceManagerImpl;
import br.com.brjdevs.miyuki.modules.cmds.manager.CommandManager;
import br.com.brjdevs.miyuki.utils.Log4jUtils;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.ReconnectedEvent;
import net.dv8tion.jda.core.events.ResumedEvent;
import net.dv8tion.jda.core.hooks.SubscribeEvent;
import org.apache.logging.log4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Function;

public class ModuleManager {
	private static final Logger LOGGER = Log4jUtils.logger();
	private static Map<Class, ModuleContainer> INSTANCE_MAP = new HashMap<>();
	private static Set<Object> JDA_LISTENERS = new HashSet<>();
	private static boolean firedPre = false;
	private static boolean firedPost = false;

	static {
		JDA_LISTENERS.add(ModuleManager.class);
	}

	public static void add(Class<?> clazz) {
		try {
			//Check if class already is loaded as module
			if (INSTANCE_MAP.containsKey(clazz)) return;

			if (!clazz.isAnnotationPresent(Module.class)) return;

			Module module = clazz.getAnnotation(Module.class);

			//Can be instantiable?
			Object instance = makeInstance(clazz);
			if (instance == null) return;

			ModuleResourceManager resourceManager = new ModuleResourceManagerImpl(module, clazz);

			ModuleContainer container = new ModuleContainerImpl(module, clazz, instance, resourceManager);

			//Fields being initialized before Module.Predicate
			setFields(container, Container.class, container);
			setFields(container, ResourceManager.class, container.getResourceManager());
			setFields(container, LoggerInstance.class, container.getLogger());
			setFields(container, Instance.class, container.getInstance());
			setFields(container, Resource.class, resource -> resourceManager.get(resource.value()));
			setFields(container, JSONResource.class, resource -> resourceManager.getAsJson(resource.value()));

			//If any Module.Predicate is present and fails, it will stop the
			if (!container.getMethodsForAnnotation(Predicate.class).stream().allMatch(method -> {
				try {
					Object o = method.invoke(instance);
					return (o instanceof Boolean) ? (Boolean) o : true;
				} catch (Exception e) {
					LOGGER.error("Error while Predicating:", e);
				}
				return false;
			})) {
				fireEventsFor(container, OnDisabled.class);
				return;
			} else {
				fireEventsFor(container, OnEnabled.class);
			}

			//We past this far. Time to register.
			INSTANCE_MAP.put(clazz, container);

			//Yeah, we need to make this.
			if (module.isListener()) JDA_LISTENERS.add(container.getInstance());
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static Object[] jdaListeners() {
		return JDA_LISTENERS.toArray();
	}

	public static void firePreReadyEvents() {
		if (firedPre) throw new RuntimeException(new IllegalAccessException("Already fired."));
		fireEvents(PreReady.class);
		firedPre = true;
	}

	public static void firePostReadyEvents() {
		if (firedPost) throw new RuntimeException(new IllegalAccessException("Already fired."));
		fireEvents(PostReady.class);
		firedPost = true;
	}

	@SubscribeEvent
	private static void ready(ReadyEvent event) {
		for (ModuleContainer module : INSTANCE_MAP.values()) {
			setFields(module, JDAInstance.class, event.getJDA());
			setFields(module, SelfUserInstance.class, event.getJDA().getSelfUser());

			module.getMethodsForAnnotation(Command.class).stream().filter(method -> ICommand.class.isAssignableFrom(method.getReturnType())).forEach(m -> {
				try {
					CommandManager.addCommand(m.getAnnotation(Command.class).value(), (ICommand) m.invoke(module.getInstance()));
				} catch (Exception e) {
					LOGGER.error("Error while registering command \"" + m.getAnnotation(Command.class).value() + "\" from " + m + ":", e);
				}
			});
		}

		fireEvents(Ready.class);
	}

	@SubscribeEvent
	private static void reconnect(ReconnectedEvent event) {
		for (ModuleContainer module : INSTANCE_MAP.values())
			setFields(module, SelfUserInstance.class, event.getJDA().getSelfUser());
	}

	@SubscribeEvent
	private static void resumed(ResumedEvent event) {
		for (ModuleContainer module : INSTANCE_MAP.values())
			setFields(module, SelfUserInstance.class, event.getJDA().getSelfUser());
	}

	private static void setFields(ModuleContainer module, Class<? extends Annotation> annotation, Object object) {
		module.getFieldsForAnnotation(annotation).stream().filter(field -> object.getClass().isAssignableFrom(field.getType())).forEach(field -> {
			try {
				field.set(module.getInstance(), object);
			} catch (Exception e) {
				LOGGER.error("Error while injecting " + object + " into " + field + ":", e);
			}
		});
	}

	private static <A extends Annotation> void setFields(ModuleContainer module, Class<A> annotation, Function<A, Object> objectCreator) {
		module.getFieldsForAnnotation(annotation).forEach(field -> {
			Object object = objectCreator.apply(field.getAnnotation(annotation));
			if (object.getClass().isAssignableFrom(field.getType())) {
				try {
					field.set(module.getInstance(), object);
				} catch (Exception e) {
					LOGGER.error("Error while injecting " + object + " into " + field + ":", e);
				}
			}
		});
	}

	private static void fireEvents(Class<? extends Annotation> annotation) {
		for (ModuleContainer module : INSTANCE_MAP.values()) fireEventsFor(module, annotation);
	}

	private static void fireEventsFor(ModuleContainer module, Class<? extends Annotation> annotation) {
		module.getMethodsForAnnotation(annotation).stream().filter(m -> m.getParameterCount() == 0).forEach(m -> {
			try {
				m.invoke(module.getInstance());
			} catch (Exception e) {
				LOGGER.error("Error while firing event \"" + annotation + "\" from " + m + ":", e);
			}
		});
	}

	private static Object makeInstance(Class<?> clazz) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
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
}
