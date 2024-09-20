package com.darkona.logged;


import com.darkona.logged.annotation.Logged;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class TestObject {

    private String name;
    private int age;
    private String address;
    private String email;
    private String phone;
    private List<Pokemon> pokemonList;

    @Logged
    public String givePokemon(){
        return "Bulbasaur";
    }

    @Logged
    public String explode()
    throws IllegalAccessException {
        throw new IllegalAccessException("BOOM!");
    }

    @Logged
    public void make(){
        name = "Dr. Oak";
        age = 32;
        address = "1234 Elm St.";
        email = "email@email.com";
        phone = "555-555-5555";
        pokemonList = new ArrayList<>();

        for (int i = 0; i < 50; i++) {
            pokemonList.add(new Pokemon("Bulbasaur", 1, Map.of("Move 1", "Tackle", "Move 2", "Growl")));
        }
    }

    TestObject() {
    }

    String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    int getAge() {
        return age;
    }

    void setAge(int age) {
        this.age = age;
    }

    String getAddress() {
        return address;
    }

    void setAddress(String address) {
        this.address = address;
    }

    String getEmail() {
        return email;
    }

    void setEmail(String email) {
        this.email = email;
    }

    String getPhone() {
        return phone;
    }

    void setPhone(String phone) {
        this.phone = phone;
    }

    List<Pokemon> getPokemonList() {
        return pokemonList;
    }

    void setPokemonList(List<Pokemon> pokemonList) {
        this.pokemonList = pokemonList;
    }

    public String doSomething(String a, String b, String c){
        return a + b + c;
    }

    public record Pokemon(String name, int pokeDexNumber, Map<String, String> properties){};
}
