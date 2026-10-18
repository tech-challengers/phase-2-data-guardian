package br.com.restaurante.core.domain;

public class UserType {
    public static final String DONO_DE_RESTAURANTE = "DONO_DE_RESTAURANTE";
    public static final String CLIENTE = "CLIENTE";

    private Long id;
    private String name;

    public UserType() {}

    public UserType(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        this.name = name;
    }
}
