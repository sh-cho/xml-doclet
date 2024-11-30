package com.github.markusbernhardt.xmldoclet;

/**
 * Hi
 */
public class SimpleClass {

    /**
     * This is name
     */
    private String name;

    /**
     * This is age
     */
    private int age;

    /**
     * This is address
     */
    public String address;

    public SimpleClass(String name, int age, String address) {
        this.name = name;
        this.age = age;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
