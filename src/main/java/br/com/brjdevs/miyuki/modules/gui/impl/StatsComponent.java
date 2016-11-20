package br.com.brjdevs.miyuki.modules.gui.impl;


import br.com.brjdevs.miyuki.modules.cmds.util.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Date;

import static br.com.brjdevs.miyuki.modules.gui.GUIModule.hooks;
import static br.com.brjdevs.miyuki.modules.gui.GUIModule.loaded;

public class StatsComponent extends JComponent {
	private static final int mb = 1024 * 1024;
	private final int[] graphicValues = new int[229];
	private final String[] msgs = new String[11];
	private int vp = 0, lastValue = 0;

	public StatsComponent() {
		this.setPreferredSize(new Dimension(456, 246));
		this.setMinimumSize(new Dimension(456, 246));
		this.setMaximumSize(new Dimension(456, 246));
		new Timer(1000, actionPerformed -> tick()).start();
		this.setBackground(Color.BLACK);
		hooks.add(this::tick);
	}

	private void addToArray(int value) {
		System.arraycopy(graphicValues, 1, graphicValues, 0, graphicValues.length - 1);
		graphicValues[graphicValues.length - 1] = value;
	}

	private int getMax() {
		return Arrays.stream(graphicValues).max().orElse(0);
	}

	private void tick() {

		Runtime instance = Runtime.getRuntime();
		System.gc();
		if (!loaded) {
			this.msgs[0] = "Please Wait...";
			this.msgs[1] = null;
			this.msgs[2] = null;
			this.msgs[3] = null;
			this.msgs[4] = null;
			this.msgs[5] = null;

		} else {
			this.msgs[0] = String.format(GuiTranslationHandler.get("stats0"), SessionManager.calculate(SessionManager.startDate == null ? new Date() : SessionManager.startDate, new Date(), GuiTranslationHandler.getLang()));
			this.msgs[1] = String.format(GuiTranslationHandler.get("stats1"), SessionManager.restActions, SessionManager.cmds, SessionManager.crashes, SessionManager.toofasts, SessionManager.noperm, SessionManager.invalidargs);
			this.msgs[2] = String.format(GuiTranslationHandler.get("stats2"), SessionManager.wgets, Thread.activeCount());
			this.msgs[3] = String.format(GuiTranslationHandler.get("stats3"), (instance.totalMemory() - instance.freeMemory()) / mb, instance.totalMemory() / mb, instance.maxMemory() / mb);
			this.msgs[4] = String.format(GuiTranslationHandler.get("stats4"), SessionManager.cpuUsage);
		}
		addToArray(SessionManager.restActions - lastValue);
		lastValue = SessionManager.restActions;
		this.repaint();
	}

	public void paint(Graphics g) {
		g.setColor(new Color(16777215));
		g.fillRect(0, 0, 456, 246);

		for (int i = 0; i < graphicValues.length; ++i) {
			int eachValue = graphicValues[i] * 5;
			g.setColor(new Color(eachValue + 28 << 16));
			g.fillRect(i * 2, 1, 2, eachValue);
		}

		g.setColor(Color.BLACK);

		for (int i = 0; i < this.msgs.length; ++i)
			if (this.msgs[i] != null) g.drawString(this.msgs[i], 32, 116 + i * 16);
	}
}