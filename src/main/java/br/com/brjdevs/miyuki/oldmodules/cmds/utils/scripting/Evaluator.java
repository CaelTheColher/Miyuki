package br.com.brjdevs.miyuki.oldmodules.cmds.utils.scripting;

import br.com.brjdevs.miyuki.commands.CommandEvent;

import java.util.HashMap;
import java.util.Map;

public interface Evaluator {
	Map<String, Evaluator> EVALUATOR_REGISTER = new HashMap<>();

	void eval(CommandEvent event);
}
