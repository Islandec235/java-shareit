package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.model.Item;

import java.util.List;


public interface ItemService {
    Item create(Item item);

    Item update(Long itemId, Item item);

    Item getItemById(Long itemId);

    List<Item> getItemsByOwner(Long ownerId);

    List<Item> search(String text);
}
