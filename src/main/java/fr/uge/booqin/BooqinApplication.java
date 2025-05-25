package fr.uge.booqin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BooqinApplication {

	public static void main(String[] args) {
		SpringApplication.run(BooqinApplication.class, args);
	}

}
