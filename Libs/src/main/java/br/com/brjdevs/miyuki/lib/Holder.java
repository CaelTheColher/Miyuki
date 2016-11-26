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

package br.com.brjdevs.miyuki.lib;

public class Holder<T> {
	public T var;

	public Holder() {
	}

	public Holder(T object) {
		var = object;
	}

	@Override
	public int hashCode() {
		return var.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Holder) {
			return super.equals(obj);
		}

		return obj.equals(var);
	}

	@Override
	public String toString() {
		return var.toString();
	}
}
