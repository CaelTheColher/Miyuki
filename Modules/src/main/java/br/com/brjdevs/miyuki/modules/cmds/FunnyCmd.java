package br.com.brjdevs.miyuki.modules.cmds;

import br.com.brjdevs.java.lib.IOHelper;
import br.com.brjdevs.miyuki.core.Module;
import br.com.brjdevs.miyuki.core.Module.*;
import br.com.brjdevs.miyuki.lib.Formatter;
import br.com.brjdevs.miyuki.lib.TaskManager;
import br.com.brjdevs.miyuki.modules.cmds.manager.entities.Commands;
import br.com.brjdevs.miyuki.modules.cmds.manager.entities.ICommand;
import br.com.brjdevs.miyuki.modules.cmds.utils.SessionManager;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

import static br.com.brjdevs.miyuki.lib.AsyncUtils.async;
import static br.com.brjdevs.miyuki.lib.AsyncUtils.sleep;
import static br.com.brjdevs.miyuki.lib.CollectionUtils.random;

@Module(id = "cmds.funny", name = "FunnyCommand", order = 24)
public class FunnyCmd {
	@LoggerInstance
	private static Logger logger;
	private static String[][][] SU_THEORIES;
	private static String[] TESV_GUARDS, SU_STEVONNIE, TESV_LYDIA;
	private static boolean SU_THEORIES_LOADED = false, TESV_GUARDS_LOADED = false, SU_STEVONNIE_LOADED = false, TESV_LYDIA_LOADED = false;

	@Resource("skyrim_guards.txt")
	private static String skyrim_guards_txt;

	@Resource("skyrim_lydia.txt")
	private static String skyrim_lydia_txt;

	@Resource("stevenuniverse_stevonnie.txt")
	private static String stevenuniverse_stevonnie_txt;

	@JSONResource("stevenuniverse_theories.json")
	private static JsonElement stevenuniverse_theories_json;

	@OnEnabled
	private static void enabled() {
		try {
			TESV_GUARDS = skyrim_guards_txt.split("\\r?\\n");
			TESV_GUARDS_LOADED = true;
		} catch (Exception e) {
			logger.error("Error while parsing \"skyrim_guards.txt\" resource.", e);
		}

		try {
			TESV_LYDIA = skyrim_lydia_txt.split("\\r?\\n");
			TESV_LYDIA_LOADED = true;
		} catch (Exception e) {
			logger.error("Error while parsing \"skyrim_lydia.txt\" resource.", e);
		}

		try {
			JsonObject object = stevenuniverse_theories_json.getAsJsonObject();
			List<List<List<String>>> SU_THEORIES_BUILD = Arrays.asList(Arrays.asList(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()), Arrays.asList(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
			object.get("characters").getAsJsonArray().forEach(element -> {
				SU_THEORIES_BUILD.get(0).get(0).add(element.getAsString());
				SU_THEORIES_BUILD.get(0).get(2).add(element.getAsString());
			});

			object.get("places").getAsJsonArray().forEach(element -> {
				SU_THEORIES_BUILD.get(1).get(0).add(element.getAsString());
				SU_THEORIES_BUILD.get(1).get(2).add(element.getAsString());
			});

			object.get("objects").getAsJsonArray().forEach(element -> {
				SU_THEORIES_BUILD.get(0).get(0).add(element.getAsString());
				SU_THEORIES_BUILD.get(0).get(2).add(element.getAsString());
				SU_THEORIES_BUILD.get(1).get(0).add(element.getAsString());
				SU_THEORIES_BUILD.get(1).get(2).add(element.getAsString());
			});

			object.get("gems").getAsJsonArray().forEach(element -> {
				SU_THEORIES_BUILD.get(0).get(0).add(element.getAsString());
				SU_THEORIES_BUILD.get(0).get(2).add(element.getAsString());
				SU_THEORIES_BUILD.get(1).get(0).add(element.getAsString() + "'s room");
				SU_THEORIES_BUILD.get(1).get(2).add(element.getAsString() + "'s room");
			});

			object.get("fusionGems").getAsJsonArray().forEach(element -> {
				SU_THEORIES_BUILD.get(0).get(0).add(element.getAsString());
				SU_THEORIES_BUILD.get(0).get(2).add(element.getAsString());
				SU_THEORIES_BUILD.get(1).get(0).add(element.getAsString() + "'s room");
				SU_THEORIES_BUILD.get(1).get(2).add(element.getAsString() + "'s room");
				SU_THEORIES_BUILD.get(1).get(0).add(element.getAsString() + "'s fusion realm");
				SU_THEORIES_BUILD.get(1).get(2).add(element.getAsString() + "'s fusion realm");
			});

			object.get("verb").getAsJsonArray().forEach(element -> {
				SU_THEORIES_BUILD.get(0).get(1).add(element.getAsString());
				SU_THEORIES_BUILD.get(1).get(1).add(element.getAsString());
			});
			object.get("revelation4characters").getAsJsonArray().forEach(element -> {
				SU_THEORIES_BUILD.get(0).get(2).add(element.getAsString());
			});
			object.get("revelation4places").getAsJsonArray().forEach(element -> {
				SU_THEORIES_BUILD.get(1).get(2).add(element.getAsString());
			});
			object.get("post").getAsJsonArray().forEach(element -> {
				SU_THEORIES_BUILD.get(0).get(3).add(element.getAsString());
				SU_THEORIES_BUILD.get(1).get(3).add(element.getAsString());
			});

			SU_THEORIES = SU_THEORIES_BUILD.stream().map(ll -> ll.stream().map(l -> l.stream().toArray(String[]::new)).toArray(String[][]::new)).toArray(String[][][]::new);
			SU_THEORIES_LOADED = true;
		} catch (Exception e) {
			logger.error("Error while parsing \"stevenuniverse_theories.json\" resource.", e);
		}

		try {
			SU_STEVONNIE = stevenuniverse_stevonnie_txt.split("\\r?\\n");
			SU_STEVONNIE_LOADED = true;
		} catch (Exception e) {
			logger.error("Error while parsing \"stevenuniverse_stevonnie.txt\" resource.", e);
		}
	}

	@Command("funny")
	private static ICommand createCommand() {
		return Commands.buildTree()
			.addCommand("minecraft", Commands.buildTree()
				.addCommand("drama", Commands.buildSimple("funny.minecraft.drama.usage")
					.setAction(event -> {
						int amount = SessionManager.clampIfNotOwner(SessionManager.parseInt(event.getArgs(), 1), 0, 10, event.getAuthor());
						if (amount > 1) {
							event.getAnswers().send(Formatter.italic("Pulling " + amount + " dramas... This can take a while...")).queue(message -> event.sendTyping().queue());
						}
						for (int i = 0; i < amount; i++)
							async(() -> {
								Future<String> task = TaskManager.getThreadPool().submit(() -> {
									String latestDrama = IOHelper.toString("https://drama.thog.eu/api/drama");
									if ("null".equals(latestDrama)) return "*Failed to retrieve the Drama*";
									return "**Minecrosoft**: *" + latestDrama + "*\n  *(Provided by Minecraft Drama Generator)*";
								});
								while (!task.isDone()) {
									event.awaitTyping(false).sendAwaitableTyping();
									sleep(2000);
								}
								try {
									event.awaitTyping(false).getAnswers().send(task.get()).queue();
								} catch (Exception e) {
									logger.error("An error ocurred fetching the latest Drama: ", e);
								}
							}).run();
					}).build()
				).build()
			)
			.addCommand("mc", "minecraft")
			.addCommand("stevenuniverse", Commands.buildTree()
				.addCommand("theorygenerator", Commands.buildSimple("funny.stevenuniverse.theorygenerator.usage").setAction(event -> {
					if (!SU_THEORIES_LOADED) {
						event.awaitTyping(false).getAnswers().sendTranslated("error.contentmanager").queue();
						return;
					}
					for (int i = 0, amount = SessionManager.clampIfNotOwner(SessionManager.parseInt(event.getArgs(), 1), 0, 10, event.getAuthor()); i < amount; i++) {
						String result = "";
						for (String[] theoryArray : random(SU_THEORIES))
							result = result + random(theoryArray) + " ";
						event.getAnswers().send("[#" + (i + 1) + "] What if " + result.trim() + "?").queue();
					}
				}).build())
				.addCommand("theorygen", "theorygenerator")
				.addCommand("stevonnie", Commands.buildSimple("funny.stevenuniverse.stevonnie.usage").setAction(event -> {
					if (!SU_STEVONNIE_LOADED) {
						event.awaitTyping(false).getAnswers().sendTranslated("error.contentmanager").queue();
						return;
					}
					for (int i = 0, amount = SessionManager.clampIfNotOwner(SessionManager.parseInt(event.getArgs(), 1), 0, 10, event.getAuthor()); i < amount; i++)
						event.getAnswers().send("[#" + (i + 1) + "] " + random(SU_STEVONNIE)).queue();
				}).build())
				.build()
			)
			.addCommand("su", "stevenuniverse")
			.addCommand("skyrim", Commands.buildTree()
				.addCommand("guard", Commands.buildSimple("funny.skyrim.guard.usage").setAction(event -> {
					if (!TESV_GUARDS_LOADED) {
						event.awaitTyping(false).getAnswers().sendTranslated("error.contentmanager").queue();
						return;
					}
					for (int i = 0, amount = SessionManager.clampIfNotOwner(SessionManager.parseInt(event.getArgs(), 1), 0, 10, event.getAuthor()); i < amount; i++)
						event.getAnswers().send("[#" + (i + 1) + "] " + random(TESV_GUARDS)).queue();
				}).build())
				.addCommand("lydia", Commands.buildSimple("funny.skyrim.lydia.usage").setAction(event -> {
					if (!TESV_LYDIA_LOADED) {
						event.awaitTyping(false).getAnswers().sendTranslated("error.contentmanager").queue();
						return;
					}
					for (int i = 0, amount = SessionManager.clampIfNotOwner(SessionManager.parseInt(event.getArgs(), 1), 0, 10, event.getAuthor()); i < amount; i++)
						event.getAnswers().send("[#" + (i + 1) + "] " + random(TESV_LYDIA)).queue();
				}).build())
				.build()
			)
			.build();
	}
}
