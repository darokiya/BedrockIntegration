package com.app.playerservicejava.controller.test;

import com.app.playerservicejava.model.Prompt;
import com.app.playerservicejava.service.AssistantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("bedrock")
public class AssistantController {
    @Autowired
    private AssistantService assistantService;

    @GetMapping("prompt")
    public ResponseEntity<String> prompt(@RequestBody Prompt prompt) throws Exception {
        return new ResponseEntity<>(assistantService.askAssistant(prompt), HttpStatus.OK);

    }

    @GetMapping("llama4/askMerci")
    public ResponseEntity<String> promptLlama(@RequestBody Prompt prompt) throws Exception {
        return new ResponseEntity<>(assistantService.askAssistantLlama(prompt), HttpStatus.OK);

    }

    @GetMapping("deepSeek")
    public ResponseEntity<String> promptDeepSeek(@RequestBody Prompt prompt) throws Exception {
        return new ResponseEntity<>(assistantService.askAssistantDeepSeek(prompt), HttpStatus.OK);

    }

    @GetMapping("nova/askIsrael")
    public ResponseEntity<String> promptNova(@RequestBody Prompt prompt) throws Exception {
        return new ResponseEntity<>(assistantService.askAssistantNova(prompt), HttpStatus.OK);

    }
}
