package br.com.brjdevs.miyuki.core.entities;

import com.google.gson.JsonElement;

public interface ModuleResourceManager {
	String get(String path);

	JsonElement getAsJson(String path);
}
