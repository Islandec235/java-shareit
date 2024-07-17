package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BookingMapperTest {
    private final BookingMapper mapper = new BookingMapper();
    private BookingOutputDto outputDto = new BookingOutputDto(
            1L,
            LocalDateTime.of(2023, 4, 4, 4, 4),
            LocalDateTime.of(2024, 4, 4, 4, 4),
            BookingStatus.WAITING);
    private Booking booking = new Booking(
            1L,
            LocalDateTime.of(2023, 4, 4, 4, 4),
            LocalDateTime.of(2024, 4, 4, 4, 4));
    private BookingInputDto inputDto = new BookingInputDto(
            1L,
            1L,
            LocalDateTime.of(2023, 4, 4, 4, 4),
            LocalDateTime.of(2024, 4, 4, 4, 4));

    @Test
    public void shouldReturnBookingOutputDto() {
        booking.setStatus(BookingStatus.WAITING);
        BookingOutputDto toDto = mapper.toBookingOutputDto(booking);

        assertEquals(toDto, outputDto);
    }

    @Test
    public void shouldReturnBooking() {
        Booking toEntity = mapper.toBooking(inputDto);

        assertEquals(toEntity, booking);
    }

    @Test
    public void shouldReturnBookingWithBookerIdDto() {
        booking.setStatus(BookingStatus.WAITING);
        booking.setBooker(new User(1L, "Test", "test@email.ru"));
        BookingWithBookerIdDto toDto = mapper.toBookingWithBookerIdDto(booking);
        booking.setBooker(null);

        assertEquals(toDto, new BookingWithBookerIdDto(1L, 1L));
    }
}
