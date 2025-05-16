package com.app.playerservicejava.model;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@RequiredArgsConstructor
public class TestBody {
    private String name;
    private int age;
    @NonNull
    private String country;
    @JsonCreator
    public TestBody(@JsonProperty("name") String name,
                    @JsonProperty("age") int age,
                    @JsonProperty("country") @NonNull String country) {
        this.name = name;
        this.age = age;
        this.country = country;
    }
}
