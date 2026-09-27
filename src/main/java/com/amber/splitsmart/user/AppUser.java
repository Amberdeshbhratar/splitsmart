package com.amber.splitsmart.user;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Locale;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class AppUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 120) private String name;
    @Column(nullable = false, length = 254) private String email;
    @Column(nullable = false) private String passwordHash;
    protected AppUser() { }
    public AppUser(String name, String email, String passwordHash) { this.name = name; this.email = email.trim().toLowerCase(Locale.ROOT); this.passwordHash = passwordHash; }
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    @JsonIgnore
    public String getPasswordHash() { return passwordHash; }
}
