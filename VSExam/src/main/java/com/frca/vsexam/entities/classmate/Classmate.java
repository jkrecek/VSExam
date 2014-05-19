package com.frca.vsexam.entities.classmate;

import com.frca.vsexam.entities.base.ParentEntity;

import java.util.Date;

public class Classmate extends ParentEntity {

    private String name;
    private Date registered;
    private String identification;

    public Classmate(int id) {
        super(id);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getRegistered() {
        return registered;
    }

    public void setRegistered(Date registered) {
        this.registered = registered;
    }

    public String getIdentification() {
        return identification;
    }

    public void setIdentification(String identification) {
        this.identification = identification;
    }
}
