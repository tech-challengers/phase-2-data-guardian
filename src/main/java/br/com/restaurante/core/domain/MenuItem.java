package br.com.restaurante.core.domain;

import java.math.BigDecimal;

public class MenuItem {

    private Long id;
    private Long restaurantId;
    private String name;
    private String description;
    private BigDecimal price;
    private boolean dineInOnly;
    private String photoPath;

    public MenuItem(Long id, Long restaurantId, String name, String description,
                    BigDecimal price, boolean dineInOnly, String photoPath) {
        validate(name, price);
        this.id = id;
        this.restaurantId = restaurantId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.dineInOnly = dineInOnly;
        this.photoPath = photoPath;
    }

    public void update(String name, String description, BigDecimal price,
                       boolean dineInOnly, String photoPath) {
        validate(name, price);
        this.name = name;
        this.description = description;
        this.price = price;
        this.dineInOnly = dineInOnly;
        this.photoPath = photoPath;
    }

    private static void validate(String name, BigDecimal price) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (price == null || price.signum() < 0) {
            throw new IllegalArgumentException("Price must be zero or greater");
        }
    }

    public Long getId() { return id; }
    public Long getRestaurantId() { return restaurantId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public boolean isDineInOnly() { return dineInOnly; }
    public String getPhotoPath() { return photoPath; }
}