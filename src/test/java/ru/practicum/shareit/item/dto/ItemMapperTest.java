package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ItemMapperTest {
    private final ItemMapper mapper = new ItemMapper();
    private Item item = new Item(1L, "Test", "test desc", 0, true);
    private ItemDto itemDto = new ItemDto(1L, "Test", "test desc", 0, true);
    private ItemCommentAndBookingDto itemCommentAndBookingDto =
            new ItemCommentAndBookingDto(1L, "Test", "test desc", 0, true);

    @Test
    public void shouldReturnItemDto() {
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
