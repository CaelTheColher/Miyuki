package br.com.brjdevs.miyuki.modules.db;

import br.com.brjdevs.miyuki.framework.Module;
import br.com.brjdevs.miyuki.framework.Module.Instance;
import br.com.brjdevs.miyuki.framework.Module.JDAInstance;
import br.com.brjdevs.miyuki.framework.Module.Type;
import br.com.brjdevs.miyuki.lib.data.ConfigUtils;
import br.com.brjdevs.miyuki.lib.data.ReturnHandler;
import br.com.brjdevs.miyuki.lib.data.ReturnHandler.HandlerInstance;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rethinkdb.RethinkDB;
import com.rethinkdb.ast.ReqlAst;
import com.rethinkdb.model.OptArgs;
import com.rethinkdb.net.Connection;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.User;

import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Module(id = "db", type = {Type.STATIC, Type.INSTANCE}, order = 10)
public class DBModule {
	public static final Gson
		GSON_TO_FILES = new GsonBuilder().setPrettyPrinting().serializeNulls().create(),
		GSON_INTERNAL = new GsonBuilder().serializeNulls().create();

	@Instance
	private static DBModule instance = null;

	@JDAInstance
	private static JDA jda;

	private final RethinkDB r = RethinkDB.r;
	private final ReturnHandler h = ReturnHandler.h;
	private final Connection conn;
	private final JsonObject mainConfig;

	private DBModule() {
		mainConfig = ConfigUtils.get(
			"main",
			ImmutableMap.<String, java.util.function.Predicate<JsonElement>>builder()
				.put("owners", JsonElement::isJsonArray)
				.put("token", ConfigUtils::isJsonString)
				.put("pasteeKey", ConfigUtils::isJsonString)
				.build(),
			() -> {
				JsonObject object = new JsonObject();
				object.add("owners", null);
				object.add("token", null);
				object.add("pasteeKey", null);
				return object;
			},
			false,
			true
		);

		JsonObject dbConfig = ConfigUtils.get(
			"db",
			ImmutableMap.<String, java.util.function.Predicate<JsonElement>>builder()
				.put("hostname", ConfigUtils::isJsonString)
				.put("port", element -> ConfigUtils.isJsonNumber(element) && element.getAsInt() != 0)
				.build(),
			() -> {
				JsonObject object = new JsonObject();
				object.addProperty("hostname", "localhost");
				object.addProperty("port", 28015);
				return object;
			},
			true,
			true
		);

		conn = r.connection().hostname(dbConfig.get("hostname").getAsString()).port(dbConfig.get("port").getAsInt()).db("bot").connect();
	}

	public static DBModule getInstance() {
		return instance;
	}

	public static JsonObject getConfig() {
		return getInstance().mainConfig;
	}

	public static Handler onDB(Function<RethinkDB, ReqlAst> dbConsumer) {
		return new Handler(dbConsumer.apply(instance.r));
	}

	public static Set<User> getOwners() {
		return jda.getUsers().stream().filter(user -> getOwnerIDs().contains(user.getId())).collect(Collectors.toSet());
	}

	public static Set<String> getOwnerIDs() {
		return StreamSupport.stream(getConfig().get("owners").getAsJsonArray().spliterator(), false)
			.map(JsonElement::getAsString)
			.collect(Collectors.toSet());
	}

	public static Handler onDB(Supplier<ReqlAst> dbConsumer) {
		return new Handler(dbConsumer.get());
	}

	public static Handler onDB(ReqlAst db) {
		return new Handler(db);
	}

	public static class Handler {
		private final ReqlAst run;

		private Handler(ReqlAst run) {
			this.run = run;
		}

		public HandlerInstance run() {
			return instance.h.from(run.run(instance.conn));
		}

		public HandlerInstance run(OptArgs runOpts) {
			return instance.h.from(run.run(instance.conn, runOpts));
		}

		public void noReply() {
			run.runNoReply(instance.conn);
		}

		public void noReply(OptArgs globalOpts) {
			run.runNoReply(instance.conn, globalOpts);
		}
	}
}