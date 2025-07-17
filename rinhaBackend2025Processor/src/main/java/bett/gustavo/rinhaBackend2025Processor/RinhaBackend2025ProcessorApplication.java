package bett.gustavo.rinhaBackend2025Processor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
		"bett.gustavo.rinhaBackend2025Processor",
		"bett.gustavo.rinhaBackend2025Model"
})
@EnableRedisRepositories(basePackages = "bett.gustavo.rinhaBackend2025Model.repository")
@EntityScan(basePackages = "bett.gustavo.rinhaBackend2025Model.model")
public class RinhaBackend2025ProcessorApplication {

	public static void main(String[] args) {
		SpringApplication.run(RinhaBackend2025ProcessorApplication.class, args);
	}

}
