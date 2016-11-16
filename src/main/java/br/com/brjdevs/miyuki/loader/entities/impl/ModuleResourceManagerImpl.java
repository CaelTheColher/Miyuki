/*
 * This class was created by <AdrianTodt>. It's distributed as
 * part of the DavidBot. Get the Source Code in github:
 * https://github.com/adriantodt/David
 *
 * DavidBot is Open Source and distributed under the
 * GNU Lesser General Public License v2.1:
 * https://github.com/adriantodt/David/blob/master/LICENSE
 *
 * File Created @ [11/11/16 08:33]
 */

package br.com.brjdevs.miyuki.David.loader.entities.impl;

import br.com.brjdevs.miyuki.David.Loader;
import br.com.brjdevs.miyuki.David.loader.Module;
import br.com.brjdevs.miyuki.David.loader.entities.ModuleResourceManager;

public class ModuleResourceManagerImpl implements ModuleResourceManager {
	private final String moduleName;

	public ModuleResourceManagerImpl(Module module) {
		this.moduleName = module.name().replace('.', '/');
	}

	@Override
	public String get(String path) {
		return Loader.resource("/assets/" + moduleName + "/" + path);
	}
}
