package br.com.brjdevs.miyuki.framework;

import br.com.brjdevs.java.lib.IOHelper;
import br.com.brjdevs.miyuki.framework.Module.*;
import br.com.brjdevs.miyuki.framework.entities.CommandRegisterEvent;
import br.com.brjdevs.miyuki.framework.entities.ModuleContainer;
import br.com.brjdevs.miyuki.framework.entities.impl.ModuleContainerImpl;
import br.com.brjdevs.miyuki.framework.entities.impl.ModuleResourceManagerImpl;
import br.com.brjdevs.miyuki.lib.DiscordUtils;
import br.com.brjdevs.miyuki.lib.Java;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.ReconnectedEvent;
import net.dv8tion.jda.core.events.ResumedEvent;
import net.dv8tion.jda.core.hooks.AnnotatedEventManager;
import net.dv8tion.jda.core.hooks.SubscribeEvent;
import org.reflections.Reflections;
import org.slf4j.Logger;

import java.util.*;

import static br.com.brjdevs.miyuki.framework.LoadControllerUtils.*;
import static br.com.brjdevs.miyuki.lib.log.LogUtils.logger;

public class LoadController {
	public static final Reflections REFLECTIONS = new Reflections("");
	private static final Logger logger = logger("LoadController");
	public static String[] args;
	public static String token = null;
	public static AccountType accountType = AccountType.BOT;
	private static Map<Class, ModuleContainer> instanceMap = new HashMap<>();

	public static Set<ModuleContainer> modules() {
		return Collections.unmodifiableSet(new TreeSet<>(instanceMap.values()));
	}

	public static void main(String[] args) throws Exception {
		LoadController.args = args;
		DiscordUtils.hackJDALog();
		try {
			createContainers(scanClasspath());
			predicate(injectStarters(modules()));
			fireEvents(modules(), PreReady.class);
			createJDA();
			fireEvents(modules(), PostReady.class);
		} catch (Exception e) {
			e.printStackTrace();
			Java.stopApp();
		}
	}

	private static void createJDA() throws Exception {
		if (token == null || token.isEmpty()) {
			logger.error("No token set. Aborting...");

			throw new IllegalStateException("Set \"LoadController.token\" to a valid Token!");
		}
		logger.error("Modules: " + Arrays.toString(modules().toArray()));
		new JDABuilder(accountType)
			.setToken(token)
			.setEventManager(new AnnotatedEventManager())
			.addListener(jdaListeners())
			.buildBlocking();
	}

	private static Object[] jdaListeners() {
		List<Object> list = new ArrayList<>();
		list.add(LoadController.class);
		Collections.addAll(
			list,
			modules().stream()
				.filter(container -> container.getAnnotation(Module.class).isListener())
				.map(ModuleContainer::getInstance)
				.toArray()
		);
		return list.toArray();
	}

	@SubscribeEvent
	private static void ready(ReadyEvent event) {
		Set<ModuleContainer> modules = modules();
		for (ModuleContainer module : modules) {
			setFields(module, JDAInstance.class, event.getJDA());
			setFields(module, SelfUserInstance.class, event.getJDA().getSelfUser());

			logger.trace(module + ".Methods = " + Arrays.toString(module.getModuleClass().getDeclaredMethods()));
			logger.trace(module + ".Methods.Command = " + Arrays.toString(module.getMethodsForAnnotation(Command.class).toArray()));

			module.getMethodsForAnnotation(Command.class).forEach(m -> {
				try {
					m.setAccessible(true);
					CommandRegisterEvent registerEvent = new CommandRegisterEvent(m.invoke(module.getRealInstance()), m.getAnnotation(Command.class).value());
					modules.forEach(container -> container.getMethodsForAnnotation(CommandRegister.class).forEach(method -> {
						try {
							method.setAccessible(true);
							method.invoke(registerEvent);
						} catch (Exception e) {
							logger.error("Error while registering command \"" + m.getAnnotation(Command.class).value() + "\" from " + m + " into " + method + ":", e);
						}
					}));
				} catch (Exception e) {
					logger.error("Error while registering command \"" + m.getAnnotation(Command.class).value() + "\" from " + m + ":", e);
				}
			});
		}

		fireEvents(modules, Ready.class);
	}

	@SubscribeEvent
	private static void reconnect(ReconnectedEvent event) {
		for (ModuleContainer module : modules())
			setFields(module, SelfUserInstance.class, event.getJDA().getSelfUser());
	}

	@SubscribeEvent
	private static void resumed(ResumedEvent event) {
		for (ModuleContainer module : modules())
			setFields(module, SelfUserInstance.class, event.getJDA().getSelfUser());
	}

	private static Set<ModuleContainer> injectStarters(Set<ModuleContainer> modules) {
		for (ModuleContainer module : modules) {
			try {
				//Fields being initialized before Module.Predicate
				setFields(module, Container.class, module);
				setFields(module, ResourceManager.class, module.getResourceManager());
				setFields(module, LoggerInstance.class, module.getLogger());
				setFields(module, Instance.class, module.getRealInstance());
				setFields(module, Resource.class, resource -> module.getResourceManager().get(resource.value()));
				setFields(module, JSONResource.class, resource -> module.getResourceManager().getAsJson(resource.value()));
			} catch (Exception e) {
				logger.error("Exception ocurred", e);
				//TODO PROPER ERROR HANDLER
			}
		}

		return modules;
	}

	private static void predicate(Set<ModuleContainer> modules) {
		Set<ModuleContainer> disabled = new HashSet<>();
		for (ModuleContainer module : modules) {
			try {
				if (!module.getMethodsForAnnotation(Predicate.class).stream().allMatch(method -> {
					try {
						method.setAccessible(true);
						Object o = method.invoke(module.getInstance());
						return (o instanceof Boolean) ? (Boolean) o : true;
					} catch (Exception e) {
						logger.error("Error while Predicating:", e);
					}
					return false;
				})) {
					disabled.add(module);
				}
			} catch (Exception e) {
				logger.error("Exception ocurred", e);
				//TODO PROPER ERROR HANDLER
			}
		}

		modules.forEach(container -> {
			if (disabled.contains(container)) fireEventsFor(container, OnDisabled.class);
			else fireEventsFor(container, OnEnabled.class);
		});

		instanceMap.values().removeAll(disabled);
	}

	private static void createContainers(Set<Class<?>> classes) {
		for (Class<?> moduleClass : classes) {
			try {
				//Check if class already is loaded as module
				if (instanceMap.containsKey(moduleClass)) return;
				if (!moduleClass.isAnnotationPresent(Module.class)) return;

				Module module = moduleClass.getAnnotation(Module.class);

				//Can be instantiable?
				Object instance = makeInstance(moduleClass);
				if (instance == null) throw new IllegalStateException("Could not instantiate object.");

				instanceMap.put(
					moduleClass,
					new ModuleContainerImpl(module, moduleClass, instance, new ModuleResourceManagerImpl(module, moduleClass))
				);
			} catch (Exception e) {
				logger.error("Exception ocurred", e);
				//TODO PROPER ERROR HANDLER
			}
		}
	}

	private static Set<Class<?>> scanClasspath() {
		return REFLECTIONS.getTypesAnnotatedWith(Module.class);
	}

	public static String resource(Class c, String file) {
		return IOHelper.toString(c.getResourceAsStream(file));
	}
}
