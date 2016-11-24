package br.com.brjdevs.miyuki.modules.cmds.manager;

import br.com.brjdevs.miyuki.commands.CommandEvent;
import br.com.brjdevs.miyuki.loader.Module;
import br.com.brjdevs.miyuki.loader.Module.JDAInstance;
import br.com.brjdevs.miyuki.modules.db.DBModule;
import br.com.brjdevs.miyuki.modules.db.GuildModule;
import br.com.brjdevs.miyuki.modules.db.GuildModule.Data;
import br.com.brjdevs.miyuki.utils.DiscordUtils;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.User;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static br.com.brjdevs.java.lib.MathHelper.previousPowerOfTwo;
import static br.com.brjdevs.java.lib.MathHelper.roundToPowerOf2;

/*
USE ESSE COMENTÁRIO PARA EXPLICAR O SISTEMA DE PERMISSÕES. OBRIGADO.

Como funciona o sistema de permissões:
	Inteiro Longo (long/64-bits). Cada permissão é colocada em um Bit, ou seja, 64 combinações:

Guia de Referência:
	LONG: !!ZYXWVUTSRQPONMLKJIHGFEDCBAzyxwvutsrqponmlkjihgfedcba9876543210 //No caso, 0 = 2^0 e Z = 2^62
	- [!] = Bits reservados para nunca serem usados (Eles podem deixar o valor do Long grande demais...)
	- [0-9] = Bits relacionados a execução de comandos e sistema de permissão.
	- [a-z] = Bits para permissões especiais menores (Comandos específicos em geral).
	- [A-Z] = Bits para permissões especiais maiores (Opções geralmente perigosas/inseguras).
Permissões:
	(0)		RUN_CMDS
				Basic Permission. If took from User, the Bot will ignore any commands.
	(1)		RUN_USER_CMDS
				Execute User Coomands. May be took from User because of spam.
	(2)		RUN_SCRIPT_CMDS
				Run Script Commands. Disabled from everyone until proper Sandboxing.
	(3)		SET_PERMS
				Set others users Permissions (using &guild perms set)
	(4)		GUILD_PASS
				Access the Guild in another using &GUILD:<command>
	(5)		MANAGE_USER_CMDS
				Allows creating and removing User Commands using &cmds add/rm
	(6)		MANAGE_SPECIAL_USER_CMDS
				Allows creating and removing User Commands that do special things
	(7-9)	PERMSYS_GM/PERMSYS_GO/PERMSYS_BO
				Assist Perms. Protects people with higher perms from being affected by lower rank people
	(10/a)	PUSH_SUBSCRIBE
				Subscribe the channel to Push Notifications.
	(11/b)	SCRIPTS
				Run Scripts
	(12/c)	SET_GUILD
				Set Guild configs
	(13/d)	PUSH_SEND
				Send Push Notifications
	(36/A)	USE_INTERFACES
				Use Interfaces (Currently broken)
	(37/B)	SCRIPTS_UNSAFEENV
				Run Commands in a unsafe environiment (Disabled until proper Sandboxing)
	(62/Z)	BOT_ADMIN
				Stops/Resets the Bot
 */
@Module(id = "permissions")
public class PermissionsModule {
	public static final long
		RUN_CMDS = bits(0),
		RUN_USER_CMDS = bits(1),
		SET_PERMS = bits(2),
		GUILD_PASS = bits(3),
		MANAGE_USER_CMDS = bits(4),
		MANAGE_SPECIAL_USER_CMDS = bits(5),
		PERMSYS_GM = bits(6),
		PERMSYS_GO = bits(7),
		PERMSYS_BM = bits(8),
		PERMSYS_BO = bits(9),
		MANAGE_PUSH = bits(36), //A
		MANAGE_FEEDS = bits(37), //B
		MANAGE_I18N = bits(38), //C
		MANAGE_GUILD = bits(39), //D
		BOT_ADMIN = bits(62);
	public static final long
		BASE_USER = RUN_CMDS | RUN_USER_CMDS | GUILD_PASS,
		GUILD_MOD = BASE_USER | MANAGE_USER_CMDS | MANAGE_SPECIAL_USER_CMDS | SET_PERMS | PERMSYS_GM | MANAGE_PUSH | MANAGE_FEEDS | MANAGE_GUILD,
		GUILD_OWNER = GUILD_MOD | PERMSYS_GO,
		BOT_MOD = BASE_USER | MANAGE_FEEDS | MANAGE_PUSH | MANAGE_I18N | PERMSYS_BM,
		BOT_OWNER = GUILD_OWNER | BOT_MOD | BOT_ADMIN | PERMSYS_BO;
	public static Map<String, Long> perms = new HashMap<>();
	@JDAInstance
	private static JDA jda = null;

	static {
		Arrays.stream(PermissionsModule.class.getDeclaredFields()) //This Reflection is used to HashMap-fy all the Fields above.
			.filter(field -> Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers()) && Modifier.isPublic(field.getModifiers())) //public static final fields only
			.forEach(field -> {
				try {
					perms.put(field.getName(), field.getLong(null));
				} catch (Exception ignored) {
				}
			});
	}

	private static long bits(int... bits) {
		long mask = 0;
		for (long bit : bits) mask |= (long) Math.pow(2, bit);
		return mask;
	}

	public static long getSenderPerm(Data guild, CommandEvent event) {
		return getPermFor(guild, event.getAuthor().getId());
	}

	public static boolean setPerms(Data guild, CommandEvent event, String target, long permsToAdd, long permsToTake) {
		target = DiscordUtils.processId(target); //Un-mention ID
		if (target.equals(event.getAuthor().getId())) return false; //Disable changing itself
		long senderPerm = getSenderPerm(guild, event), targetPerm = getPermFor(guild, target); //Get perrms
		if (!checkPerms(senderPerm, targetPerm)) return false; //Check the Special Bits
		if ((senderPerm & (permsToAdd | permsToTake)) != (permsToAdd | permsToTake))
			return false; //Check if the Sender Perm have all the permissions
		guild.setUserPerms(target, targetPerm ^ (targetPerm & permsToTake) | permsToAdd);
		return true;
	}

	private static boolean checkPerms(long senderPerm, long targetPerm) {
		long perms = bits(6, 7, 8, 9);
		senderPerm &= perms;
		targetPerm &= perms; //Select bits 6 7 8 9
		targetPerm = previousPowerOfTwo(roundToPowerOf2(targetPerm));
		senderPerm = previousPowerOfTwo(roundToPowerOf2(senderPerm)); //Get the biggest
		return targetPerm <= senderPerm;
	}

	public static long getPermFor(Data guild, String target) {
		target = DiscordUtils.processId(target);
		long global = GuildModule.GLOBAL.getUserPerms(target, 0L), unrevokeable = DBModule.getOwnerIDs().contains(target) ? BOT_OWNER : guild.getGuild(jda) != null && guild.getGuild(jda).getOwner().getUser().getId().equals(target) ? GUILD_OWNER : 0;
		return global | guild.getUserPerms(target, (global == 0 ? guild.getUserPerms("default", BASE_USER) : global)) | unrevokeable;
		//this will merge the Global Perms, the Local Perms, and Unrevokeable Perms (BOT_OWNER or GUILD_OWNER)
	}

	public static boolean canRunCommand(GuildModule.Data guild, CommandEvent event) {
		return havePermsRequired(guild, event.getAuthor(), event.getCommand().retrievePerm());
	}

	public static boolean havePermsRequired(GuildModule.Data guild, User user, long perms) {
		return (perms & getPermFor(guild, user.getId())) == perms;
	}

	public static List<String> toCollection(long userPerms) {
		return perms
			.entrySet()
			.stream()
			.filter(entry -> (entry.getValue() & userPerms) == entry.getValue())
			.map(Map.Entry::getKey)
			.sorted(String::compareTo).collect(Collectors.toList());
	}
}
