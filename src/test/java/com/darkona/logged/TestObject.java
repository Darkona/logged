package com.darkona.toolset.objects;


import com.darkona.logged.annotation.Logged;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.darkona.toolset.App.faker;

@Data
public class TestObject {

    private String name;
    private int age;
    private String address;
    private String email;
    private String phone;
    private List<Animal> animalList;

    @Logged
    public String givePokemon(){
        return faker.pokemon().name();
    }

    @Logged
    public String explode()
    throws IllegalAccessException {
        throw new IllegalAccessException("BOOM!");
    }

    @Logged
    public void make(){
        name = faker.name().fullName();
        age = faker.number().numberBetween(1, 100);
        address = faker.address().fullAddress();
        email = "email@email.com";
        phone = faker.phoneNumber().cellPhone();
        animalList = new ArrayList<>();

        for (int i = 0; i < 50; i++) {
            animalList.add(new Animal(faker.animal().name(), faker.animal().species(), Map.of("color", faker.color().name())));
        }
    }

    public String doSomething(String a, String b, String c){
        return a + b + c;
    }

    public record Animal(String name, String species, Map<String, String> properties){};
}
