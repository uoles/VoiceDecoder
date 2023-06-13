package ru.uoles.proj;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.uoles.proj.service.SowaService;

@SpringBootApplication
public class Application implements CommandLineRunner {

    @Autowired
    private SowaService sowaService;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) {
        sowaService.initSowa();
    }
}