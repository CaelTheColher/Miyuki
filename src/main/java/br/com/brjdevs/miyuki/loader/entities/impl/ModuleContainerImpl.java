package br.com.brjdevs.miyuki.loader.entities.impl;

import br.com.brjdevs.miyuki.loader.Module;
import br.com.brjdevs.miyuki.loader.Module.Type;
import br.com.brjdevs.miyuki.loader.entities.ModuleContainer;
import br.com.brjdevs.miyuki.loader.entities.ModuleResourceManager;
import br.com.brjdevs.miyuki.utils.Log4jUtils;
import org.apache.logging.log4j.Logger;

public class ModuleContainerImpl implements ModuleContainer {

	private final Class<?> moduleClass;
	private final Object moduleInstance;
	private final Module module;
	private final ModuleResourceManager manager;
	private final String moduleName;

	public ModuleContainerImpl(Module module, Class<?> moduleClass, Object moduleInstance, ModuleResourceManager manager) {
		this.module = module;
		this.moduleClass = moduleClass;
		this.moduleInstance = moduleInstance;
		this.manager = manager;
		this.moduleName = module.name().isEmpty() ? moduleClass.getSimpleName() : module.name();
	}

	@Override
	public Class<?> getModuleClass() {
		return moduleClass;
	}

	@Override
	public Object getInstance() {
		return moduleInstance;
	}

	@Override
	public Object getRealInstance() {
		return moduleInstance instanceof Class ? null : moduleInstance;
	}

	@Override
	public Logger getLogger() {
		return Log4jUtils.logger(getModuleClass());
	}

	@Override
	public String getName() {
		return moduleName;
	}

	@Override
	public Type[] getType() {
		return module.type();
	}

	@Override
	public ModuleResourceManager getResourceManager() {
		return manager;
	}
}
