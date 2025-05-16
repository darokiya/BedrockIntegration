package com.app.playerservicejava;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.ollama4j.OllamaAPI;

@SpringBootTest
class PlayerServiceJavaApplicationTests {

    @Test
    void contextLoads() {
        OllamaAPI ollamaAPI = new OllamaAPI("http://localhost:11434/");
        ollamaAPI.setVerbose(true);
        boolean isOllamaRunning = ollamaAPI.ping();
        assertTrue(isOllamaRunning);
    }

    @Test
    void test() {
        System.out.println("test");

    }

}
