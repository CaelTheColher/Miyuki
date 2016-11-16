package br.com.brjdevs.miyuki.utils.data;


import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

import static br.com.brjdevs.miyuki.utils.CollectionUtils.apply;

public class DBUtils {
	public static Path getPath(String file, String ext) {
		try {
			return Paths.get(file + "." + ext);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static List<String> encode(List<String> list) {
		return apply(list, DBUtils::encode);
	}

	public static List<String> decode(List<String> list) {
		return apply(list, DBUtils::decode);
	}

	public static String encode(String string) {
		return Base64.getEncoder().encodeToString(string.getBytes(Charset.forName("UTF-8")));
	}

	public static String decode(String string) {
		return new String(Base64.getDecoder().decode(string), Charset.forName("UTF-8"));
	}
}
