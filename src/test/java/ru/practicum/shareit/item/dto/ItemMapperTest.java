package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ItemMapperTest {
    private final ItemMapper mapper = new ItemMapper();
    private Item item;
    private ItemDto itemDto;
    private ItemCommentAndBookingDto itemCommentAndBookingDto;

    @BeforeEach
    void setUp() {
        item = new Item(1L, "Test", "test desc", 0, true);
        itemDto = new ItemDto(1L, "Test", "test desc", 0, true);
        itemCommentAndBookingDto =
                new ItemCommentAndBookingDto(1L, "Test", "test desc", 0, true);
    }

    @Test
    public void shouldReturnItemDto() {
        ItemRequest request = new ItemRequest(
                1L,
                "desc",
                LocalDateTime.of(2024, 1, 1, 1, 1, 1));
        item.setRequest(request);
        ItemDto toDto = mapper.toItemDto(item);
        itemDto.setRequestId(1L);

        assertEquals(itemDto, toDto);
    }

    @Test
    public void shouldReturnItemDtoWithRequestNull() {
        ItemDto toDto = mapper.toItemDto(item);

        assertEquals(itemDto, toDto);
    }

    @Test
    public void shouldReturnItem() {
        Item toEntity = mapper.toItem(itemDto);

        assertEquals(item, toEntity);
    }

    @Test
    public void shouldReturnItemCommentAndBookingDto() {
        ItemCommentAndBookingDto toDto = mapper.toItemWithCommentDto(item);

        assertEquals(itemCommentAndBookingDto, toDto);
    }

    @Test
    public void shouldReturnListItemDto() {
        Item otherItem = new Item(2L, "Test2", "test desc2", 1, true);
        ItemDto otherItemDto = new ItemDto(2L, "Test2", "test desc2", 1, true);
        List<Item> itemList = List.of(item, otherItem);
        List<ItemDto> itemDtoList = List.of(itemDto, otherItemDto);


        List<ItemDto> toDto = mapper.listItemDto(itemList);

        assertEquals(toDto, itemDtoList);
    }
}
