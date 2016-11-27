package br.com.brjdevs.miyuki.modules.db;

import br.com.brjdevs.miyuki.core.Module;
import br.com.brjdevs.miyuki.core.Module.JDAInstance;
import br.com.brjdevs.miyuki.core.Module.PreReady;
import br.com.brjdevs.miyuki.core.Module.Ready;
import br.com.brjdevs.miyuki.lib.StringUtils;
import br.com.brjdevs.miyuki.lib.TaskManager;
import br.com.brjdevs.miyuki.lib.data.ConfigUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rethinkdb.RethinkDB;
import com.rethinkdb.model.MapObject;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.SubscribeEvent;

import java.awt.*;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.function.Function;

@Module(id = "db.user", order = 12)
public class UserModule {
	@JDAInstance
	private static JDA jda = null;
	private static List<Data> all = new ArrayList<>();
	private static Map<User, Data> userMap = new HashMap<>();
	private static Map<Data, Integer> timeoutUntilDbRemoval = new HashMap<>();

	@PreReady
	private static void preReady() {
		TaskManager.startAsyncTask("UserTimeoutCleanup", () -> {
			timeoutUntilDbRemoval.replaceAll((guild, integer) -> Math.min(integer - 1, 0));
			timeoutUntilDbRemoval.entrySet().stream().filter(entry -> entry.getValue() == 0).map(Map.Entry::getKey).forEach(data -> {
				//TODO IMPL DB REMOVAL
				timeoutUntilDbRemoval.remove(data);
			});
		}, 60);
	}

	public static List<Data> all() {
		return Collections.unmodifiableList(all);
	}

	@Ready
	private static void loadAll() {
		DBModule.onDB(r -> r.table("users")).run().cursorExpected().forEach(UserModule::unpack);
	}

	private static Data unpack(JsonElement element) {
		JsonObject object = element.getAsJsonObject();
		Data data = all.stream().filter(dataPredicate -> object.get("id").getAsString().equals(dataPredicate.id)).findFirst().orElseGet(Data::new);
		data.id = object.get("id").getAsString();
		data.lang = ConfigUtils.isJsonString(object.get("lang")) ? object.get("lang").getAsString() : null;
		userMap.put(data.getUser(jda), data);
		if (data.getUser(jda) == null) {
			timeoutUntilDbRemoval.put(data, 5);
		}
		return data;
	}

	@SubscribeEvent
	private static void newUser(GuildMemberJoinEvent e) {
		Data data = fromDiscord(e.getMember().getUser());
		if (timeoutUntilDbRemoval.containsKey(data)) timeoutUntilDbRemoval.remove(data);
	}

	@SubscribeEvent
	private static void byeUser(GuildMemberLeaveEvent e) {
		if (e.getJDA().getGuilds().stream().anyMatch(guild -> guild != e.getGuild() && guild.isMember(e.getMember().getUser())))
			return;
		timeoutUntilDbRemoval.put(fromDiscord(e.getMember().getUser()), 5);
	}

	public static Data fromDiscord(User user) {
		if (userMap.containsKey(user)) {
			return userMap.get(user);
		} else {
			Data data = new Data();
			userMap.put(user, data);
			data.id = user.getId();

			MapObject m =
				new MapObject()
					.with("id", data.id)
					.with("lang", data.lang);

			DBModule.onDB(r -> r.table("users").insert(m)).noReply();

			return data;
		}
	}

	public static Data fromId(String id) {
		for (Data g : all) {
			if (g.id.equals(id)) return g;
		}

		return null;
	}

	public static Data fromDiscord(GuildMessageReceivedEvent event) {
		return fromDiscord(event.getAuthor());
	}
	public static MessageEmbed createEmbed(Data data, JDA jda, String language, Guild guildAt) {
		User user = data.getUser(jda);
		Member member = data.getMember(guildAt);
		if (member == null) throw new RuntimeException("User doesn't belong to the Guild");
		EmbedBuilder builder = new EmbedBuilder();
		builder.setColor(member.getColor() == null ? Color.decode("#f1c40f") : member.getColor());
		builder.setThumbnail(getAvatarUrl(user));
		builder.setTimestamp(Instant.now().atOffset(ZoneOffset.UTC));
		builder.addField(I18nModule.getLocalized("user.name", language), user.getName(), true);
		builder.addField(I18nModule.getLocalized("user.nick", language), member.getNickname() == null ? "*(" + I18nModule.getLocalized("user.none",language) + ")*" : member.getNickname(), true);
		builder.addField("ID", user.getId(), true);
		builder.addField(I18nModule.getLocalized("user.roles", language), StringUtils.notNullOrDefault(String.join(", ", member.getRoles().stream().map(Role::getName).toArray(String[]::new)), "(" + I18nModule.getLocalized("user.none", language) + ")"), true);
		builder.addField(I18nModule.getLocalized("user.memberSince", language), member.getJoinDate().format(DateTimeFormatter.RFC_1123_DATE_TIME), true);
		builder.addField(I18nModule.getLocalized("user.donator", language), "false", true);
		builder.addField(I18nModule.getLocalized("user.playing", language), (member.getGame() == null ? "*(" + I18nModule.getLocalized("user.none", language) + ")*" : member.getGame().getName()), true);
		builder.addField(I18nModule.getLocalized("user.status", language), member.getOnlineStatus().name(), true);
		return builder.build();
	}
	public static String toString(Data data, JDA jda, String language, Guild guildAt) {
		User user = data.getUser(jda);
		Member member = data.getMember(guildAt);
		if (member == null) throw new RuntimeException("User doesn't belong to the Guild.");
		return I18nModule.getLocalized("user.name", language) + ": " + user.getName() + "\n" +
			I18nModule.getLocalized("user.nick", language) + ": " + (member.getNickname() == null ? "(" + I18nModule.getLocalized("user.none", language) + ")" : member.getNickname()) + "\n" +
			I18nModule.getLocalized("user.roles", language) + ": " + StringUtils.notNullOrDefault(String.join(", ", member.getRoles().stream().map(Role::getName).toArray(String[]::new)), "(" + I18nModule.getLocalized("user.none", language) + ")") + "\n" +
			I18nModule.getLocalized("user.memberSince", language) + ": " + member.getJoinDate().format(DateTimeFormatter.RFC_1123_DATE_TIME) + "\n" +
			I18nModule.getLocalized("user.commonGuildModule", language) + ": " + StringUtils.notNullOrDefault(String.join(", ", jda.getGuilds().stream().filter(guild -> guild.isMember(user)).map(Guild::getName).toArray(String[]::new)), "(" + I18nModule.getLocalized("user.none", language) + ")") + "\n" +
			"ID: " + user.getId() + "\n" +
			I18nModule.getLocalized("user.status", language) + ": " + member.getOnlineStatus() + "\n" +
			I18nModule.getLocalized("user.playing", language) + ": " + (member.getGame() == null ? "(" + I18nModule.getLocalized("user.none", language) + ")" : member.getGame().getName());
	}
	public static String getAvatarUrl(User user) {
		return user.getAvatarUrl() == null ? user.getDefaultAvatarUrl() : user.getAvatarUrl();
	}
	public static class Data {
		private String id = "-1", lang = null;

		private static void pushUpdate(Data data, Function<RethinkDB, MapObject> changes) {
			DBModule.onDB(r -> r.table("users").get(data.id).update(changes.apply(r))).noReply();
		}

		public String getId() {
			return id;
		}

		public String getLang() {
			return lang;
		}

		public void setLang(String lang) {
			if (lang.isEmpty()) lang = null;
			this.lang = lang;
			pushUpdate(this, r -> r.hashMap("lang", this.lang));
		}

		public long getUserPerms(GuildModule.Data data) {
			return data.getUserPerms(id);
		}

		public long getUserPerms(GuildModule.Data data, long orDefault) {
			return data.getUserPerms(id, orDefault);
		}

		public void setUserPerms(GuildModule.Data data, long userPerms) {
			data.setUserPerms(id, userPerms);
		}

		public long getUserPerms(Guild guild) {
			return getUserPerms(GuildModule.fromDiscord(guild));
		}

		public long getUserPerms(Guild guild, long orDefault) {
			return getUserPerms(GuildModule.fromDiscord(guild), orDefault);
		}

		public void setUserPerms(Guild guild, long userPerms) {
			setUserPerms(GuildModule.fromDiscord(guild), userPerms);
		}

		public User getUser(JDA jda) {
			return jda.getUserById(id);
		}

		public Member getMember(GuildModule.Data data, JDA jda) {
			return getMember(data.getGuild(jda));
		}

		public Member getMember(Guild guild) {
			return guild == null ? null : guild.getMember(getUser(guild.getJDA()));
		}
	}
}