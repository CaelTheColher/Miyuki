package br.com.brjdevs.miyuki.modules.rest;

import br.com.brjdevs.miyuki.core.Module;
import br.com.brjdevs.miyuki.core.Module.JDAInstance;
import br.com.brjdevs.miyuki.core.Module.PostReady;
import br.com.brjdevs.miyuki.core.Module.Type;
import br.com.brjdevs.miyuki.lib.AsyncUtils;
import net.dv8tion.jda.core.JDA;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;

@Module(id = "rest", type = Type.STATIC)
@Controller
@SpringBootApplication
public class RESTInterface {
	@JDAInstance
	public static JDA jda = null;

	@PostReady
	public static void startWebServer() {
		AsyncUtils.async("RESTInterface", () -> SpringApplication.run(RESTInterface.class)).run();
	}

	@Bean
	public EmbeddedServletContainerCustomizer containerCustomizer() {
		return (container -> {
			container.setPort(8012);
			container.setDisplayName(jda.getSelfUser().getName() + " REST API");
		});
	}
}
