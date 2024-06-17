package ru.practicum.shareit.booking.dto;

import lombok.NonNull;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.model.Booking;

@Component
public class BookingMapper {
    public BookingOutputDto toBookingOutputDto(@NonNull Booking booking) {
        return new BookingOutputDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getStatus()
        );
    }

    public Booking toBooking(@NonNull BookingInputDto bookingInputDto) {
        return new Booking(
                bookingInputDto.getId(),
                bookingInputDto.getStart(),
                bookingInputDto.getEnd()
        );
    }

    public BookingWithBookerIdDto toBookingWithBookerIdDto(@NonNull Booking booking) {
        return new BookingWithBookerIdDto(
                booking.getId(),
                booking.getBooker().getId()
        );
    }
}
