package br.com.brjdevs.miyuki.loader.entities;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public interface ModuleResourceManager {
	String get(String path);

	default JsonElement getAsJson(String path) {
		return new JsonParser().parse(get(path));
	}
}
