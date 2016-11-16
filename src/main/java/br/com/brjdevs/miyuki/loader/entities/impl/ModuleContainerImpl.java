package br.com.brjdevs.miyuki.loader.entities.impl;

import br.com.brjdevs.miyuki.loader.Module;
import br.com.brjdevs.miyuki.loader.Module.Type;
import br.com.brjdevs.miyuki.loader.entities.ModuleContainer;
import br.com.brjdevs.miyuki.loader.entities.ModuleResourceManager;

public class ModuleContainerImpl implements ModuleContainer {


	private final Class<?> moduleClass;
	private final Object moduleInstance;
	private final Module module;
	private final ModuleResourceManager manager;

	public ModuleContainerImpl(Module module, Class<?> moduleClass, Object moduleInstance, ModuleResourceManager manager) {
		this.module = module;
		this.moduleClass = moduleClass;
		this.moduleInstance = moduleInstance;
		this.manager = manager;
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
	public String getName() {
		return module.name();
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
