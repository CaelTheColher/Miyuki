package br.com.brjdevs.miyuki.modules.gui.impl;


import br.com.brjdevs.miyuki.modules.db.I18nModule;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GuiTranslationHandler {
	private static final List<Runnable> hooks = new ArrayList<>(), lazyHooks = new ArrayList<>();
	private static String lang = "en_US";

	public static void update() {
		hooks.forEach(Runnable::run);
		lazyHooks.forEach(Runnable::run);
	}

	public static void addHook(Runnable runnable) {
		hooks.add(runnable);
	}

	public static void addLazyHook(Runnable runnable) {
		lazyHooks.add(runnable);
	}

	public static void addHook(Consumer<String> consumer, String unlocalized) {
		addHook(() -> consumer.accept(get(unlocalized)));
	}

	public static void addLazyHook(Consumer<String> consumer, String unlocalized) {
		addLazyHook(() -> consumer.accept(get(unlocalized)));
	}

	public static String get(String unlocalized) {
		if (!Bot.LOADED) return unlocalized;
		else return I18nModule.getLocalized("gui." + unlocalized, lang);
	}

	public static String getLang() {
		return lang;
	}

	public static void setLang(String lang) {
		GuiTranslationHandler.lang = lang;
		update();
	}
}
