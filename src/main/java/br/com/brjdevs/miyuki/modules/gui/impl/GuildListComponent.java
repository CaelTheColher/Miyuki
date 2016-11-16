package br.com.brjdevs.miyuki.modules.gui.impl;


import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.hooks.SubscribeEvent;

import javax.swing.*;
import java.util.Vector;

import static br.com.brjdevs.miyuki.modules.gui.GUIModule.hooks;
import static br.com.brjdevs.miyuki.modules.gui.GUIModule.jda;
import static net.dv8tion.jda.core.JDA.Status.INITIALIZING;

public class GuildListComponent extends JList<String> implements Runnable {
	public GuildListComponent() {
		run();
		hooks.add(this);
		hooks.add(() -> jda.addEventListener(this));
	}

	@SubscribeEvent
	public void onGuildJoin(GuildJoinEvent event) {
		run();
	}

	@SubscribeEvent
	public void onGuildLeave(GuildLeaveEvent event) {
		run();
	}

	@SuppressWarnings("unchecked")
	public void run() {
		Vector<String> vector = new Vector<>();
		if (jda == null || jda.getStatus() == INITIALIZING) {
			vector.add("<Bot being Loaded>");
		} else {
			jda.getGuilds().stream().map(Guild::getName).forEach(vector::add);
		}
		this.setListData(vector);
	}
}