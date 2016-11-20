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
import br.com.brjdevs.miyuki.modules.cmds.util.SessionManager;
import br.com.brjdevs.miyuki.modules.db.I18nModule;
import br.com.brjdevs.miyuki.utils.*;
import com.google.common.base.Throwables;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.requests.RestAction;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


public class FastAnswers {
	public final MessageChannel channel;
	public final CommandEvent event;

	public FastAnswers(CommandEvent event) {
		this(event.getChannel(), event);
	}

	public FastAnswers(MessageChannel channel, CommandEvent event) {
		this.channel = channel;
		this.event = event;
	}

	public FastAnswers forChannel(MessageChannel channel) {
		return new FastAnswers(channel, event);
	}

	public RestAction<Message> exception(Exception e) {
		Future<String> stringFuture = TaskManager.getThreadPool().submit(() -> Hastebin.post(Throwables.getStackTraceAsString(e)));
		dear("uma exceção ocorreu durante a execução do comando:");
		Log4jUtils.logger().error("Exception occurred during command \"" + event.getMessage().getContent() + "\": ", e);
		SessionManager.crashes++;
		String s;
		try {
			s = "//Full StackTrace: " + stringFuture.get();
		} catch (InterruptedException | ExecutionException ignored) {
			s = "//Could not upload the StackTrace \uD83D\uDE26";
		}
		return sendCased(StringUtils.limit(e.toString(), 500) + "\n" + s, "java");
	}

	public RestAction<Message> toofast() {
		SessionManager.toofasts++;
		return send("*" + I18nModule.getLocalized("answers.calmDown", event) + " " + event.getAuthor().getAsMention() + "! " + I18nModule.getLocalized("answers.tooFast", event) + "!*");
	}

	public RestAction<Message> sendTranslated(String unlocalized) {
		return send(I18nModule.getLocalized(unlocalized, event));
	}

	public RestAction<Message> send(String message) {
		//Statistics.msgs++;
		event.awaitTyping(false);
		return event.getChannel().sendMessage(message);
	}

	public RestAction<Message> sendCased(String message) {
		return sendCased(message, "");
	}

	public RestAction<Message> sendCased(String message, String format) {
		return send(Formatter.encase(message, format));
	}

	public RestAction<Message> announce(String message) {
		return send(Formatter.boldAndItalic(message));
	}

	public RestAction<Message> noperm() {
		long perm = event.getCommand().retrievePerm();
		perm ^= PermissionsModule.getSenderPerm(event.getGuild(), event) & perm;
		return noperm(perm);
	}

	public RestAction<Message> noperm(long permsMissing) {
		SessionManager.noperm++;
		StringBuilder b = new StringBuilder("*(Permissões Ausentes:");
		PermissionsModule.toCollection(permsMissing).forEach(s -> b.append(" ").append(s));
		b.append(")*");
		dear("você não tem permissão para executar esse comando.");
		return send(b.toString());
	}

	public RestAction<Message> bool(boolean v) {
		return send(v ? ":white_check_mark:" : ":negative_squared_cross_mark:");
	}

	public RestAction<Message> bool(boolean v, String post) {
		return send((v ? ":white_check_mark:" : ":negative_squared_cross_mark:") + post);
	}

	public RestAction<Message> invalidargs() {
		SessionManager.invalidargs++;
		String usage = event.getCommand().toString(I18nModule.getLocale(event));
		if (usage == null) return dear(I18nModule.getLocalized("answers.invalidArgs", event));
		else if (!usage.isEmpty()) return sendCased(usage);
		return new RestAction.EmptyRestAction<>(null);
	}

	public RestAction<Message> dear(String answer) {
		return send(Formatter.italic(I18nModule.getLocalized("answers.dear", event) + " " + event.getAuthor().getName() + ", " + answer));
	}
}
