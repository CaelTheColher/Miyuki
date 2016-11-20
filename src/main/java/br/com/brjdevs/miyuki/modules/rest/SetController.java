package br.com.brjdevs.miyuki.modules.rest;

import com.google.gson.JsonElement;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@RestController
public class SetController {
	public static final Map<String, Function<Map<String, String>, JsonElement>> api = new HashMap<>();

//	static {
//
//	}

	@RequestMapping("/set")
	public String api(@RequestParam Map<String, String> params) {
		return api.getOrDefault(params.getOrDefault("type", ""), WebInterfaceHelper.API_CALL_NOT_FOUND).apply(params).toString();
	}
}
