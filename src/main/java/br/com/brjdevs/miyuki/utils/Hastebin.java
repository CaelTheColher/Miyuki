package br.com.brjdevs.miyuki.utils;

import com.google.gson.JsonParser;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class Hastebin {
	public static String post(String data) {
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost("http://hastebin.com/documents");

		try {
			post.setEntity(new StringEntity(data));

			HttpResponse response = client.execute(post);

			String result = EntityUtils.toString(response.getEntity());
			return "http://hastebin.com/" + new JsonParser().parse(result).getAsJsonObject().get("key");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
