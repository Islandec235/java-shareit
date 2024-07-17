package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingWithBookerIdDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentMapper;
import ru.practicum.shareit.item.dto.ItemCommentAndBookingDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import javax.validation.ValidationException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@Transactional
@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceTest {
    @Mock
    ItemRepository mockItemRepository;
    @Mock
    UserRepository mockUserRepository;
    @Mock
    CommentRepository mockCommentRepository;
    @Mock
    ItemRequestRepository mockItemRequestRepository;
    @Mock
    BookingRepository mockBookingRepository;
    @Mock
    ItemMapper mockItemMapper;
    @Mock
    CommentMapper mockCommentMapper;
    @Mock
    BookingMapper mockBookingMapper;
    @InjectMocks
    ItemServiceImpl service;

    private User owner;
    private Item item;
    private ItemDto itemDto;

    @BeforeEach
    void setUp() {
        owner = new User(1L, "Victor", "test@mail.ru");
        item = new Item(1L, "Notebook", "testDesc", 0, true);
        itemDto = new ItemDto(1L, "Notebook", "testDesc", 0, true);

    }

    @Test
    public void shouldCreateItem() {
        User userForRequest = new User(2L, "Gena", "yandex@yandex.ru");
        Item createdItem = new Item(1L, "Notebook", "testDesc", 0, true);
        ItemRequest request = new ItemRequest(1L, "Test desc", LocalDateTime.now());
        itemDto.setRequestId(1L);
        request.setUser(userForRequest);
        ItemDto createdItemDto = new ItemDto(1L, "Notebook", "testDesc", 0, true);
        createdItemDto.setRequestId(request.getId());
        createdItem.setOwner(owner);

        when(mockItemMapper.toItem(itemDto)).thenReturn(item);
        when(mockUserRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(mockItemRepository.save(item)).thenReturn(createdItem);
        when(mockItemMapper.toItemDto(createdItem)).thenReturn(createdItemDto);
        when(mockItemRequestRepository.findById(request.getId())).thenReturn(Optional.of(request));

        ItemDto newItemDto = service.create(itemDto, owner.getId());

        assertEquals(newItemDto, createdItemDto);
    }

    @Test
    public void shouldReturnExceptionForCreateWithEmptyOwner() {
        when(mockItemMapper.toItem(itemDto)).thenReturn(item);
        when(mockUserRepository.findById(owner.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.create(itemDto, owner.getId()));
    }

    @Test
    public void shouldReturnExceptionForCreateWithNotFoundRequest() {
        itemDto.setRequestId(1L);

        when(mockItemMapper.toItem(itemDto)).thenReturn(item);
        when(mockUserRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(mockItemRequestRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.create(itemDto, owner.getId()));
    }

    @Test
    public void shouldCreateWithoutRequest() {
        Item createdItem = new Item(1L, "Notebook", "testDesc", 0, true);
        ItemDto createdItemDto = new ItemDto(1L, "Notebook", "testDesc", 0, true);

        when(mockItemMapper.toItem(itemDto)).thenReturn(item);
        when(mockUserRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(mockItemRepository.save(item)).thenReturn(createdItem);
        when(mockItemMapper.toItemDto(createdItem)).thenReturn(createdItemDto);

        ItemDto newItemDto = service.create(itemDto, owner.getId());

        assertEquals(newItemDto, createdItemDto);
    }

    @Test
    public void shouldUpdate() {
        Item itemInDb = new Item(1L, "Test", "test", 0, true);
        itemInDb.setOwner(owner);

        when(mockUserRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(mockItemRepository.findById(itemDto.getId())).thenReturn(Optional.of(itemInDb));
        when(mockItemMapper.toItem(itemDto)).thenReturn(item);
        when(mockItemRepository.save(item)).thenReturn(item);
        when(mockItemMapper.toItemDto(item)).thenReturn(itemDto);

        ItemDto updatedItem = service.update(itemDto, item.getId(), owner.getId());

        assertEquals(updatedItem, itemDto);
    }

    @Test
    public void shouldReturnExceptionForUpdateWithEmptyOwner() {
        when(mockUserRepository.findById(owner.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.update(itemDto, item.getId(), owner.getId()));
    }

    @Test
    public void shouldReturnNotFoundExceptionForUpdate() {
        when(mockUserRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(mockItemRepository.findById(itemDto.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.update(itemDto, item.getId(), owner.getId()));
    }

    @Test
    public void shouldReturnExceptionForUpdateNotOwner() {
        User user = new User(2L, "Gena", "yandex@yandex.ru");
        Item itemInDb = new Item(1L, "Test", "test", 0, true);
        itemInDb.setOwner(owner);

        when(mockUserRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(mockItemRepository.findById(itemDto.getId())).thenReturn(Optional.of(itemInDb));

        assertThrows(ConflictException.class,
                () -> service.update(itemDto, item.getId(), user.getId()));
    }

    @Test
    public void shouldUpdateWithAvailable() {
        itemDto.setName(null);
        itemDto.setDescription(null);
        item.setName(null);
        item.setDescription(null);
        Item itemInDb = new Item(1L, "Test", "test", 0, true);
        itemInDb.setOwner(owner);

        when(mockUserRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(mockItemRepository.findById(itemDto.getId())).thenReturn(Optional.of(itemInDb));
        when(mockItemMapper.toItem(itemDto)).thenReturn(item);
        when(mockItemRepository.save(item)).thenReturn(item);
        when(mockItemMapper.toItemDto(item)).thenReturn(itemDto);

        ItemDto updatedItem = service.update(itemDto, item.getId(), owner.getId());
        itemDto.setDescription("test");
        itemDto.setName("Test");

        assertEquals(itemDto, updatedItem);
    }

    @Test
    public void shouldUpdateWithName() {
        itemDto.setAvailable(null);
        itemDto.setDescription(null);
        item.setAvailable(null);
        item.setDescription(null);
        Item itemInDb = new Item(1L, "Test", "test", 0, true);
        itemInDb.setOwner(owner);

        when(mockUserRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(mockItemRepository.findById(itemDto.getId())).thenReturn(Optional.of(itemInDb));
        when(mockItemMapper.toItem(itemDto)).thenReturn(item);
        when(mockItemRepository.save(item)).thenReturn(item);
        when(mockItemMapper.toItemDto(item)).thenReturn(itemDto);

        ItemDto updatedItem = service.update(itemDto, item.getId(), owner.getId());
        itemDto.setDescription("test");
        itemDto.setAvailable(true);

        assertEquals(itemDto, updatedItem);
    }

    @Test
    public void shouldUpdateWithDescription() {
        itemDto.setName(null);
        itemDto.setAvailable(null);
        item.setName(null);
        item.setAvailable(null);
        Item itemInDb = new Item(1L, "Test", "test", 0, true);
        itemInDb.setOwner(owner);

        when(mockUserRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(mockItemRepository.findById(itemDto.getId())).thenReturn(Optional.of(itemInDb));
        when(mockItemMapper.toItem(itemDto)).thenReturn(item);
        when(mockItemRepository.save(item)).thenReturn(item);
        when(mockItemMapper.toItemDto(item)).thenReturn(itemDto);

        ItemDto updatedItem = service.update(itemDto, item.getId(), owner.getId());
        itemDto.setAvailable(true);
        itemDto.setName("Test");

        assertEquals(itemDto, updatedItem);
    }

    @Test
    public void shouldReturnExceptionForUpdateWithoutItem() {
        itemDto.setAvailable(null);
        itemDto.setName(null);
        itemDto.setDescription(null);
        item.setAvailable(null);
        item.setName(null);
        item.setDescription(null);
        Item itemInDb = new Item(1L, "Test", "test", 0, true);
        itemInDb.setOwner(owner);

        when(mockUserRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(mockItemRepository.findById(itemDto.getId())).thenReturn(Optional.of(itemInDb));

        assertThrows(NotFoundException.class,
                () -> service.update(itemDto, item.getId(), owner.getId()));
    }

    @Test
    public void shouldReturnItemById() {
        ItemCommentAndBookingDto itemCommentDto =
                new ItemCommentAndBookingDto(1L, "Notebook", "testDesc", 0, true);
        item.setOwner(owner);

        when(mockItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(mockItemMapper.toItemWithCommentDto(item)).thenReturn(itemCommentDto);
        when(mockBookingRepository.findByItemIdAndStatusIsAndStartBeforeOrderByStartDesc(
                eq(1L),
                eq(BookingStatus.APPROVED),
                any())).thenReturn(Collections.emptyList());
        when(mockBookingRepository.findByItemIdAndStatusIsAndStartAfterOrderByStartAsc(
                eq(1L),
                eq(BookingStatus.APPROVED),
                any())).thenReturn(Collections.emptyList());
        when(mockCommentRepository.findByItemId(itemDto.getId())).thenReturn(Collections.emptyList());
        when(mockCommentMapper.listCommentDto(any())).thenReturn(Collections.emptyList());

        ItemCommentAndBookingDto itemById = service.getItemById(owner.getId(), item.getId());

        assertEquals(itemById, itemCommentDto);
    }

    @Test
    public void shouldReturnExceptionForItemByIdWithoutItem() {
        when(mockItemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.getItemById(1L, 1L));
    }

    @Test
    public void shouldReturnItemByIdWithBookings() {
        User otherUser = new User(2L, "Test", "test@yandex.ru");
        Booking lastBooking = new Booking(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(2));
        lastBooking.setId(1L);
        Booking nextBooking = new Booking(LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(4));
        nextBooking.setId(2L);
        lastBooking.setStatus(BookingStatus.APPROVED);
        lastBooking.setItem(item);
        lastBooking.setBooker(otherUser);
        nextBooking.setStatus(BookingStatus.APPROVED);
        nextBooking.setItem(item);
        nextBooking.setBooker(otherUser);
        BookingWithBookerIdDto lastBookingWithBooker = new BookingWithBookerIdDto(1L, 2L);
        BookingWithBookerIdDto nextBookingWithBooker = new BookingWithBookerIdDto(1L, 2L);
        ItemCommentAndBookingDto itemCommentDto =
                new ItemCommentAndBookingDto(1L, "Notebook", "testDesc", 0, true);
        item.setOwner(owner);

        when(mockItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(mockItemMapper.toItemWithCommentDto(item)).thenReturn(itemCommentDto);
        when(mockBookingRepository.findByItemIdAndStatusIsAndStartBeforeOrderByStartDesc(
                eq(1L),
                eq(BookingStatus.APPROVED),
                any())).thenReturn(List.of(lastBooking));
        when(mockBookingRepository.findByItemIdAndStatusIsAndStartAfterOrderByStartAsc(
                eq(1L),
                eq(BookingStatus.APPROVED),
                any())).thenReturn(List.of(nextBooking));
        when(mockBookingMapper.toBookingWithBookerIdDto(lastBooking)).thenReturn(lastBookingWithBooker);
        when(mockBookingMapper.toBookingWithBookerIdDto(nextBooking)).thenReturn(nextBookingWithBooker);
        when(mockCommentRepository.findByItemId(itemDto.getId())).thenReturn(Collections.emptyList());
        when(mockCommentMapper.listCommentDto(any())).thenReturn(Collections.emptyList());

        ItemCommentAndBookingDto itemById = service.getItemById(owner.getId(), item.getId());

        assertEquals(itemById, itemCommentDto);
    }

    @Test
    public void shouldReturnItemByIdWithComments() {
        CommentDto commentDto = new CommentDto(1L, "text", "Test", Instant.now());
        Comment comment = new Comment(1L, "text", commentDto.getCreated());
        ItemCommentAndBookingDto itemCommentDto =
                new ItemCommentAndBookingDto(1L, "Notebook", "testDesc", 0, true);
        item.setOwner(owner);

        when(mockItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(mockItemMapper.toItemWithCommentDto(item)).thenReturn(itemCommentDto);
        when(mockBookingRepository.findByItemIdAndStatusIsAndStartBeforeOrderByStartDesc(
                eq(1L),
                eq(BookingStatus.APPROVED),
                any())).thenReturn(Collections.emptyList());
        when(mockBookingRepository.findByItemIdAndStatusIsAndStartAfterOrderByStartAsc(
                eq(1L),
                eq(BookingStatus.APPROVED),
                any())).thenReturn(Collections.emptyList());
        when(mockCommentRepository.findByItemId(itemDto.getId())).thenReturn(List.of(comment));
        when(mockCommentMapper.listCommentDto(any())).thenReturn(List.of(commentDto));

        ItemCommentAndBookingDto itemById = service.getItemById(owner.getId(), item.getId());

        assertEquals(itemById, itemCommentDto);
    }

    @Test
    public void shouldReturnItemForSearch() {
        when(mockItemRepository
                .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndAvailableTrue(
                        "ote", "ote", PageRequest.of(0, 20)))
                .thenReturn(List.of(item));
        when(mockItemMapper.listItemDto(any())).thenReturn(List.of(itemDto));

        List<ItemDto> items = service.search("ote", 0, 20);

        assertEquals(items, List.of(itemDto));
    }

    @Test
    public void shouldReturnEmptyListForSearchWithEmptyText() {
        List<ItemDto> items = service.search(" ", 0, 20);

        assertEquals(items, Collections.emptyList());
    }

    @Test
    public void shouldCreateComment() {
        Booking booking = new Booking(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(2));
        booking.setId(1L);
        CommentDto commentDto = new CommentDto(1L, "text", "Test", Instant.now());
        Comment comment = new Comment(1L, "text", commentDto.getCreated());

        when(mockBookingRepository.findAllByItemIdAndBookerIdAndStatusIsAndEndBefore(
                eq(item.getId()),
                eq(2L),
                eq(BookingStatus.APPROVED),
                any())).thenReturn(List.of(booking));
        when(mockCommentMapper.toComment(commentDto)).thenReturn(comment);
        when(mockCommentRepository.save(comment)).thenReturn(comment);
        when(mockCommentMapper.toCommentDto(comment)).thenReturn(commentDto);

        CommentDto createdComment = service.createComment(2L, 1L, commentDto);

        assertEquals(createdComment, commentDto);
    }

    @Test
    public void shouldReturnValidExceptionForCreateCommentWithoutBooking() {
        CommentDto commentDto = new CommentDto(1L, "text", "Test", Instant.now());

        when(mockBookingRepository.findAllByItemIdAndBookerIdAndStatusIsAndEndBefore(
                eq(item.getId()),
                eq(2L),
                eq(BookingStatus.APPROVED),
                any())).thenReturn(Collections.emptyList());

        assertThrows(ValidationException.class,
                () -> service.createComment(2L, 1L, commentDto));
    }

    @Test
    public void shouldReturnCommentsByItem() {
        CommentDto commentDto = new CommentDto(1L, "text", "Test", Instant.now());
        Comment comment = new Comment(1L, "text", commentDto.getCreated());
        comment.setItem(item);

        when(mockCommentRepository.findByItemId(1L)).thenReturn(List.of(comment));
        when(mockCommentMapper.listCommentDto(List.of(comment))).thenReturn(List.of(commentDto));

        List<CommentDto> comments = service.getCommentsByItem(1L);

        assertEquals(comments, List.of(commentDto));
    }
}