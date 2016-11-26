package br.com.brjdevs.miyuki.modules.db;

import br.com.brjdevs.miyuki.core.Module;
import br.com.brjdevs.miyuki.core.Module.*;
import br.com.brjdevs.miyuki.core.commands.CommandEvent;
import br.com.brjdevs.miyuki.core.commands.Commands;
import br.com.brjdevs.miyuki.core.commands.ICommand;
import br.com.brjdevs.miyuki.core.entities.ModuleResourceManager;
import br.com.brjdevs.miyuki.modules.cmds.PushCmd;
import br.com.brjdevs.miyuki.modules.cmds.manager.PermissionsModule;
import com.google.gson.JsonObject;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.*;
import java.util.regex.Pattern;

import static br.com.brjdevs.miyuki.lib.AsyncUtils.asyncSleepThen;
import static br.com.brjdevs.miyuki.lib.CollectionUtils.iterate;
import static br.com.brjdevs.miyuki.lib.StringUtils.advancedSplitArgs;
import static br.com.brjdevs.miyuki.lib.StringUtils.notNullOrDefault;
import static br.com.brjdevs.miyuki.lib.data.DBUtils.decode;
import static br.com.brjdevs.miyuki.lib.data.DBUtils.encode;
import static br.com.brjdevs.miyuki.modules.db.DBModule.onDB;
import static com.rethinkdb.RethinkDB.r;

@Module(id = "i18n", order = 11)
public class I18nModule {
	private static final Pattern compiledPattern = Pattern.compile("\\$\\([A-Za-z.]+?\\)");
	@ResourceManager
	private static ModuleResourceManager manager;
	@Resource("main.json")
	private static String i18nMain = "";
	@JDAInstance
	private static JDA jda = null;
	private static List<String> syncedLocalizations = new ArrayList<>(), moderated = new ArrayList<>();
	private static Map<String, Map<String, String>> locales = new HashMap<>();
	private static Map<String, String> parents = new HashMap<>();
	private static Map<String, Suggestion> suggestions = new HashMap<>();

	public static void acceptSuggestion(String key) {
		if (!suggestions.containsKey(key)) throw new IllegalStateException("Key does not exist");
		suggestions.remove(key).accept();
	}

	public static void reject(String key) {
		if (!suggestions.containsKey(key)) throw new IllegalStateException("Key does not exist");
		suggestions.remove(key).reject();
	}

	@PreReady
	private static void load() {
		onDB(r.table("i18n")).run().cursorExpected().forEach(element -> {
			JsonObject object = element.getAsJsonObject();
			if (!object.has("id") || !object.has("value")) return;
			String[] id = object.get("id").getAsString().split(":", 2);
			if (id.length != 2) return;
			setLocalTranslation(id[0], id[1], decode(object.get("value").getAsString()));

			if (object.has("moderated")) setModerated(id[0], id[1], object.get("moderated").getAsBoolean());
		});
	}

	@PostReady
	private static void postReady() {
		localizeLocal("botname", jda.getSelfUser().getName());
		localizeLocal("mention", jda.getSelfUser().getAsMention());
	}

	@Command("i18n") //TODO Escrito nos Comentários
	private static ICommand generateCommand() {
		return Commands.buildTree(PermissionsModule.BOT_ADMIN)
			.addCommand("suggest", Commands.buildSimple("i18n.suggest.usage")
				.setAction(event -> {
					String[] values = advancedSplitArgs(event.getArgs(), 3);
					String unlocalized = values[0], locale = values[1], value = values[2];
					if (unlocalized.isEmpty() || locale.isEmpty() || value.isEmpty()) {
						event.awaitTyping(true).getAnswers().invalidargs();
						return;
					}

					if (unlocalized.startsWith("!")) unlocalized = unlocalized.substring(1);

					if (value.length() > 7 && value.startsWith("base64:")) value = decode(value.substring(7));

					if (moderated.contains(unlocalized + ":" + locale)) {
						//TODO Mensagem fofa
						return;
					}

					//TODO Create I18n bans and check for ban

					Suggestion suggestion = new Suggestion(unlocalized, locale, value, event.getAuthor());

					String suggestMiniMd5 = DigestUtils.md5Hex(suggestion.toString().getBytes()).substring(0, 5);

					suggestions.put(suggestMiniMd5, suggestion);

					event.awaitTyping(true).getAnswers().bool(true).queue();

					PushCmd.pushSimple(
						"i18n",
						"User " + event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator() +
							" suggests the translation `" + unlocalized + "` in locale `" + locale + "` to:\n```\n" + value + "\n```\n" +
							"To accept: `bot mod i18n accept " + suggestMiniMd5 + "`\n" +
							"To reject: `bot mod i18n reject " + suggestMiniMd5 + "`"
					);
				})
				.build()
			)
			.addCommand("localize", Commands.buildSimple()
				.setAction(event -> {
					String[] values = advancedSplitArgs(event.getArgs(), 3);
					String unlocalized = values[0], locale = values[1], value = values[2];
					if (unlocalized.isEmpty() || locale.isEmpty() || value.isEmpty()) {
						event.awaitTyping(true).getAnswers().invalidargs();
						return;
					}

					if (value.length() > 7 && value.startsWith("base64:")) value = decode(value.substring(7));

					boolean moderated = unlocalized.startsWith("!");
					if (moderated) unlocalized = unlocalized.substring(1);
					pushTranslation(unlocalized, locale, value);
					setModerated(unlocalized, locale, moderated);
					event.awaitTyping(true).getAnswers().bool(true).queue();
				})
				.build()
			)
			.addCommand("suggestCommand", Commands.buildSimple()
				// Args: <command> <locale> <[base64:]desc> <params> <info>
				// Esse comando deverá funcionar igual ao suggest
				// Mas com os argumentos do localizeCommand
				.build()
			)
			.addCommand("localizeCommand", Commands.buildSimple()
				// <command> <locale> <[base64:]desc> <[base64:]params> <[base64:]info>
				// paramsMeta, noParams, noDesc <= localized(%s,locale)
				// localize(genCmdUsage())
				// return bool(true)
				.setAction(event -> {
					String[] values = advancedSplitArgs(event.getArgs(), 5);
					String unlocalized = values[0], locale = values[1], desc = values[2], params = values[3], info = values[4];
					if (unlocalized.isEmpty() || locale.isEmpty()) {
						event.awaitTyping(true).getAnswers().invalidargs();
						return;
					}

					if (desc.length() > 7 && desc.startsWith("base64:")) desc = decode(desc.substring(7));
					if (params.length() > 7 && params.startsWith("base64:")) params = decode(params.substring(7));
					if (info.length() > 7 && info.startsWith("base64:")) info = decode(info.substring(7));

					boolean moderated = unlocalized.startsWith("!");
					if (moderated) unlocalized = unlocalized.substring(1);

					pushTranslation(
						unlocalized, locale,
						genCmdUsage(
							desc, params, info,
							getLocalized("meta.noDesc", locale),
							getLocalized("meta.noParams", locale),
							getLocalized("meta.paramsMeta", locale)
						)
					);

					setModerated(unlocalized, locale, moderated);
					event.awaitTyping(true).getAnswers().bool(true).queue();
				})
				.build()
			)
			.addCommand("remove", Commands.buildSimple()
				// Args: <unlocalized> <locale|"all">
				// Apaga a tradução
				.build()
			)
			.addCommand("suggestRemove", Commands.buildSimple()
				// Args: <unlocalized> <locale|"all">
				// Esse comando deverá funcionar igual ao suggest
				// Mas com os argumentos do remove
				.build()
			)
			.addCommand("list", Commands.buildSimple()
				// Args: [page]
				// TODO-ADRIAN Gerar a lista
				// TODO-STEVEN Sistema de Lista baseado em page
				// Lista as traduções
				.build()
			)
			.addCommand("about", Commands.buildSimple()
				// NoArgs
				// Explicar o sistema de traduções
				.build()
			)
			.build();
	}

	private static String genCmdUsage(String desc, String params, String info, String noDesc, String noParams, String paramsMeta) {
		desc = desc != null ? desc : noDesc != null ? noDesc : "null";
		params = params != null ? params : noParams != null ? noParams : "null";
		info = info != null ? "\n  " + info.replace("\n", "\n  ") : "";
		return desc + "\n" + (paramsMeta != null ? paramsMeta : "null") + ": " + params + info;
	}

	private static void localize(String lang, String untranslated, String translated) {
		pushTranslation(untranslated, lang, translated);
		setModerated(untranslated, lang, true);
	}

	private static void localizeLocal(String untranslated, String translated) {
		setLocalTranslation("dynamic." + untranslated, "en_US", translated);
		setModerated("dynamic." + untranslated, "en_US", true);
	}

//	public static String generateJsonDump() {
//		System.out.println();
//		JsonObject json = new JsonObject();
//		JsonObject parentsJson = new JsonObject();
//		JsonObject localizations = new JsonObject();
//
//		parents.forEach(parentsJson::addProperty);
//		locales.forEach((k, v) -> {
//			JsonObject localization = new JsonObject();
//			v.forEach(localization::addProperty);
//			localizations.add(k, localization);
//		});
//
//		json.add("parents", parentsJson);
//		json.add("localizations", localizations);
//		return Pastee.post(json.toString());
//	}

	public static void pushTranslation(String unlocalized, String locale, String localized) {
		String localeId = unlocalized + ":" + locale;
		if (syncedLocalizations.contains(localeId)) {
			onDB(r.table("i18n").get(localeId).update(arg -> r.hashMap("value", encode(localized)))).noReply();
		} else {
			onDB(r.table("i18n").insert(r.hashMap("id", localeId).with("value", encode(localized)).with("moderated", moderated.contains(localeId)))).noReply();
			syncedLocalizations.add(localeId);
		}

		setLocalTranslation(unlocalized, locale, localized);
	}

	public static void setModerated(String unlocalized, String locale, boolean flag) {
		String localeId = unlocalized + ":" + locale;

		if (flag && !moderated.contains(localeId)) {
			moderated.add(localeId);
		} else if (!flag && moderated.contains(localeId)) {
			moderated.remove(localeId);
		}

		if (syncedLocalizations.contains(localeId)) {
			onDB(r.table("i18n").get(localeId).update(arg -> r.hashMap("moderated", flag))).noReply();
		}
	}

	public static void setLocalTranslation(String unlocalized, String locale, String localized) {
		if (!locales.containsKey(unlocalized)) locales.put(unlocalized, new HashMap<>());
		locales.get(unlocalized).put(locale, localized);
	}

	public static String getLocale(CommandEvent event) {
		return notNullOrDefault(UserModule.fromDiscord(event.getAuthor()).getLang(), event.getGuild().getLang());
	}

	public static void setParent(String locale, String parent) {
		parents.put(locale, parent);
	}

	public static String getLocalized(String unlocalized, String locale) {
		return dynamicTranslate(getBaseLocalized(unlocalized, locale), locale, null);
	}

	public static String dynamicTranslate(String string, String locale, Optional<Map<String, String>> dynamicMap) {
		if (dynamicMap == null) dynamicMap = Optional.empty();
		if (!string.contains("$(")) return string;

		Set<String> skipIfIterated = new HashSet<>();
		for (String key : iterate(compiledPattern.matcher(string))) {
			if (skipIfIterated.contains(key)) continue;
			String unlocalizedKey = key.substring(2, key.length() - 1);

			if (dynamicMap.isPresent()) {
				string = string.replace(key, Optional.ofNullable(dynamicMap.get().get(unlocalizedKey)).orElseGet(() -> getLocalized(unlocalizedKey, locale)));
			} else {
				string = string.replace(key, getLocalized(unlocalizedKey, locale));
			}

			if (!string.contains("$(")) break;
			skipIfIterated.add(key);
		}

		return string;
	}

	public static String getLocalized(String unlocalized, CommandEvent event) {
		return getLocalized(unlocalized, getLocale(event));
	}

	public static String getLocalized(String unlocalized, TextChannel channel) {
		return getLocalized(unlocalized, GuildModule.fromDiscord(channel.getGuild()).getLang());
	}

	private static String getBaseLocalized(final String unlocalized, final String locale) {
		String unlocalizing = unlocalized, localed = locale, localized = unlocalizing;
		Map<String, String> currentLocales = locales.get(unlocalizing);
		while (unlocalizing.equals(localized) && localed != null) {
			localized = currentLocales != null ? currentLocales.getOrDefault(localed, unlocalizing) : unlocalizing;
			if (unlocalizing.equals(localized)) localed = parents.get(localed);
			else if (localized.length() > 1 && localized.startsWith("$$=") && localized.endsWith(";")) { //This won't change the parent
				localized = localized.substring(3, localized.length() - 1); //Substring localized
				if (unlocalizing.equals(localized)) {//unlocalized = localized -> LOOP
					break;
				} else {
					unlocalizing = localized;
					currentLocales = locales.get(unlocalizing);
				}
			}
		}

		if (unlocalizing.equals(localized) || localed == null) {
			asyncSleepThen(1000, () -> PushCmd.pushSimple("i18n", channel -> "I18nModule Warn: Detected an untranslated String: " + unlocalized + ":" + locale)).run();
		}

		return localized;
	}

	private static class Suggestion {
		private final String unlocalized;
		private final String locale;
		private final String value;
		private final User author;

		public Suggestion(String unlocalized, String locale, String value, User author) {
			this.unlocalized = unlocalized;
			this.locale = locale;
			this.value = value;
			this.author = author;
		}

		public void accept() {
			pushTranslation(unlocalized, locale, value);
			//TODO GIB XP (PROFILES)
			suggestions.values().removeIf(this::equals);
		}

		public void reject() {
			//TODO TAKE XP (PROFILES)
			suggestions.values().removeIf(this::equals);
		}

		@Override
		public String toString() {
			return author.toString() + "(" + unlocalized + ":" + locale + "=" + value;
		}
	}
}
