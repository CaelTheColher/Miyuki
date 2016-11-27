package br.com.brjdevs.miyuki.modules.db;

import br.com.brjdevs.miyuki.core.Module;
import br.com.brjdevs.miyuki.core.Module.OnEnabled;
import br.com.brjdevs.miyuki.lib.CollectionUtils;
import br.com.brjdevs.miyuki.lib.TaskManager;
import br.com.brjdevs.miyuki.lib.data.Commitable;
import br.com.brjdevs.miyuki.modules.cmds.manager.PermissionsModule;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rethinkdb.RethinkDB;
import com.rethinkdb.model.MapObject;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.guild.update.GuildUpdateNameEvent;
import net.dv8tion.jda.core.hooks.SubscribeEvent;

import java.awt.*;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Module(id = "db.guild", isListener = true, order = 13)
public class GuildModule {
	public static final String[] DEFAULT_PREFIXES = {"&", "?"};
	public static Data GLOBAL;
	private static List<Data> all = new ArrayList<>();
	private static Map<Guild, Data> guildMap = new HashMap<Guild, Data>() {
		@Override
		public Data put(Guild key, Data value) {
			if (key == null) return null;
			return super.put(key, value);
		}

		@Override
		public void putAll(Map<? extends Guild, ? extends Data> m) {
			m.remove(null);
			super.putAll(m);
		}

		@Override
		public Data putIfAbsent(Guild key, Data value) {
			if (key == null) return null;
			return super.putIfAbsent(key, value);
		}
	};
	private static Map<Data, Integer> timeoutUntilDbRemoval = new HashMap<>();

	@OnEnabled
	private static void load() {
		//FakeGuilds Impl
		GLOBAL = new Data();
		GLOBAL.id = "-1";
		GLOBAL.name = "GLOBAL";

		TaskManager.startAsyncTask("GuildTimeoutCleanup", () -> {
			timeoutUntilDbRemoval.replaceAll((guild, integer) -> Math.min(integer - 1, 0));
			timeoutUntilDbRemoval.entrySet().stream().filter(entry -> entry.getValue() == 0).map(Map.Entry::getKey).forEach(data -> {
				DBModule.onDB(r -> r.table("commands").filter(r.row("gid").eq(data.id)).delete()).noReply();
				DBModule.onDB(r -> r.table("guilds").get(data.id).delete()).noReply();
				timeoutUntilDbRemoval.remove(data);
			});
		}, 60);
	}

	public static List<Data> all() {
		return Collections.unmodifiableList(all);
	}

	private static Data unpack(JsonElement element) {
		JsonObject object = element.getAsJsonObject();
		Data data = all.stream().filter(dataPredicate -> object.get("id").getAsString().equals(dataPredicate.id)).findFirst().orElseGet(Data::new);
		data.id = object.get("id").getAsString();
		data.name = object.get("name").getAsString();
		data.cmdPrefixes.clear();
		object.get("cmdPrefixes").getAsJsonArray().forEach(jsonElement -> data.cmdPrefixes.add(jsonElement.getAsString()));
		data.lang = object.get("lang").getAsString();
		data.flags.clear();
		object.get("flags").getAsJsonObject().entrySet().forEach(entry -> data.flags.put(entry.getKey(), entry.getValue().getAsBoolean()));
		object.get("userPerms").getAsJsonObject().entrySet().forEach(entry -> data.userPerms.put(entry.getKey(), entry.getValue().getAsLong()));
		return data;
	}

	@SubscribeEvent
	private static void newGuild(GuildJoinEvent e) {
		Data data = fromDiscord(e.getGuild());
		if (timeoutUntilDbRemoval.containsKey(data)) timeoutUntilDbRemoval.remove(data);
	}

	@SubscribeEvent
	private static void byeGuild(GuildLeaveEvent e) {
		timeoutUntilDbRemoval.put(fromDiscord(e.getGuild()), 5);
	}

	@SubscribeEvent
	private static void renamedGuild(GuildUpdateNameEvent e) {
		fromDiscord(e.getGuild()).setName(toGuildName(e.getGuild().getName()));
	}

	public static Data fromDiscord(Guild guild) {
		if (guild == null) return GLOBAL;
		if (guildMap.containsKey(guild)) {
			return guildMap.get(guild);
		} else {
			return getOrGen(Optional.of(guild), Optional.empty(), Optional.empty());
		}
	}

	private static Data getOrGen(Optional<Guild> guildOptional, Optional<String> optionalId, Optional<String> optionalName) {
		RuntimeException ex = new IllegalStateException("Id and/or Name can't be Optional if Guild isn't returned");
		String id = optionalId.orElseGet(() -> guildOptional.orElseThrow(() -> ex).getId());
		String name = optionalName.orElseGet(() -> guildOptional.orElseThrow(() -> ex).getName());

		Data data;

		JsonElement object = DBModule.onDB(r -> r.db("bot").table("guilds").get(id)).run().simpleExpected();
		if (object.isJsonNull()) {
			data = new Data();

			data.id = id;
			data.name = toGuildName(name);

			MapObject m =
				new MapObject()
					.with("id", data.id)
					.with("name", data.name)
					.with("cmdPrefixes", data.cmdPrefixes)
					.with("lang", data.lang)
					.with("userPerms", data.userPerms)
					.with("flags", data.flags);

			DBModule.onDB(r -> r.table("guilds").insert(m)).noReply();
		} else {
			data = unpack(object);
		}
		Data finalData = data;
		guildOptional.ifPresent(guild -> guildMap.put(guild, finalData));
		UserCommandsModule.loadAllFrom(data);
		return data;
	}

	public static Data fromName(String name) {
		for (Data g : all) {
			if (g.name.equals(name)) return g;
		}
		return null;
	}

	private static String toGuildName(String name) {
		name = name.replace(" ", "_").replace(":", "");
		if (fromName(name) == null) return name;
		for (int i = 2; i < 1000; i++) {
			if (fromName(name + i) == null) return name + i;
		}
		throw new RuntimeException("What. the. fuck.");
	}

	public static MessageEmbed createEmbed(Data data, JDA jda, String lang) {
		Guild guild = data.getGuild(jda);
		EmbedBuilder builder = new EmbedBuilder();
		if (guild != null)
			builder.setThumbnail(guild.getIconUrl());
		builder.setColor(guild == null ? Color.decode("#f1c40f") : guild.getOwner().getColor() == null ? Color.decode("#f1c40f") : guild.getOwner().getColor());
		builder.setTimestamp(Instant.now().atOffset(ZoneOffset.UTC));
		builder.addField(I18nModule.getLocalized("guild.guild", lang), data.name + (guild != null && !data.name.equals(guild.getName()) ? " \n(" + guild.getName() + ")" : ""), true)
				.addField("VIP", data.getFlag("vip") + "", true)
				.addField(I18nModule.getLocalized("guild.owner", lang), (guild == null ? CollectionUtils.toString(DBModule.getOwners(), User::getName, ", ") : guild.getOwner().getUser().getName()), true)
				.addField(I18nModule.getLocalized("guild.cmds", lang), UserCommandsModule.allFrom(data).size() + "", true)
				.addField(I18nModule.getLocalized("guild.channels", lang), (guild == null ? (jda.getTextChannels().size() + jda.getPrivateChannels().size()) : guild.getTextChannels().size()) + "", true)
				.addField(I18nModule.getLocalized("guild.users", lang), (guild == null ? jda.getUsers().size() : guild.getMembers().size()) + "", true)
				.addField(I18nModule.getLocalized("guild.id", lang), data.id, true)
				.addField(I18nModule.getLocalized("guild.emotes.count", lang), (guild == null ? jda.getEmotes().size() : guild.getEmotes().size()) + "", true);
		if (guild != null && !guild.getEmotes().isEmpty())
			builder.addField(I18nModule.getLocalized("guild.emotes", lang), (String.join(" ", guild.getEmotes().stream().map(Emote::getAsMention).collect(Collectors.toList()))), false);
        if (guild == null)
            builder.addField("Exists", "false", true);
		return builder.build();
	}

	public static class Data {
		private Map<String, Long> userPerms = new HashMap<>();
		private Map<String, Boolean> flags = new HashMap<>();
		private String id = "-1", name = "", lang = "en_US";
		private List<String> cmdPrefixes = new ArrayList<>(Arrays.asList(DEFAULT_PREFIXES));

		private Data() {
			flags.put("cleanup", false);
			flags.put("vip", true);
			userPerms.put("default", PermissionsModule.BASE_USER);
			all.add(this);
		}

		private static void pushUpdate(Data data, Function<RethinkDB, MapObject> changes) {
			DBModule.onDB(r -> r.table("guilds").get(data.id).update(arg -> changes.apply(r))).noReply();
		}

		public List<String> getCmdPrefixes() {
			return Collections.unmodifiableList(cmdPrefixes);
		}

		public Commitable<List<String>> modifyCmdPrefixes() {
			int oldHash = Arrays.hashCode(cmdPrefixes.toArray());
			return Commitable.bake(new ArrayList<>(cmdPrefixes), list -> {
				if (Arrays.hashCode(list.toArray()) != oldHash) {
					pushUpdate(this, r -> r.hashMap("cmdPrefixes", list));
					cmdPrefixes.clear();
					cmdPrefixes.addAll(list);
				}
			});
		}

		public long getUserPerms(String s) {
			return getUserPerms(s, 0L);
		}

		public long getUserPerms(String s, long orDefault) {
			return userPerms.getOrDefault(s, orDefault);
		}

		public void setUserPerms(String s, long userPerms) {
			this.userPerms.put(s, userPerms);
			pushUpdate(this, r -> r.hashMap("userPerms", this.userPerms));
		}

		public boolean getFlag(String flag) {
			return flags.getOrDefault(flag, false);
		}

		public void setFlag(String flag, Boolean value) {
			this.flags.put(flag, value);
			pushUpdate(this, r -> r.hashMap("flags", flags));
		}

		public boolean toggleFlag(String s) {
			boolean f = !getFlag(s);
			setFlag(s, f);
			return f;
		}

		public Guild getGuild(JDA jda) {
			return jda.getGuildById(id);
		}

		public String getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
			pushUpdate(this, r -> r.hashMap("name", name));
		}

		public String getLang() {
			return lang;
		}

		public void setLang(String lang) {
			this.lang = lang;
			pushUpdate(this, r -> r.hashMap("lang", lang));
		}
	}
}
