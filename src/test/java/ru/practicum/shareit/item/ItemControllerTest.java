package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingWithBookerIdDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemCommentAndBookingDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemControllerTest {
    final ObjectMapper mapper;
    @MockBean
    final ItemService service;
    private final MockMvc mvc;

    private ItemDto itemDto = new ItemDto(
            1L,
            "test",
            "testDesc",
            0,
            true,
            1L);

    @Test
    public void createItem() throws Exception {
        when(service.create(itemDto, 1L)).thenReturn(itemDto);

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable()), Boolean.class));
    }

    @Test
    public void updateItem() throws Exception {
        when(service.update(itemDto, 1L)).thenReturn(itemDto);

        mvc.perform(patch("/items/{itemId}", itemDto.getId())
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable()), Boolean.class));
    }

    @Test
    public void getItemById() throws Exception {
        ItemCommentAndBookingDto item = new ItemCommentAndBookingDto(
                1L,
                "test",
                "desc",
                0,
                true,
                new BookingWithBookerIdDto(1L, 1L),
                new BookingWithBookerIdDto(2L, 1L),
                List.of(new CommentDto(1L, "Test comment", "Gena", Instant.now())));
        when(service.getItemById(1L, 1L)).thenReturn(item);

        mvc.perform(get("/items/{itemId}", item.getId())
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1L), Long.class))
                .andExpect(jsonPath("$.name", is(item.getName())))
                .andExpect(jsonPath("$.description", is(item.getDescription())))
                .andExpect(jsonPath("$.rentals", is(item.getRentals())))
                .andExpect(jsonPath("$.available", is(item.getAvailable()), Boolean.class))
                .andExpect(jsonPath("$.lastBooking.id", is(item.getLastBooking().getId()), Long.class))
                .andExpect(jsonPath("$.lastBooking.bookerId",
                        is(item.getLastBooking().getBookerId()), Long.class))
                .andExpect(jsonPath("$.nextBooking.id",
                        is(item.getNextBooking().getId()), Long.class))
                .andExpect(jsonPath("$.nextBooking.bookerId",
                        is(item.getNextBooking().getBookerId()), Long.class))
                .andExpect(jsonPath("$.comments[0].id", is(item.getComments().get(0).getId()), Long.class))
                .andExpect(jsonPath("$.comments[0].text", is(item.getComments().get(0).getText())))
                .andExpect(jsonPath("$.comments[0].authorName", is(item.getComments().get(0).getAuthorName())))
                .andExpect(jsonPath("$.comments[0].created",
                        is(String.valueOf(item.getComments().get(0).getCreated()))));
    }

    @Test
    public void getItemsByOwnerId() throws Exception {
        ItemCommentAndBookingDto itemCommentAndBookingDto = new ItemCommentAndBookingDto(1L,
                "test",
                "testDesc",
                0,
                true);
        ItemCommentAndBookingDto otherItem = new ItemCommentAndBookingDto(1L,
                "newTest",
                "desc",
                1,
                true);
        when(service.getItemsByOwner(any(), eq(0), eq(20)))
                .thenReturn(List.of(itemCommentAndBookingDto, otherItem));

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content()
                        .json(mapper.writeValueAsString(Arrays.asList(itemCommentAndBookingDto, otherItem))));
    }

    @Test
    public void search() throws Exception {
        when(service.search(any(), eq(0), eq(20))).thenReturn(Collections.singletonList(itemDto));

        mvc.perform(get("/items/search")
                        .param("text", "test"))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(Collections.singletonList(itemDto))));
    }

    @Test
    public void createComment() throws Exception {
        CommentDto commentDto = new CommentDto(1L, "test comment", "Gena", Instant.now());
        when(service.createComment(any(), any(), any())).thenReturn(commentDto);

        mvc.perform(post("/items/{itemId}/comment", 1L)
                        .content(mapper.writeValueAsString(commentDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentDto.getText())))
                .andExpect(jsonPath("$.authorName", is(commentDto.getAuthorName())))
                .andExpect(jsonPath("$.created", is(String.valueOf(commentDto.getCreated()))));
    }
}
