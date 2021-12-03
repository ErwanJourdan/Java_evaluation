package com.freestack.evaluation.models;


import javax.persistence.*;

@Entity
@Table(name = "uber_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String firstname;
    @Column(nullable = false)
    private String lastname;

    public User() {}

    public User(String firstname, String lastname) {
        this.firstname = firstname;
        this.lastname = lastname;
    }

    @Override
    public String toString() {
        return ("Client : "+ firstname + " " + lastname);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }
}