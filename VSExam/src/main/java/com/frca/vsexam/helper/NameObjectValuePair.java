package com.frca.vsexam.helper;

/**
 * Created by KillerFrca on 18.11.13.
 */
public class NameObjectValuePair  {
    private String name;
    private Object value;

    public NameObjectValuePair(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }
}
