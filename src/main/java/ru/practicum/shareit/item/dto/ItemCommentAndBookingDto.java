package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingWithBookerIdDto;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
@AllArgsConstructor
public class ItemCommentAndBookingDto {
    private Long id;
    @NotBlank
    private String name;
    @NotBlank
    private String description;
    private int rentals;
    private Boolean available;
    private BookingWithBookerIdDto lastBooking;
    private BookingWithBookerIdDto nextBooking;
    private List<CommentDto> comments;

    public ItemCommentAndBookingDto(Long id,
                                    String name,
                                    String description,
                                    int rentals,
                                    Boolean available) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.rentals = rentals;
        this.available = available;
    }
}
