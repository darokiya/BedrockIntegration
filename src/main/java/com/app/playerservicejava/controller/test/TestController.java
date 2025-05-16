package com.app.playerservicejava.controller.test;

import com.app.playerservicejava.model.TestBody;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.DeleteMapping;

@RestController
@RequestMapping(value = "test/myTest", produces = {MediaType.APPLICATION_JSON_VALUE})
public class TestController {

    @GetMapping("get")
    public String getMethod(@RequestParam String name, @RequestParam int value) {
        return String.format(" Hello %s the value is %d", name, value);
    }

    @GetMapping("path/{name}/{id}")
    public String getPath(@PathVariable String name, @PathVariable int id) {
        return String.format(" Hello %s the value is %d", name, id);
    }

    @PostMapping("requestBody")
    public TestBody getPath(@RequestBody TestBody testBody) {
        //return String.format(" Hello %s", testBody.getName());
        return testBody;
    }

    @PutMapping("putBody")
    public TestBody putBody(@RequestBody TestBody testBody) {
        return testBody;
    }

    @DeleteMapping("deleteBody/{name}/{age}")
    public TestBody deleteBody(@PathVariable String name, @PathVariable int age) {
        return new TestBody(name, age, "USA");
    }
}
