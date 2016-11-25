package br.com.brjdevs.miyuki.core.entities.impl;

import br.com.brjdevs.miyuki.core.Loader;
import br.com.brjdevs.miyuki.core.Module;
import br.com.brjdevs.miyuki.core.entities.ModuleResourceManager;

public class ModuleResourceManagerImpl implements ModuleResourceManager {
	private final String moduleID;
	private final Class clazz;

	public ModuleResourceManagerImpl(Module module, Class clazz) {
		this.moduleID = module.id().replace('.', '/');
		this.clazz = clazz;
	}

	@Override
	public String get(String path) {
		return Loader.resource(clazz, "/assets/" + moduleID + "/" + path);
	}
}
