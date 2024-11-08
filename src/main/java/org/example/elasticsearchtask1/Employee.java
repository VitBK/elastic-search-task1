package org.example.elasticsearchtask1;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Employee {

    private String id;
    private String name;
    private String dob;
    private Address address;
    private String email;
    private String[] skills;
    private int experience;
    private double rating;
    private String description;
    @JsonProperty("description_suggest")
    private String descriptionSuggest;
    private boolean verified;
    private double salary;

    @Getter
    @Setter
    public static class Address {
        private String country;
        private String town;
    }
}
