package ru.practicum.shareit.item.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * TODO Sprint add-controllers.
 */
@Data
@AllArgsConstructor
public class Item {
    private Long id;
    private String name;
    private String description;
    private Long ownerId;
    private int rentals;
    private Boolean available;
}
