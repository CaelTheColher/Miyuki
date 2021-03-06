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

package br.com.brjdevs.miyuki.modules.cmds.manager.entities;

import br.com.brjdevs.miyuki.modules.cmds.utils.SessionManager;
import br.com.brjdevs.miyuki.modules.db.GuildModule.Data;
import br.com.brjdevs.miyuki.modules.db.I18nModule;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.requests.RestAction;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Future;

import static br.com.brjdevs.miyuki.lib.AsyncUtils.sleep;
import static br.com.brjdevs.miyuki.lib.StringUtils.splitArgs;
import static br.com.brjdevs.miyuki.lib.core.DiscordUtils.submit;


public class CommandEvent {
	private final GuildMessageReceivedEvent event;
	private final Data targetGuild;
	private final ICommand command;
	private final String args;
	private final FastAnswers answers;
	private Future<Void> awaitableTyping = null;

	public CommandEvent(GuildMessageReceivedEvent event, Data targetGuild, ICommand command, String args) {
		SessionManager.cmds++;
		this.event = event;
		this.targetGuild = targetGuild;
		this.command = command;
		this.args = args;
		this.answers = new FastAnswers(this);
	}

	public FastAnswers getAnswersForChannel(MessageChannel channel) {
		return getAnswers().forChannel(channel);
	}

	public FastAnswers getAnswers() {
		return answers;
	}

	public String getArgs() {
		return args;
	}

	public String[] getArgs(int expectedArgs) {
		return splitArgs(getArgs(), expectedArgs);
	}

	public String getArg(int expectedArgs, int arg) {
		return getArgs(expectedArgs)[arg];
	}

	public GuildMessageReceivedEvent getEvent() {
		return event;
	}

	public Data getGuild() {
		return targetGuild;
	}

	public ICommand getCommand() {
		return command;
	}

	public TextChannel getChannel() {
		return getEvent().getChannel();
	}

	public Guild getOriginGuild() {
		return getEvent().getGuild();
	}

	public Member getMember() {
		return getEvent().getMember();
	}

	public Message getMessage() {
		return getEvent().getMessage();
	}

	public User getAuthor() {
		return getEvent().getAuthor();
	}

	public JDA getJDA() {
		return getEvent().getJDA();
	}

	public RestAction<Message> sendMessage(String text) {
		return getChannel().sendMessage(text);
	}

	public RestAction<Message> sendMessage(Message msg) {
		return getChannel().sendMessage(msg);
	}

	public RestAction<Message> sendMessage(MessageEmbed embed) {
		return getChannel().sendMessage(embed);
	}

	public RestAction<Message> sendFile(File file, Message message) throws IOException {
		return getChannel().sendFile(file, message);
	}

	public String getLocalized(String unlocalized) {
		return I18nModule.getLocalized(unlocalized, this);
	}

	public String getLocalized(String unlocalized, Object... format) {
		return String.format(I18nModule.getLocalized(unlocalized, this), format);
	}

	public RestAction<Void> sendTyping() {
		return getChannel().sendTyping();
	}

	public CommandEvent sendAwaitableTyping() {
		awaitableTyping = submit(sendTyping());
		return this;
	}

	public CommandEvent createChild(ICommand command, String args) {
		return new CommandEvent(getEvent(), getGuild(), command, args);
	}

	public CommandEvent awaitTyping(boolean lazy) {
		int sleepMilis = lazy ? 200 : 1000;
		if (awaitableTyping == null) return this;
		while (awaitableTyping != null && !awaitableTyping.isDone()) {
			sleep(sleepMilis);
		}
		awaitableTyping = null;
		return this;
	}

	public boolean tryOpenPrivateChannel() {
		if (!event.getAuthor().hasPrivateChannel()) {
			try {
				event.getAuthor().openPrivateChannel().block();
				return true;
			} catch (Exception e) {
				//LogManager.getLogger("CommandEvent - PMs").info("Failure when trying to open private channel for user " + event.getAuthor().toString() + ". User was asked to send a pm.\n" +
				//	e.getClass().getSimpleName() + ": " + e.getMessage());
				awaitTyping(true).sendMessage(event.getMember().getAsMention() + " I can't send any DM messages to you, please send a DM to me with the message \"!ping\" to resolve the issue.\n" +
					"You should receive a \"pong!\" as response\n" +
					"Your command was ignored because it is required that the user can receive a DM to execute commands.").queue();
				return false;
			}
		} else {
			return true;
		}
	}

	public Optional<FastAnswers> getAnswersForPrivate() {
		if (tryOpenPrivateChannel()) return Optional.ofNullable(answers.forChannel(getAuthor().getPrivateChannel()));
		return Optional.empty();
	}

	public CommandEvent awaitTyping() {
		return awaitTyping(false);
	}
}
