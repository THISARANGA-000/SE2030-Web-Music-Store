package com.melodymart.common.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "[ADMINISTRATOR]", schema = "dbo")
@PrimaryKeyJoinColumn(name = "AdminID")
public class Administrator extends User {

    @Column(name = "AdminRole", nullable = false, length = 50)
    private String adminRole = "Admin";

    public Administrator() {
        super();
    }

    public Administrator(String firstName, String lastName, String email, String passwordHash, String phoneNumber, String adminRole) {
        super(firstName, lastName, email, passwordHash, phoneNumber);
        this.adminRole = adminRole;
    }

    public String getAdminRole() {
        return adminRole;
    }

    public void setAdminRole(String adminRole) {
        this.adminRole = adminRole;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserId());
    }
}
