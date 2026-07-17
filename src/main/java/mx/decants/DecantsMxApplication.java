package mx.decants;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DecantsMxApplication {

    public static void main(String[] args) {
        SpringApplication.run(DecantsMxApplication.class, args);
    }
}
