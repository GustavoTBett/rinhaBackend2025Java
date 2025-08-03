package bett.gustavo.rinhaBackend2025Api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@ComponentScan(basePackages = {
		"bett.gustavo.rinhaBackend2025Api",
		"bett.gustavo.rinhaBackend2025Model"
})
@EnableRedisRepositories(basePackages = "bett.gustavo.rinhaBackend2025Model.repository")
@EntityScan(basePackages = "bett.gustavo.rinhaBackend2025Model.model")
@EnableAsync
public class RinhaBackend2025ApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(RinhaBackend2025ApiApplication.class, args);
	}

}
