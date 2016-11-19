package br.com.brjdevs.miyuki.modules.rest;

import br.com.brjdevs.miyuki.modules.db.DBModule;
import br.com.brjdevs.miyuki.modules.db.GuildModule;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@RestController
public class GetController {
	public static final Map<String, Function<Map<String, String>, JsonElement>> api = new HashMap<>();

	static {
		api.put("user", map -> {
			String id = map.getOrDefault("id", "");
			if (id.isEmpty()) return WebInterfaceHelper.error("Invalid User");
			User user = RESTInterface.jda.getUserById(id);
			if (user == null) return WebInterfaceHelper.error("User not found");
			JsonObject object = WebInterfaceHelper.object();
			JsonArray array = new JsonArray();
			RESTInterface.jda.getGuilds().stream().filter(guild -> guild.getOwner().getUser().equals(user)).forEach(g -> {
				if (map.containsKey("detailed")) array.add(toJson(g));
				else array.add(new JsonPrimitive(g.getId()));
			});
			object.add("guildsOwned", array);
			object.addProperty("owner", DBModule.getOwnerIDs().contains(id));
			return object;
		});

		api.put("me", map -> {
			JsonObject object = WebInterfaceHelper.object();
			object.addProperty("avatar", RESTInterface.jda.getSelfUser().getAvatarUrl());
			JsonArray array = new JsonArray();
			DBModule.getOwnerIDs().forEach(s -> array.add(new JsonPrimitive(s)));
			object.add("owner", array);
			return object;
		});
	}

	public static JsonElement toJson(Guild g) {
		JsonObject guild = new JsonObject();
		guild.addProperty("id", g.getId());
		guild.addProperty("name", g.getName());
		guild.addProperty("avatar", g.getIconUrl());
		guild.addProperty("vip", GuildModule.fromDiscord(g).getFlag("vip"));
		return guild;
	}

	@RequestMapping("/get")
	public String api(@RequestParam Map<String, String> params) {
		return api.getOrDefault(params.getOrDefault("type", ""), WebInterfaceHelper.API_CALL_NOT_FOUND).apply(params).toString();
	}
}
