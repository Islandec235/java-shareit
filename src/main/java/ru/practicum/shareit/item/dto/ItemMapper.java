package ru.practicum.shareit.item.dto;

import lombok.NonNull;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Component
public class ItemMapper {
    public ItemDto toItemDto(@Valid Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getRentals(),
                item.getAvailable()
        );
    }

    public Item toItem(@Valid ItemDto itemDto) {
        return new Item(
                itemDto.getId(),
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getRentals(),
                itemDto.getAvailable()
        );
    }

    public ItemCommentAndBookingDto toItemWithCommentDto(@NonNull Item item) {
        return new ItemCommentAndBookingDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getRentals(),
                item.getAvailable()
        );
    }

    public List<ItemDto> listItemDto(@NonNull List<Item> items) {
        List<ItemDto> itemsDto = new ArrayList<>();

        for (Item item : items) {
            itemsDto.add(toItemDto(item));
        }

        return itemsDto;
    }

    public List<ItemCommentAndBookingDto> listItemCommentAndBookingDto(@NonNull List<Item> items) {
        List<ItemCommentAndBookingDto> itemsDto = new ArrayList<>();

        for (Item item : items) {
            itemsDto.add(toItemWithCommentDto(item));
        }

        return itemsDto;
    }

    public List<Item> listItem(@NonNull List<ItemDto> itemsDto, @NonNull Long ownerId) {
        List<Item> items = new ArrayList<>();

        for (ItemDto itemDto : itemsDto) {
            items.add(toItem(itemDto));
        }

        return items;
    }
}