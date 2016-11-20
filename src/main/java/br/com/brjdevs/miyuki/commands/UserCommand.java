/*
 * This class was created by <$user.name>. It's distributed as
 * part of the Miyuki Bot. Get the Source Code in github:
 * https://github.com/BRjDevs/Miyuki
 *
 * Miyuki is Open Source and distributed under the
 * GNU Lesser General Public License v2.1:
 * https://github.com/BRjDevs/Miyuki/blob/master/LICENSE
 *
 * File Created @ [16/11/16 13:58]
 */

package br.com.brjdevs.miyuki.commands;

import br.com.brjdevs.miyuki.modules.cmds.manager.PermissionsModule;
import br.com.brjdevs.miyuki.modules.db.I18nModule;
import br.com.brjdevs.miyuki.utils.DiscordUtils;

import java.util.*;

import static br.com.brjdevs.miyuki.utils.CollectionUtils.random;


//import cf.adriantodt.bot.commands.cmds.utils.scripting.JS;

public class UserCommand implements ICommand, ITranslatable {
	public List<String> responses = new ArrayList<>();

	@Override
	public void run(CommandEvent event) {
		String response = random(responses);
		if (response.length() > 7) {
//			if (response.substring(0, 6).equals("get://")) {
//				event.getAnswers().send(IOHelper.toString(response.substring(6))).queue();
//				return;
//			} else
			//} else if (response.substring(0, 6).equals("aud://")) {
			//	Audio.queue(IOHelper.newURL(response.substring(6)), event);
			//	return;
		} else if (response.substring(0, 5).equals("js://")) {
			if (PermissionsModule.havePermsRequired(event.getGuild(), event.getAuthor(), PermissionsModule.RUN_SCRIPT_CMDS)) {
				//JS.eval(event.getGuild(), response.substring(5), event.getEvent());
			} else {
				event.awaitTyping(false).getAnswers().noperm().queue();
			}
			return;
		}

		Map<String, String> dynamicMap = new HashMap<>();
		dynamicMap.put("event.username", event.getAuthor().getName());
		dynamicMap.put("event.nickname", event.getMember().getNickname());
		dynamicMap.put("event.name", DiscordUtils.name(event.getAuthor(), event.getGuild().getGuild(event.getJDA())));
		dynamicMap.put("event.mentionUser", event.getAuthor().getAsMention());
		dynamicMap.put("event.args", event.getArgs());
		dynamicMap.put("event.guild", event.getGuild().getName());
		event.awaitTyping(false).getAnswers().send(I18nModule.dynamicTranslate(response, I18nModule.getLocale(event), Optional.of(dynamicMap))).queue();
	}

	@Override
	public long retrievePerm() {
		return PermissionsModule.RUN_CMDS | PermissionsModule.RUN_USER_CMDS;
	}

	@Override
	public String toString(String language) {
		return Arrays.toString(responses.toArray());
	}
}
