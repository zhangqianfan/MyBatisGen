package org.entity;

import org.annotation.PrimaryKey;
import org.annotation.Unique;

import java.util.Objects;

public class Person {

    @PrimaryKey
    private final Integer id;
    @Unique
    private String name;
    private Integer age;
    private String homeAddress;
    private String companyAddress;

    public Person(Integer id, String name, Integer age, String homeAddress, String companyAddress) {
        this.id = (id == null) ? 0 : id;
        this.name = (name == null) ? "null" : name;
        this.age = (age == null) ? 0 : age;
        this.homeAddress = (homeAddress == null) ? "null" : homeAddress;
        this.companyAddress = (companyAddress == null) ? "null" : companyAddress;
    }

    public Person() {
        this.id = 0;
        this.name = "null";
        this.age = 0;
        this.homeAddress = "null";
        this.companyAddress = "null";
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getHomeAddress() {
        return homeAddress;
    }

    public void setHomeAddress(String homeAddress) {
        this.homeAddress = homeAddress;
    }

    public String getCompanyAddress() {
        return companyAddress;
    }

    public void setCompanyAddress(String companyAddress) {
        this.companyAddress = companyAddress;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Person per = (Person) obj;
        return Objects.equals(id, per.id) &&
                Objects.equals(name, per.name) &&
                Objects.equals(age, per.age) &&
                Objects.equals(homeAddress, per.homeAddress) &&
                Objects.equals(companyAddress, per.companyAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, age, homeAddress, companyAddress);
    }

    @Override
    public String toString() {
        return "Person {" +
                "id = " + id +
                ", name = '" + name + '\'' +
                ", age = " + age +
                ", homeAddress = '" + homeAddress + '\'' +
                ", companyAddress = '" + companyAddress + '\'' +
                '}';
    }
}
