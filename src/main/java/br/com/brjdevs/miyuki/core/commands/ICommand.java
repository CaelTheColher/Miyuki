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

package br.com.brjdevs.miyuki.core.commands;

import br.com.brjdevs.miyuki.modules.cmds.manager.PermissionsModule;

public interface ICommand extends ITranslatable {
	void run(CommandEvent event);

	/**
	 * Provides Check for Minimal Perm usage.
	 *
	 * @return the Permission Required
	 */
	default long retrievePerm() {
		return PermissionsModule.RUN_CMDS;
	}

	default boolean sendStartTyping() {
		return true;
	}
}