package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.dto.BookingOutputDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingControllerTest {
    final ObjectMapper mapper;
    @MockBean
    final BookingService service;
    private final MockMvc mvc;
    private final BookingInputDto inputDto = new BookingInputDto(
            1L,
            LocalDateTime.of(2030, 4, 4, 4, 4, 42),
            LocalDateTime.of(2031, 4, 4, 5, 4, 42));
    private final BookingOutputDto outputDto = new BookingOutputDto(
            1L,
            LocalDateTime.of(2030, 4, 4, 4, 4, 42),
            LocalDateTime.of(2031, 4, 4, 5, 4, 42),
            BookingStatus.WAITING);

    @Test
    public void createBooking() throws Exception {
        when(service.create(any(), any())).thenReturn(outputDto);

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(inputDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(outputDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(String.valueOf(outputDto.getStart()))))
                .andExpect(jsonPath("$.end", is(String.valueOf(outputDto.getEnd()))))
                .andExpect(jsonPath("$.status", is(String.valueOf(outputDto.getStatus()))));
    }

    @Test
    public void shouldConfirmBooking() throws Exception {
        when(service.confirmBooking(any(), any(), any())).thenReturn(outputDto);

        mvc.perform(patch("/bookings/{bookingId}", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(outputDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(String.valueOf(outputDto.getStart()))))
                .andExpect(jsonPath("$.end", is(String.valueOf(outputDto.getEnd()))))
                .andExpect(jsonPath("$.status", is(String.valueOf(outputDto.getStatus()))));
    }

    @Test
    public void shouldGetBookingById() throws Exception {
        when(service.getBookingById(any(), any())).thenReturn(outputDto);

        mvc.perform(get("/bookings/{bookingId}", 1L)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(outputDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(String.valueOf(outputDto.getStart()))))
                .andExpect(jsonPath("$.end", is(String.valueOf(outputDto.getEnd()))))
                .andExpect(jsonPath("$.status", is(String.valueOf(outputDto.getStatus()))));
    }

    @Test
    public void shouldGetBookingByUser() throws Exception {
        when(service.getBookingsByUser(any(), any(), any(), any())).thenReturn(Collections.singletonList(outputDto));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(Collections.singletonList(outputDto))));
    }

    @Test
    public void shouldGetBookingByOwner() throws Exception {
        when(service.getBookingsByOwner(any(), any(), any(), any())).thenReturn(Collections.singletonList(outputDto));

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(Collections.singletonList(outputDto))));
    }
}
