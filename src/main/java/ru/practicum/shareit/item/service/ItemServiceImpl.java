package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;

    @Override
    public Item createItem(Item item) {
        return itemStorage.createItem(item);
    }

    @Override
    public Item updateItem(Long itemId, Item item) {
        return itemStorage.updateItem(itemId, item);
    }

    @Override
    public Item getItemById(Long itemId) {
        return itemStorage.getItemById(itemId);
    }

    @Override
    public List<Item> getItemsByOwner(Long ownerId) {
        return itemStorage.getItemsByOwner(ownerId);
    }

    @Override
    public List<Item> search(String text) {
        return itemStorage.search(text);
    }
}
