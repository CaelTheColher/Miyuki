package br.com.brjdevs.miyuki.core.entities.impl;

import br.com.brjdevs.miyuki.core.LoadController;
import br.com.brjdevs.miyuki.core.Module;
import br.com.brjdevs.miyuki.core.entities.ModuleResourceManager;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class ModuleResourceManagerImpl implements ModuleResourceManager {
	private final String moduleID;
	private final Class clazz;

	public ModuleResourceManagerImpl(Module module, Class clazz) {
		this.moduleID = module.id().replace('.', '/');
		this.clazz = clazz;
	}

	@Override
	public String get(String path) {
		return LoadController.resource(clazz, "/assets/" + moduleID + "/" + path);
	}

	@Override
	public JsonElement getAsJson(String path) {
		return new JsonParser().parse(get(path));
	}
}
