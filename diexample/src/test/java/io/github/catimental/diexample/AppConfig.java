package io.github.catimental.diexample;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AppConfig implements CommandLineRunner {

    private final MainController mainController;

    public AppConfig(MainController mainController){
        this.mainController = mainController;
    }

    public static void main(String[] args){
        SpringApplication.run(AppConfig.class, args);
    }

    @Override
    public void run(String... args){
        mainController.showMember();
    }
}
