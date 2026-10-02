package br.com.dataguardian.restaurante.core.domain;

public class User {
    private Long id;
    private String name;
    private String email;
    private UserType userType;

    public User() {}

    public User(Long id, String name, String email, UserType userType) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.userType = userType;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public UserType getUserType() { return userType; }
    public void setUserType(UserType userType) { this.userType = userType; }

    public void assignType(UserType type) {
        if (type == null) {
            throw new IllegalArgumentException("UserType cannot be null");
        }
        this.userType = type;
    }
}
