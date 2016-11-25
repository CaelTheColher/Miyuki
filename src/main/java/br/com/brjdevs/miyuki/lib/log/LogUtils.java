package br.com.brjdevs.miyuki.lib.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtils {
	public static Logger logger(String name) {
		return LoggerFactory.getLogger(name);
	}
}
