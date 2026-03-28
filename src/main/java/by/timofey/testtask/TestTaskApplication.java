package by.timofey.testtask;

import by.timofey.testtask.config.KafkaPropertyConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(KafkaPropertyConfig.class)
public class TestTaskApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestTaskApplication.class, args);
	}

}
