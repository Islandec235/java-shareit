package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */
@Data
@AllArgsConstructor
public class BookingInputDto {
    private Long id;
    private Long itemId;
    @NonNull
    @FutureOrPresent
    private LocalDateTime start;
    @NonNull
    @Future
    private LocalDateTime end;
}
