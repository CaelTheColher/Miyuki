package br.com.brjdevs.miyuki.modules.cmds.utils.scripting;

import br.com.brjdevs.miyuki.core.commands.CommandEvent;
import br.com.brjdevs.miyuki.modules.db.I18nModule;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class JS {
	private static final ScriptEngine engine, unsafeEngine;
	public static final Evaluator JS_EVALUATOR = JS::eval, JS_UNSAFE_EVALUATOR = JS::unsafeEval;

	static {

		Evaluator.EVALUATOR_REGISTER.put("JS", JS_EVALUATOR);
		Evaluator.EVALUATOR_REGISTER.put("JS_UNSAFE", JS_UNSAFE_EVALUATOR);
		engine = new ScriptEngineManager().getEngineByName("nashorn");
		unsafeEngine = new ScriptEngineManager().getEngineByName("nashorn");
		try {
			engine.eval("var imports = new JavaImporter(java.io, java.lang, java.util);");
		} catch (ScriptException e) {
			e.printStackTrace();
		}
	}

	public static void eval(CommandEvent event) {
		try {
			engine.put("event", event);
			Object out = engine.eval(
				"(function() {" +
					"with (imports) {" +
					event.getArgs() +
					"}" +
					"})();");
			event.awaitTyping(false).getAnswers().send(out == null ? I18nModule.getLocalized("bot.eval.noOut", event) : out.toString()).queue();
		} catch (ScriptException e) {
			event.awaitTyping(false).getAnswers().exception(e).queue();
		}
	}

	public static void unsafeEval(CommandEvent event) {
		try {
			engine.put("event", event);
			Object out = engine.eval(
				"(function() {" +
					"with (imports) {" +
					event.getArgs() +
					"}" +
					"})();");
			event.awaitTyping(false).getAnswers().send(out == null ? I18nModule.getLocalized("eval.noOut", event) : out.toString()).queue();
		} catch (ScriptException e) {
			event.awaitTyping(false).getAnswers().exception(e).queue();
		}
	}
}
