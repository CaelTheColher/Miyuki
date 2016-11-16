/*
 * This class was created by <AdrianTodt>. It's distributed as
 * part of the DavidBot. Get the Source Code in github:
 * https://github.com/adriantodt/David
 *
 * DavidBot is Open Source and distributed under the
 * GNU Lesser General Public License v2.1:
 * https://github.com/adriantodt/David/blob/master/LICENSE
 *
 * File Created @ [11/11/16 08:46]
 */

package br.com.brjdevs.miyuki.David.modules.init;

import br.com.brjdevs.miyuki.David.commands.base.Holder;
import br.com.brjdevs.miyuki.David.loader.Module;
import br.com.brjdevs.miyuki.David.loader.Module.SubscribeJDA;
import br.com.brjdevs.miyuki.David.loader.Module.Type;
import br.com.brjdevs.miyuki.David.modules.db.GuildModule;
import br.com.brjdevs.miyuki.David.modules.db.I18nModule;
import br.com.brjdevs.miyuki.David.modules.db.UserModule;
import br.com.brjdevs.miyuki.David.oldmodules.init.Statistics;
import br.com.brjdevs.miyuki.David.utils.DiscordUtils;
import br.com.brjdevs.miyuki.utils.StringUtils;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.SubscribeEvent;

import java.util.Optional;

@Module(name = "greeter", type = Type.STATIC)
@SubscribeJDA
public class BotGreeter {
	public static void greet(TextChannel channel, Optional<User> optionalUser) {
		Holder<String> lang = new Holder<>(GuildModule.fromDiscord(channel.getGuild()).getLang());
		optionalUser.ifPresent(user -> lang.var = StringUtils.notNullOrDefault(UserModule.fromDiscord(user).getLang(), lang.var));
		channel.sendTyping().queue(success -> {
			Statistics.restActions++;
			channel.sendMessage(I18nModule.getLocalized("bot.help", lang.var)).queue();
		});
	}

	@SubscribeEvent
	public static void onGuildJoin(GuildJoinEvent event) {
		GuildModule.Data guild = GuildModule.fromDiscord(event.getGuild());
		guild.setLang(DiscordUtils.guessGuildLanguage(event.getGuild()));
		event.getGuild().getPublicChannel().sendTyping().queue();
		event.getGuild().getPublicChannel().sendMessage(I18nModule.getLocalized("bot.hello1", guild.getLang())).queue();
		event.getGuild().getPublicChannel().sendMessage(String.format(I18nModule.getLocalized("bot.hello2", guild.getLang()), event.getGuild().getOwner().getAsMention(), guild.getLang())).queue();
	}

	@SubscribeEvent
	public static void onMessageReceived(GuildMessageReceivedEvent event) {
		if (event.getMessage().getRawContent().trim().matches("<@!?" + event.getJDA().getSelfUser().getId() + ">"))
			greet(event.getChannel(), Optional.of(event.getAuthor()));
	}
}
