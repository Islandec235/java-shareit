package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.exception.RequestNotFoundException;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestMapper requestMapper;
    private final ItemMapper itemMapper;
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ItemRequestDto create(Long userId, ItemRequestDto itemRequestDto) {
        ItemRequest itemRequest = requestMapper.toItemRequest(itemRequestDto);
        User user = userValidation(userId);
        itemRequest.setUser(user);
        itemRequest.setCreated(LocalDateTime.now());
        return requestMapper.toItemRequestDto(itemRequestRepository.save(itemRequest));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestDto> getItemRequests(Long userId) {
        userValidation(userId);
        List<ItemRequest> itemRequestList = itemRequestRepository.findByUserId(userId);
        List<ItemRequestDto> itemRequestDtoList = new ArrayList<>();

        for (ItemRequest request : itemRequestList) {
            List<Item> items = request.getItems();
            ItemRequestDto requestDto = requestMapper.toItemRequestDto(request);
            requestDto.setItems(itemMapper.listItemDto(items));
            itemRequestDtoList.add(requestDto);
        }

        return itemRequestDtoList;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestDto> getAllItemRequests(Long userId, Integer from, Integer size) {
        userValidation(userId);
        List<ItemRequest> requests = itemRequestRepository.findAllByUserIdNot(
                    userId,
                    PageRequest.of(from / size, size, Sort.by("created").descending()));
        List<ItemRequestDto> itemRequestDtoList = new ArrayList<>();

        for (ItemRequest request : requests) {
            List<Item> items = request.getItems();
            ItemRequestDto requestDto = requestMapper.toItemRequestDto(request);
            requestDto.setItems(itemMapper.listItemDto(items));
            itemRequestDtoList.add(requestDto);
        }

        return itemRequestDtoList;
    }

    @Override
    @Transactional(readOnly = true)
    public ItemRequestDto getItemRequestById(Long userId, Long requestId) {
        userValidation(userId);
        Optional<ItemRequest> request = itemRequestRepository.findById(requestId);

        if (request.isEmpty()) {
            log.error("Запрос предмета с id = {} не найден", requestId);
            throw new RequestNotFoundException("Не найден запрос предмета с id = " + requestId);
        }

        List<Item> items = request.get().getItems();
        ItemRequestDto requestDto = requestMapper.toItemRequestDto(request.get());
        requestDto.setItems(itemMapper.listItemDto(items));

        return requestDto;
    }

    private User userValidation(Long userId) {
        Optional<User> user = userRepository.findById(userId);

        if (user.isEmpty()) {
            log.error("Пользователь с id = {} не найден", userId);
            throw new UserNotFoundException("Не найден пользователь с id = " + userId);
        }

        return user.get();
    }
}
