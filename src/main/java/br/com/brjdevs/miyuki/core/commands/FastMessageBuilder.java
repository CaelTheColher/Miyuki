package br.com.brjdevs.miyuki.core.commands;

import br.com.brjdevs.miyuki.lib.ReflectionEasyAsFuck;
import br.com.brjdevs.miyuki.modules.db.I18nModule;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.MessageBuilder.Formatting;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.entities.MessageReaction.ReactionEmote;
import net.dv8tion.jda.core.requests.RestAction;
import org.apache.commons.lang3.ArrayUtils;

import java.util.function.Function;
import java.util.stream.Stream;

public class FastMessageBuilder {
	private static final Function<Formatting, String> GET_TAG = ReflectionEasyAsFuck.Virtual.getField(Formatting.class, "tag", String.class);
	private final MessageBuilder builder = new MessageBuilder();

	public FastMessageBuilder setTTS(boolean tts) {
		builder.setTTS(tts);
		return this;
	}

	public FastMessageBuilder setEmbed(MessageEmbed embed) {
		builder.setEmbed(embed);
		return this;
	}

	public FastMessageBuilder appendString(String text) {
		builder.appendString(text);
		return this;
	}

	public FastMessageBuilder appendString(String text, Formatting... format) {
		builder.appendString(text, format);
		return this;
	}

	public FastMessageBuilder appendFormat(String format, Object... args) {
		builder.appendFormat(format, args);
		return this;
	}

	public FastMessageBuilder appendCodeBlock(String text, String language) {
		builder.appendCodeBlock(text, language);
		return this;
	}

	public FastMessageBuilder appendMention(User user) {
		builder.appendMention(user);
		return this;
	}

	public FastMessageBuilder appendEveryoneMention() {
		builder.appendEveryoneMention();
		return this;
	}

	public FastMessageBuilder appendHereMention() {
		builder.appendHereMention();
		return this;
	}

	public FastMessageBuilder appendMention(TextChannel channel) {
		builder.appendMention(channel);
		return this;
	}

	public FastMessageBuilder appendMention(Role role) {
		builder.appendMention(role);
		return this;
	}

	public FastMessageBuilder appendEmote(Emote emote) {
		return appendString(emote.getAsMention());
	}

	public FastMessageBuilder appendEmote(ReactionEmote emote) {
		return appendString(emote.isEmote() ? emote.getEmote().getAsMention() : emote.getName());
	}

	public FastMessageBuilder appendBoolean(boolean bool) {
		return appendString(bool ? ":white_check_mark:" : ":negative_squared_cross_mark:");
	}

	public FastMessageBuilder appendLocalized(String unlocalized, String locale) {
		return appendString(I18nModule.getLocalized(unlocalized, locale));
	}

	public FastMessageBuilder appendLocalized(String unlocalized, String locale, Object... format) {
		return appendString(String.format(I18nModule.getLocalized(unlocalized, locale), format));
	}

	public FastMessageBuilder appendLocalized(String unlocalized, CommandEvent event) {
		return appendString(event.getLocalized(unlocalized));
	}

	public FastMessageBuilder appendLocalized(String unlocalized, CommandEvent event, Object... format) {
		return appendString(event.getLocalized(unlocalized, format));
	}

	public FastMessageBuilder appendFormattingStart(Formatting... formatting) {
		return appendString(Stream.of(formatting).map(GET_TAG).reduce("", String::concat));
	}

	public FastMessageBuilder appendFormattingEnd(Formatting... formatting) {
		ArrayUtils.reverse(formatting);
		return appendFormattingStart(formatting);
	}

	public int getLength() {
		return builder.getLength();
	}

	public Message build() {
		return builder.build();
	}

	public RestAction<Message> sendMessage(MessageChannel channel) {
		return channel.sendMessage(build());
	}
}
