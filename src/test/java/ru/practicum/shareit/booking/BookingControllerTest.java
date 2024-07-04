package ru.practicum.shareit.booking;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.dto.BookingOutputDto;
import ru.practicum.shareit.booking.dto.BookingWithBookerIdDto;
import ru.practicum.shareit.booking.service.BookingService;

@WebMvcTest(BookingController.class)
public class BookingControllerTest {
    @Mock
    private BookingService service;
    @InjectMocks
    private MockMvc mvc;
    private BookingInputDto inputDto;
    private BookingOutputDto outputDto;
    private BookingWithBookerIdDto withBookerIdDto;
}
