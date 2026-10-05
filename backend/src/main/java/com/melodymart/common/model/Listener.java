package com.melodymart.common.model;

import jakarta.persistence.*;

@Entity
@Table(name = "[LISTENER]", schema = "dbo")
@PrimaryKeyJoinColumn(name = "ListenerID")
public class Listener extends User {

    public Listener() {
        super();
    }

    public Listener(String firstName, String lastName, String email, String passwordHash, String phoneNumber) {
        super(firstName, lastName, email, passwordHash, phoneNumber);
    }
}
