package br.com.brjdevs.miyuki.modules.cmds;

import br.com.brjdevs.miyuki.commands.Commands;
import br.com.brjdevs.miyuki.commands.ICommand;
import br.com.brjdevs.miyuki.loader.Module;
import br.com.brjdevs.miyuki.loader.Module.Command;
import br.com.brjdevs.miyuki.loader.Module.Type;
import br.com.brjdevs.miyuki.utils.StringUtils;

import java.util.Arrays;

@Module(name = "cmds.minor", type = Type.STATIC)
public class MinorCmds {
	//	@Command("parser")
//	private static ICommand parser() {
//		return Commands.buildSimple()
//			.setAction(event -> event.awaitTyping().sendMessage(Arrays.toString(StringUtils.parse(event.getArgs(0)).entrySet().toArray())).queue())
//			.build();
//	}
//
	@Command("splitargs")
	private static ICommand splitargs() {
		return Commands.buildSimple()
			.setAction(event -> event.awaitTyping().sendMessage(Arrays.toString(StringUtils.advancedSplitArgs(event.getArgs(), 0))).queue())
			.build();
	}

//	@Command("testcode")
//	private static ICommand testcode() {
//		return Commands.buildSimple()
//			.setAction(event -> event.awaitTyping().sendMessage(
//				CollectionUtils.subListOn(
//					Arrays.asList(
//						StringUtils.advancedSplitArgs(event.getArg(2, 1), 0)
//					),
//					t -> t.equals(event.getArg(2, 0))
//				).toString()).queue()
//			)
//			.build();
//	}

	//implAnnoy
//		addCommand("annoy", CommandsProvider.buildSimple()
//			.setPermRequired(ANNOY)
//			.setUsageDeprecatedMethod("")
//			.setAction(((arguments, event) -> {
//				String[] args = splitArgs(arguments, 3); //!spysend CH MSG
//				if (args[0].isEmpty() || args[1].isEmpty()) event.getAnswers().invalidargs().queue();
//				else {
//					args[0] = processId(args[0]);
//					if ("clear".equals(args[1])) {
//						DataManager.data.annoy.remove(args[0]);
//						event.getAnswers().bool( true).queue();
//					}
//					if ("add".equals(args[1])) {
//						if (!DataManager.data.annoy.containsKey(args[0]))
//							DataManager.data.annoy.put(args[0], new ArrayList<>());
//						DataManager.data.annoy.get(args[0]).add(args[2]);
//						event.getAnswers().bool( true).queue();
//					}
//				}
//			}))
//			.build()
//		);
}
