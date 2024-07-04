package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.dto.BookingOutputDto;

import java.util.List;

public interface BookingService {
    BookingOutputDto create(BookingInputDto booking, Long userId);

    BookingOutputDto confirmBooking(Long userId, Long bookingId, Boolean approved);

    BookingOutputDto getBookingById(Long userId, Long bookingId);

    List<BookingOutputDto> getBookingsByUser(Long userId, String state, Integer from, Integer size);

    List<BookingOutputDto> getBookingsByOwner(Long userId, String state, Integer from, Integer size);
}
