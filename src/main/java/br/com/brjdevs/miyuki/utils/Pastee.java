package br.com.brjdevs.miyuki.utils;

import br.com.brjdevs.miyuki.modules.db.DBModule;
import com.google.common.collect.ImmutableMap;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class Pastee {
	public static String post(String paste) {
		return post(paste, null);
	}

	public static String post(String paste, String desc) {
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost("https://paste.ee/api");

		try {
			post.setEntity(
				new StringEntity(
					implode(
						new ImmutableMap.Builder<String, Object>()
							.put("key", DBModule.getConfig().get("pasteeKey").getAsString())
							.put("description", desc)
							.put("paste", paste)
							.put("format", "simple")
							.build()
					)
				)
			);

			HttpResponse response = client.execute(post);

			return EntityUtils.toString(response.getEntity());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String implode(Map<String, Object> values) throws IOException {
		StringBuilder builder = new StringBuilder();
		Iterator<Entry<String, Object>> iterator = values.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, Object> entry = iterator.next();

			if (entry.getValue() == null) continue;

			builder.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue().toString(), "UTF-8"));

			if (iterator.hasNext())
				builder.append("&");
		}
		return builder.toString();
	}
}
