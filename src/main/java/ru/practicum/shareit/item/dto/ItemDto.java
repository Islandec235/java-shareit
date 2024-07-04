package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * TODO Sprint add-controllers.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemDto {
    private Long id;
    @NotBlank
    private String name;
    @NotBlank
    private String description;
    private int rentals;
    private Boolean available;
    private Long requestId;

    public ItemDto(Long id, String name, String description, int rentals, Boolean available) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.rentals = rentals;
        this.available = available;
    }
}
