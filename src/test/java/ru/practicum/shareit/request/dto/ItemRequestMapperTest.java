package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.request.model.ItemRequest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ItemRequestMapperTest {
    private final ItemRequestMapper mapper = new ItemRequestMapper();
    private ItemRequestDto itemRequestDto;
    private ItemRequest itemRequest;

    @BeforeEach
    void setUp() {
        itemRequestDto = new ItemRequestDto(
                1L,
                "test",
                LocalDateTime.of(2024, 7, 1, 5, 17, 42));
        itemRequest = new ItemRequest(1L, "test", itemRequestDto.getCreated());
    }

    @Test
    public void shouldReturnItemRequestDto() {
        ItemRequestDto toDto = mapper.toItemRequestDto(itemRequest);

        assertEquals(toDto, itemRequestDto);
    }

    @Test
    public void shouldReturnItemRequest() {
        ItemRequest toEntity = mapper.toItemRequest(itemRequestDto);

        assertEquals(toEntity, itemRequest);
    }
}
