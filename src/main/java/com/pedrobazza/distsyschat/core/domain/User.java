package com.pedrobazza.distsyschat.core.domain;

import java.io.Serializable;
import java.util.Objects;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String username;

    public User(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username não pode ser vazio.");
        }
        this.username = username.trim();
    }

    public String getUsername() {
        return username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    @Override
    public String toString() {
        return username;
    }
}
