package br.com.brjdevs.miyuki.modules.cmds;

import br.com.brjdevs.miyuki.commands.Commands;
import br.com.brjdevs.miyuki.commands.Holder;
import br.com.brjdevs.miyuki.commands.ICommand;
import br.com.brjdevs.miyuki.loader.Module;
import br.com.brjdevs.miyuki.loader.Module.*;
import br.com.brjdevs.miyuki.modules.cmds.manager.PermissionsModule;
import br.com.brjdevs.miyuki.modules.db.GuildModule;
import br.com.brjdevs.miyuki.modules.db.I18nModule;
import br.com.brjdevs.miyuki.utils.Log4jUtils;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static br.com.brjdevs.miyuki.modules.db.DBModule.onDB;
import static com.rethinkdb.RethinkDB.r;

@Module(name = "cmds.push")
public class PushCmd {
	@LoggerInstance
	private static Logger logger;
	@JDAInstance
	private static JDA jda;
	private static Map<String, String> pushParenting = new HashMap<>();
	private static Map<Supplier<Set<String>>, String> dynamicParenting = Collections.synchronizedMap(new HashMap<>());
	private static SetMultimap<TextChannel, String> subscriptions = MultimapBuilder.hashKeys().hashSetValues().build();

	@OnEnabled
	private static void enabled() {
		pushParenting.put("*", null);
		registerType("bot", "*");
		registerType("stop", "bot");
		registerType("start", "bot");
		registerType("update", "*");
		registerType("changelog", "update");
		registerType("log", "*");
		registerType("owner", "*");
		registerType("guild", "*");
		registerType("i18n", "*");
		registerType("feeds", "*");

		registerDynamicTypes(() -> jda.getGuilds().stream().map(guild -> "guild_" + GuildModule.fromDiscord(guild).getName()).collect(Collectors.toSet()), "guild");
	}

	@Ready
	private static void ready() {
		onDB(r.table("pushSubs")).run().cursorExpected().forEach(json -> {
			JsonObject subscription = json.getAsJsonObject();
			TextChannel channel = jda.getTextChannelById(subscription.get("id").getAsString());

			if (channel == null) {
				onDB(r.table("pushSubs").get(subscription.get("id").getAsString()).delete()).noReply();
				return;
			}

			subscriptions.putAll(channel, StreamSupport.stream(subscription.get("types").getAsJsonArray().spliterator(), false).map(JsonElement::getAsString).collect(Collectors.toSet()));
		});
	}

	public static boolean subscribe(TextChannel channel, Set<String> typesToAdd) {
		typesToAdd = new HashSet<>(typesToAdd);
		Set<String> valid = resolveTypeSet();
		typesToAdd.removeIf(s -> !valid.contains(s));
		typesToAdd.remove(null);

		if (typesToAdd.size() == 0) return false;

		if (subscriptions.containsKey(channel)) {
			Set<String> currentSubs = subscriptions.get(channel);
			int size = currentSubs.size();
			currentSubs.addAll(typesToAdd);

			if (currentSubs.size() == size) return false;

			onDB(r.table("pushSubs").get(channel.getId()).update(arg -> r.hashMap("types", new ArrayList<>(currentSubs)))).noReply();
		} else {
			onDB(r.table("pushSubs").insert(r.hashMap("id", channel.getId()).with("types", new ArrayList<>(typesToAdd)))).noReply();
			subscriptions.putAll(channel, typesToAdd);
		}
		return true;
	}

	public static boolean unsubscribe(TextChannel channel, Set<String> typesToRemove) {
		if (!subscriptions.containsKey(channel)) return false;

		Set<String> currentSubs = subscriptions.get(channel);
		int size = currentSubs.size();
		currentSubs.removeAll(typesToRemove);

		if (currentSubs.size() == size) return false;

		if (currentSubs.size() > 0) {
			onDB(r.table("pushSubs").get(channel.getId()).update(arg -> r.hashMap("types", new ArrayList<>(currentSubs)))).noReply();
		} else {
			onDB(r.table("pushSubs").get(channel.getId()).delete()).noReply();
			subscriptions.removeAll(channel);
		}

		return true;
	}

	public static void unsubscribeAll(Set<String> typesToRemove) {
		jda.getTextChannels().parallelStream().forEach(c -> unsubscribe(c, typesToRemove));
	}

	public static void subscribeAll(Set<String> typesToRemove) {
		jda.getTextChannels().parallelStream().forEach(c -> subscribe(c, typesToRemove));
	}

	public static void registerType(String type, String parent) {
		pushParenting.put(type.toLowerCase(), parent.toLowerCase());
	}

	public static void registerDynamicTypes(Supplier<Set<String>> supplier, String parent) {
		dynamicParenting.put(supplier, parent.toLowerCase());
	}

	public static Set<String> subscriptionsFor(TextChannel channel) {
		return Collections.unmodifiableSet(subscriptions.get(channel));
	}

	public static void pushMessage(String type, Function<TextChannel, Message> pushSupplier) {
		resolveTextChannels(type).forEach(channel -> channel.sendMessage(pushSupplier.apply(channel)).queue());
	}

	public static void pushSimple(String type, Function<TextChannel, String> pushSupplier) {
		pushMessage(type, channel -> new MessageBuilder().appendString(pushSupplier.apply(channel)).build());
	}

	public static void pushSimple(String type, String pushMessage) {
		pushMessage(type, channel -> new MessageBuilder().appendString(pushMessage).build());
	}

	public static Set<TextChannel> resolveTextChannels(String type) {
		Set<String> appliable = resolve(type);
		return subscriptions.asMap().entrySet().stream()
			.filter(entry -> entry.getValue().stream().anyMatch(appliable::contains))
			.map(Map.Entry::getKey)
			.collect(Collectors.toSet());
	}

	public static Map<String, String> resolveTypeMap() {
		Map<String, String> resolvedMap = new HashMap<>(pushParenting);
		dynamicParenting.forEach((supplier, s) -> {
			try {
				supplier.get().forEach(s1 -> resolvedMap.put(s1, s));
			} catch (Exception e) {
				Log4jUtils.logger().error("Error while resolving dynamic type: ", e);
			}
		});
		return resolvedMap;
	}

	public static Set<String> resolveTypeSet() {
		Map<String, String> resolvedMap = resolveTypeMap();
		Set<String> set = new HashSet<>();
		set.addAll(resolvedMap.values());
		set.addAll(resolvedMap.keySet());
		set.remove(null);
		return set;
	}

	public static Set<String> resolve(String type) {
		Map<String, String> resolvedMap = resolveTypeMap();

		Set<String> r = new HashSet<>();

		do {
			r.add(type);
			type = resolvedMap.getOrDefault(type, "*");
			if (type == null) type = "*";
		} while (!"*".equals(type));

		return r;
	}

	@Command("push")
	private static ICommand createCommand() {
		return Commands.buildTree()
			.addCommand("subscribe", Commands.buildSimple("push.subscribe.usage", PermissionsModule.MANAGE_PUSH)
				.setAction(event -> {
					Set<String> args = new HashSet<>();
					Collections.addAll(args, event.getArgs(0));
					event.awaitTyping(false).getAnswers().bool(subscribe(event.getChannel(), args)).queue();
				})
				.build()
			)
			.addCommand("unsubscribe", Commands.buildSimple("push.unsubscribe.usage", PermissionsModule.MANAGE_PUSH)
				.setAction(event -> {
					Set<String> args = new HashSet<>();
					Collections.addAll(args, event.getArgs(0));
					event.awaitTyping(false).getAnswers().bool(unsubscribe(event.getChannel(), args)).queue();
				})
				.build()
			)
			.addCommand("send", Commands.buildSimple("push.send.usage", PermissionsModule.BOT_ADMIN)
				.setAction(event -> {
					pushSimple(event.getArg(2, 0), (channel) -> event.getArg(2, 1));
					event.awaitTyping(false).getAnswers().bool(true).queue();
				})
				.build()
			)
			.addCommand("list", Commands.buildSimple("push.list.usage")
				.setAction(event -> {
					Set<String> subscribed = new TreeSet<>(subscriptionsFor(event.getChannel())), all = new TreeSet<>(resolveTypeSet());
					Holder<StringBuilder> b = new Holder<>(new StringBuilder().append("**").append(I18nModule.getLocalized("push.list", event)).append(":**\n "));
					Holder<Boolean> first = new Holder<>(true);
					first.var = true;
					all.forEach(s -> {
						if (subscribed.contains(s)) s = "**" + s + "**";
						if ("*****".equals(s)) s = "*";

						if (first.var) {
							first.var = false;
							b.var.append(s);
						} else {
							String a = " " + s;
							if (b.var.length() + a.length() >= 1999) {
								event.awaitTyping(false).getAnswers().send(b.var.toString()).queue();
								b.var = new StringBuilder();
							}
							b.var.append(a);
						}
					});
					if (first.var) b.var.append("(").append(I18nModule.getLocalized("push.none", event)).append(")");
					event.awaitTyping(false).getAnswers().send(b.var.toString()).queue();
				})
				.build()
			)
			.addDefault("list")
			.build();
	}
}
