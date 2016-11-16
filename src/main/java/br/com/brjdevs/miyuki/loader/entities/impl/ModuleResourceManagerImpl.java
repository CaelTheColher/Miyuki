package br.com.brjdevs.miyuki.loader.entities.impl;

import br.com.brjdevs.miyuki.Loader;
import br.com.brjdevs.miyuki.loader.Module;
import br.com.brjdevs.miyuki.loader.entities.ModuleResourceManager;

public class ModuleResourceManagerImpl implements ModuleResourceManager {
	private final String moduleName;
	private final Class clazz;

	public ModuleResourceManagerImpl(Module module, Class clazz) {
		this.moduleName = module.name().replace('.', '/');
		this.clazz = clazz;
	}

	@Override
	public String get(String path) {
		return Loader.resource(clazz, "/assets/" + moduleName + "/" + path);
	}
}
