package ru.practicum.shareit.item.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.exceptions.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.storage.UserStorage;

import javax.validation.ValidationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class InMemoryItemStorage implements ItemStorage {
    private long id = 1L;
    private final HashMap<Long, Item> items = new HashMap<>();
    private final UserStorage userStorage;

    @Override
    public Item createItem(Item item) {
        if (item.getAvailable() == null) {
            log.error(String.valueOf(item));
            throw new ValidationException("Поле аренды не может быть пустым");
        }
        userStorage.getUserById(item.getOwnerId());
        item.setId(id);
        items.put(item.getId(), item);
        log.info("Запрос на создание предмета");
        this.id++;
        return items.get(item.getId());
    }

    @Override
    public Item updateItem(Long itemId, Item item) {
        if (!items.containsKey(itemId) || !item.getOwnerId().equals(items.get(itemId).getOwnerId())) {
            log.error(String.valueOf(itemId));
            throw new ItemNotFoundException("Предмет не найден");
        } else {
            Item itemInStorage = items.get(itemId);
            if (item.getAvailable() != null && item.getDescription() != null && item.getName() != null) {
                items.put(itemId, item);
                log.info("Запрос на обновление предмета");
                return items.get(itemId);
            } else if (item.getAvailable() != null) {
                itemInStorage.setAvailable(item.getAvailable());
                return items.get(itemId);
            } else if (item.getName() != null) {
                itemInStorage.setName(item.getName());
                return items.get(itemId);
            } else if (item.getDescription() != null) {
                itemInStorage.setDescription(item.getDescription());
                return items.get(itemId);
            } else {
                log.error(String.valueOf(item));
                throw new ItemNotFoundException("Предмет не найден");
            }
        }
    }

    @Override
    public Item getItemById(Long itemId) {
        if (!items.containsKey(itemId)) {
            log.error(String.valueOf(itemId));
            throw new ItemNotFoundException("Предмет не найден");
        } else {
            log.info("Запрос на получение предмета по id");
            return items.get(itemId);
        }
    }

    @Override
    public List<Item> getItemsByOwner(Long ownerId) {
        List<Item> itemsByOwner = new ArrayList<>();

        for (Item item : items.values()) {
            if (item.getOwnerId().equals(ownerId)) {
                itemsByOwner.add(item);
            }
        }

        log.info("Запрос на получение предметов владельца");
        return itemsByOwner;
    }

    @Override
    public List<Item> search(String text) {
        List<Item> searchItems = new ArrayList<>();

        if (text.isBlank()) {
            return searchItems;
        }

        String lowerText = text.toLowerCase();

        for (Item item : items.values()) {
            if ((item.getName().toLowerCase().contains(lowerText)
                    || item.getDescription().toLowerCase().contains(lowerText))
                    && item.getAvailable()) {
                searchItems.add(item);
            }
        }

        log.info("Запрос на поиск предметов");
        return searchItems;
    }
}
