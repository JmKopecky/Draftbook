package dev.jkopecky.draftbook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DraftbookApplication {

    public static void main(String[] args) {
        SpringApplication.run(DraftbookApplication.class, args);
    }

    public static String retrieveRoot() {
        return System.getProperty("user.home") + "/draftbook_data/";
    }
}
