package ru.practicum.shareit.item.dto;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.List;

@Component
public class ItemMapper {
    public ItemDto toItemDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getRentals(),
                item.getAvailable()
        );
    }

    public Item toItem(ItemDto itemDto, Long ownerId) {
        return new Item(
                itemDto.getId(),
                itemDto.getName(),
                itemDto.getDescription(),
                ownerId,
                itemDto.getRentals(),
                itemDto.getAvailable()
        );
    }

    public List<ItemDto> collectionToItemDto(List<Item> items) {
        List<ItemDto> itemsDto = new ArrayList<>();

        for (Item item : items) {
            itemsDto.add(toItemDto(item));
        }

        return itemsDto;
    }

    public List<Item> collectionToItem(List<ItemDto> itemsDto, Long ownerId) {
        List<Item> items = new ArrayList<>();

        for (ItemDto itemDto : itemsDto) {
            items.add(toItem(itemDto, ownerId));
        }

        return items;
    }
}
