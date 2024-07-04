package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.exception.RequestNotFoundException;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Transactional
@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestServiceTest {
    @Mock
    ItemRequestRepository mockRequestRepository;
    @Mock
    ItemRepository mockItemRepository;
    @Mock
    UserRepository mockUserRepository;
    @Mock
    ItemMapper mockItemMapper;
    @Mock
    ItemRequestMapper mockRequestMapper;
    @InjectMocks
    ItemRequestServiceImpl service;

    private User user;
    private Item item;
    private ItemRequest request;
    private ItemRequestDto requestDto;


    @BeforeEach
    void setUp() {
        user = new User(1L, "TestName", "test@mail.ru");
        item = new Item(1L, "Test", "test desc", 0, true);
        requestDto = new ItemRequestDto(1L, "descTest", LocalDateTime.now());
        request = new ItemRequest(1L, "descTest", requestDto.getCreated());
        request.setUser(user);
        request.setItems(List.of(item));
    }

    @Test
    public void shouldCreateRequest() {
        when(mockUserRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(mockRequestMapper.toItemRequest(requestDto)).thenReturn(request);
        when(mockRequestRepository.save(request)).thenReturn(request);
        when(mockRequestMapper.toItemRequestDto(request)).thenReturn(requestDto);

        ItemRequestDto createdRequest = service.create(user.getId(), requestDto);

        assertEquals(createdRequest, requestDto);
    }

    @Test
    public void shouldReturnExceptionForCreateRequest() {
        when(mockUserRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> service.create(user.getId(), requestDto));
    }

    @Test
    public void shouldReturnRequests() {
        ItemDto itemDto = new ItemDto(1L, "Test", "test desc", 0, true);
        when(mockUserRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(mockRequestRepository.findByUserId(user.getId())).thenReturn(Collections.singletonList(request));
        when(mockItemMapper.listItemDto(any())).thenReturn(List.of(itemDto));
        when(mockRequestMapper.toItemRequestDto(request)).thenReturn(requestDto);

        List<ItemRequestDto> requests = service.getItemRequests(user.getId());

        assertEquals(requests, List.of(requestDto));
    }

    @Test
    public void shouldReturnAllRequests() {
        ItemDto itemDto = new ItemDto(1L, "Test", "test desc", 0, true);
        ItemRequest otherRequest = new ItemRequest(2L, "descTest", LocalDateTime.now());
        ItemRequestDto otherRequestDto = new ItemRequestDto(2L, "descTest", otherRequest.getCreated());

        when(mockUserRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(mockRequestRepository.findByUserIdNotOrderByCreatedDesc(any())).thenReturn(List.of(otherRequest, request));
        when(mockItemMapper.listItemDto(any())).thenReturn(List.of(itemDto));
        when(mockRequestMapper.toItemRequestDto(request)).thenReturn(requestDto);
        when(mockRequestMapper.toItemRequestDto(otherRequest)).thenReturn(otherRequestDto);

        List<ItemRequestDto> requests = service.getAllItemRequests(user.getId(), null, null);

        assertEquals(requests, List.of(otherRequestDto, requestDto));
    }

    @Test
    public void shouldReturnNotFoundForGetItemByRequest() {
        when(mockUserRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(mockRequestRepository.findById(request.getId())).thenReturn(Optional.empty());

        assertThrows(RequestNotFoundException.class,
                () -> service.getItemRequestById(user.getId(), request.getId()));
    }
}
