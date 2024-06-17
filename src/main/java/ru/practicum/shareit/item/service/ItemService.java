package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemCommentAndBookingDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;


public interface ItemService {
    ItemDto create(ItemDto itemDto, Long ownerId);

    ItemDto update(ItemDto itemDto, Long ownerId);

    ItemCommentAndBookingDto getItemById(Long userId, Long itemId);

    List<ItemCommentAndBookingDto> getItemsByOwner(Long ownerId);

    List<ItemDto> search(String text);

    CommentDto createComment(Long userId, Long itemId, CommentDto comment);

    List<CommentDto> getCommentsByItem(Long itemId);
}
