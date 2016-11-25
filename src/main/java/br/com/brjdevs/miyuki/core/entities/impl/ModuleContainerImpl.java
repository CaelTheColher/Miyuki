package br.com.brjdevs.miyuki.core.entities.impl;

import br.com.brjdevs.miyuki.core.Module;
import br.com.brjdevs.miyuki.core.Module.Type;
import br.com.brjdevs.miyuki.core.entities.ModuleContainer;
import br.com.brjdevs.miyuki.core.entities.ModuleResourceManager;
import org.slf4j.Logger;

import static br.com.brjdevs.miyuki.lib.log.LogUtils.logger;

public class ModuleContainerImpl implements ModuleContainer {

	private final Class<?> moduleClass;
	private final Object moduleInstance;
	private final Module module;
	private final ModuleResourceManager manager;
	private final String moduleID, moduleName;

	public ModuleContainerImpl(Module module, Class<?> moduleClass, Object moduleInstance, ModuleResourceManager manager) {
		this.module = module;
		this.moduleClass = moduleClass;
		this.moduleInstance = moduleInstance;
		this.manager = manager;
		this.moduleID = module.id().isEmpty() ? moduleClass.getSimpleName() : module.id();
		this.moduleName = module.name().isEmpty() ? moduleID : module.name();
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
		return logger(getName());
	}

	@Override
	public String getID() {
		return moduleID;
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
