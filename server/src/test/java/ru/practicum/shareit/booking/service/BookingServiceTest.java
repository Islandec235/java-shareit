package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingOutputDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import javax.validation.ValidationException;
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
public class BookingServiceTest {
    @Mock
    ItemRepository mockItemRepository;
    @Mock
    UserRepository mockUserRepository;
    @Mock
    BookingRepository mockBookingRepository;
    @Mock
    UserMapper mockUserMapper;
    @Mock
    BookingMapper mockBookingMapper;
    @Mock
    ItemMapper mockItemMapper;
    @InjectMocks
    BookingServiceImpl service;

    private User owner;
    private User user;
    private Item item;
    private UserDto userDto;
    private ItemDto itemDto;
    private Booking booking;

    @BeforeEach
    void setUp() {
        owner = new User(1L, "Test owner", "owner@mail.ru");
        user = new User(2L, "Test user", "test@mail.ru");
        item = new Item(1L, "Test item", "test desc", 0, true);
        item.setOwner(owner);
        userDto = new UserDto(1L, "Test userDto", "test@mail.ru");
        itemDto = new ItemDto(1L, "Test itemDto", "test desc", 0, true);
        booking = new Booking(
                LocalDateTime.of(2023, 7, 1, 5, 17, 42),
                LocalDateTime.of(2024, 7, 1, 5, 17, 42));
        booking.setId(1L);
    }

    @Test
    public void shouldCreateBooking() {
        BookingInputDto inputBooking = new BookingInputDto(
                1L,
                LocalDateTime.of(2023, 7, 1, 5, 17, 42),
                LocalDateTime.of(2024, 7, 1, 5, 17, 42));
        BookingOutputDto outputBooking = new BookingOutputDto(
                1L,
                LocalDateTime.of(2023, 7, 1, 5, 17, 42),
                LocalDateTime.of(2024, 7, 1, 5, 17, 42),
                BookingStatus.WAITING);

        when(mockUserRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(mockBookingMapper.toBooking(inputBooking)).thenReturn(booking);
        when(mockItemRepository.findById(any())).thenReturn(Optional.of(item));
        when(mockBookingRepository.saveAndFlush(any())).thenReturn(booking);
        when(mockBookingMapper.toBookingOutputDto(any())).thenReturn(outputBooking);
        when(mockUserMapper.toUserDto(any())).thenReturn(userDto);
        when(mockItemMapper.toItemDto(any())).thenReturn(itemDto);

        BookingOutputDto createdBooking = service.create(inputBooking, user.getId());
        outputBooking.setBooker(userDto);
        outputBooking.setItem(itemDto);

        assertEquals(createdBooking, outputBooking);
    }

    @Test
    public void shouldReturnNotFoundExceptionForCreateBookingWithoutUser() {
        BookingInputDto inputBooking = new BookingInputDto(
                1L,
                LocalDateTime.of(2025, 7, 1, 5, 17, 42),
                LocalDateTime.of(2026, 7, 1, 5, 17, 42));

        when(mockUserRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.create(inputBooking, user.getId()));
    }

    @Test
    public void shouldReturnNotFoundExceptionForCreateBookingWithoutItem() {
        BookingInputDto inputBooking = new BookingInputDto(
                1L,
                LocalDateTime.of(2025, 7, 1, 5, 17, 42),
                LocalDateTime.of(2026, 7, 1, 5, 17, 42));

        when(mockUserRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(mockBookingMapper.toBooking(inputBooking)).thenReturn(booking);
        when(mockItemRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.create(inputBooking, user.getId()));
    }

    @Test
    public void shouldReturnValidationExceptionForCreateBookingWithItemNotAvailable() {
        item.setAvailable(false);
        BookingInputDto inputBooking = new BookingInputDto(
                1L,

                LocalDateTime.of(2025, 7, 1, 5, 17, 42),
                LocalDateTime.of(2026, 7, 1, 5, 17, 42));

        when(mockUserRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(mockBookingMapper.toBooking(inputBooking)).thenReturn(booking);
        when(mockItemRepository.findById(any())).thenReturn(Optional.of(item));

        assertThrows(ValidationException.class,
                () -> service.create(inputBooking, user.getId()));
    }

    @Test
    public void shouldReturnNotFoundExceptionForCreateBookingWithUserEqualsOwner() {
        item.setOwner(user);
        BookingInputDto inputBooking = new BookingInputDto(
                1L,
                LocalDateTime.of(2025, 7, 1, 5, 17, 42),
                LocalDateTime.of(2026, 7, 1, 5, 17, 42));

        when(mockUserRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(mockBookingMapper.toBooking(inputBooking)).thenReturn(booking);
        when(mockItemRepository.findById(any())).thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class,
                () -> service.create(inputBooking, user.getId()));
    }

    @Test
    public void shouldConfirmBookingWithApproved() {
        BookingOutputDto outputBooking = new BookingOutputDto(
                1L, booking.getStart(), booking.getEnd(), BookingStatus.APPROVED);
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);

        when(mockBookingRepository.findById(any())).thenReturn(Optional.of(booking));
        when(mockUserRepository.findById(any())).thenReturn(Optional.of(owner));
        when(mockBookingMapper.toBookingOutputDto(any())).thenReturn(outputBooking);
        when(mockUserMapper.toUserDto(any())).thenReturn(userDto);
        when(mockItemMapper.toItemDto(any())).thenReturn(itemDto);


        BookingOutputDto approvedBooking = service.confirmBooking(1L, 1L, true);
        outputBooking.setBooker(userDto);
        outputBooking.setItem(itemDto);

        assertEquals(approvedBooking, outputBooking);
    }

    @Test
    public void shouldConfirmBookingWithRejected() {
        BookingOutputDto outputBooking = new BookingOutputDto(
                1L, booking.getStart(), booking.getEnd(), BookingStatus.REJECTED);
        outputBooking.setBooker(userDto);
        outputBooking.setItem(itemDto);
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);

        when(mockBookingRepository.findById(any())).thenReturn(Optional.of(booking));
        when(mockUserRepository.findById(any())).thenReturn(Optional.of(owner));
        when(mockBookingMapper.toBookingOutputDto(any())).thenReturn(outputBooking);
        when(mockUserMapper.toUserDto(any())).thenReturn(userDto);
        when(mockItemMapper.toItemDto(any())).thenReturn(itemDto);

        BookingOutputDto approvedBooking = service.confirmBooking(1L, 1L, false);

        assertEquals(approvedBooking, outputBooking);
    }

    @Test
    public void shouldReturnNotFoundForConfirmBookingWithoutBooking() {
        when(mockUserRepository.findById(any())).thenReturn(Optional.of(owner));
        when(mockBookingRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.confirmBooking(1L, 1L, true));
    }

    @Test
    public void shouldReturnNotFoundForConfirmBookingWhereUserNotOwner() {
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);

        when(mockUserRepository.findById(any())).thenReturn(Optional.of(user));
        when(mockBookingRepository.findById(any())).thenReturn(Optional.of(booking));

        assertThrows(NotFoundException.class,
                () -> service.confirmBooking(2L, 1L, true));
    }

    @Test
    public void shouldReturnValidationExceptionForConfirmBookingWithStatusNotWaited() {
        booking.setItem(item);
        booking.setStatus(BookingStatus.APPROVED);

        when(mockBookingRepository.findById(any())).thenReturn(Optional.of(booking));
        when(mockUserRepository.findById(any())).thenReturn(Optional.of(owner));

        assertThrows(ValidationException.class,
                () -> service.confirmBooking(1L, 1L, true));
    }

    @Test
    public void shouldReturnBookingByIdForBooker() {
        booking.setItem(item);
        booking.setBooker(user);
        BookingOutputDto outputBooking = new BookingOutputDto(
                1L, booking.getStart(), booking.getEnd(), BookingStatus.WAITING);
        outputBooking.setBooker(userDto);
        outputBooking.setItem(itemDto);

        when(mockBookingRepository.findById(any())).thenReturn(Optional.of(booking));
        when(mockUserRepository.findById(any())).thenReturn(Optional.of(user));
        when(mockBookingMapper.toBookingOutputDto(any())).thenReturn(outputBooking);
        when(mockUserMapper.toUserDto(any())).thenReturn(userDto);
        when(mockItemMapper.toItemDto(any())).thenReturn(itemDto);

        BookingOutputDto foundBooking = service.getBookingById(user.getId(), booking.getId());

        assertEquals(foundBooking, outputBooking);
    }

    @Test
    public void shouldReturnBookingByIdForOwner() {
        booking.setItem(item);
        booking.setBooker(user);
        BookingOutputDto outputBooking = new BookingOutputDto(
                1L, booking.getStart(), booking.getEnd(), BookingStatus.WAITING);
        outputBooking.setBooker(userDto);
        outputBooking.setItem(itemDto);

        when(mockBookingRepository.findById(any())).thenReturn(Optional.of(booking));
        when(mockUserRepository.findById(any())).thenReturn(Optional.of(owner));
        when(mockBookingMapper.toBookingOutputDto(any())).thenReturn(outputBooking);
        when(mockUserMapper.toUserDto(any())).thenReturn(userDto);
        when(mockItemMapper.toItemDto(any())).thenReturn(itemDto);

        BookingOutputDto foundBooking = service.getBookingById(owner.getId(), booking.getId());

        assertEquals(foundBooking, outputBooking);
    }

    @Test
    public void shouldReturnNotFoundForGetByIdWithoutBooking() {
        when(mockBookingRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.getBookingById(user.getId(), booking.getId()));
    }

    @Test
    public void shouldReturnBookingByIdForUserNotBookerAndNotOwner() {
        User otherUser = new User(3L, "Test other user", "other@mail.ru");
        booking.setItem(item);
        booking.setBooker(user);

        when(mockBookingRepository.findById(any())).thenReturn(Optional.of(booking));
        when(mockUserRepository.findById(any())).thenReturn(Optional.of(otherUser));

        assertThrows(NotFoundException.class,
                () -> service.getBookingById(otherUser.getId(), booking.getId()));
    }

    @Test
    public void shouldGetBookingsByUserWithStateALL() {
        BookingOutputDto outputBooking = new BookingOutputDto(
                1L, booking.getStart(), booking.getEnd(), BookingStatus.WAITING);
        outputBooking.setBooker(userDto);
        outputBooking.setItem(itemDto);

        when(mockUserRepository.findById(any())).thenReturn(Optional.of(user));
        when(mockBookingRepository.findAllByBookerId(any(), any())).thenReturn(List.of(booking));
        when(mockBookingMapper.toBookingOutputDto(any())).thenReturn(outputBooking);
        when(mockUserMapper.toUserDto(any())).thenReturn(userDto);
        when(mockItemMapper.toItemDto(any())).thenReturn(itemDto);

        List<BookingOutputDto> bookings = service.getBookingsByUser(user.getId(), BookingState.ALL, 0, 20);

        assertEquals(List.of(outputBooking), bookings);
    }

    @Test
    public void shouldGetBookingsByUserWithStatePAST() {
        BookingOutputDto outputBooking = new BookingOutputDto(
                1L, booking.getStart(), booking.getEnd(), BookingStatus.WAITING);
        outputBooking.setBooker(userDto);
        outputBooking.setItem(itemDto);

        when(mockUserRepository.findById(any())).thenReturn(Optional.of(user));
        when(mockBookingRepository.findAllByBookerIdAndEndBefore(any(), any(), any())).thenReturn(List.of(booking));
        when(mockBookingMapper.toBookingOutputDto(any())).thenReturn(outputBooking);
        when(mockUserMapper.toUserDto(any())).thenReturn(userDto);
        when(mockItemMapper.toItemDto(any())).thenReturn(itemDto);

        List<BookingOutputDto> bookings = service.getBookingsByUser(user.getId(), BookingState.PAST, 0, 20);

        assertEquals(List.of(outputBooking), bookings);
    }

    @Test
    public void shouldGetBookingsByUserWithStateFUTURE() {
        booking.setStart(LocalDateTime.of(2100, 7, 1, 5, 17, 42));
        booking.setEnd(LocalDateTime.of(2101, 7, 1, 5, 17, 42));
        BookingOutputDto outputBooking = new BookingOutputDto(
                1L, booking.getStart(), booking.getEnd(), BookingStatus.WAITING);
        outputBooking.setBooker(userDto);
        outputBooking.setItem(itemDto);

        when(mockUserRepository.findById(any())).thenReturn(Optional.of(user));
        when(mockBookingRepository.findAllByBookerIdAndStartAfter(any(), any(), any())).thenReturn(List.of(booking));
        when(mockBookingMapper.toBookingOutputDto(any())).thenReturn(outputBooking);
        when(mockUserMapper.toUserDto(any())).thenReturn(userDto);
        when(mockItemMapper.toItemDto(any())).thenReturn(itemDto);

        List<BookingOutputDto> bookings = service.getBookingsByUser(user.getId(), BookingState.FUTURE, 0, 20);

        assertEquals(List.of(outputBooking), bookings);
    }

    @Test
    public void shouldGetBookingsByUserWithStateWAITING() {
        BookingOutputDto outputBooking = new BookingOutputDto(
                1L, booking.getStart(), booking.getEnd(), BookingStatus.WAITING);
        outputBooking.setBooker(userDto);
        outputBooking.setItem(itemDto);

        when(mockUserRepository.findById(any())).thenReturn(Optional.of(user));
        when(mockBookingRepository.findAllByBookerIdAndStatusIs(any(), any(), any())).thenReturn(List.of(booking));
        when(mockBookingMapper.toBookingOutputDto(any())).thenReturn(outputBooking);
        when(mockUserMapper.toUserDto(any())).thenReturn(userDto);
        when(mockItemMapper.toItemDto(any())).thenReturn(itemDto);

        List<BookingOutputDto> bookings =
                service.getBookingsByUser(user.getId(), BookingState.WAITING, 0, 20);

        assertEquals(List.of(outputBooking), bookings);
    }

    @Test
    public void shouldGetBookingsByUserWithStateREJECTED() {
        BookingOutputDto outputBooking = new BookingOutputDto(
                1L, booking.getStart(), booking.getEnd(), BookingStatus.REJECTED);
        outputBooking.setBooker(userDto);
        outputBooking.setItem(itemDto);

        when(mockUserRepository.findById(any())).thenReturn(Optional.of(user));
        when(mockBookingRepository.findAllByBookerIdAndStatusIs(any(), any(), any())).thenReturn(List.of(booking));
        when(mockBookingMapper.toBookingOutputDto(any())).thenReturn(outputBooking);
        when(mockUserMapper.toUserDto(any())).thenReturn(userDto);
        when(mockItemMapper.toItemDto(any())).thenReturn(itemDto);

        List<BookingOutputDto> bookings =
                service.getBookingsByUser(user.getId(), BookingState.REJECTED, 0, 20);

        assertEquals(List.of(outputBooking), bookings);
    }

    @Test
    public void shouldGetBookingsByUserWithStateCURRENT() {
        booking.setStart(LocalDateTime.of(2000, 7, 1, 5, 17, 42));
        booking.setEnd(LocalDateTime.of(2100, 7, 1, 5, 17, 42));
        BookingOutputDto outputBooking = new BookingOutputDto(
                1L, booking.getStart(), booking.getEnd(), BookingStatus.WAITING);
        outputBooking.setBooker(userDto);
        outputBooking.setItem(itemDto);

        when(mockUserRepository.findById(any())).thenReturn(Optional.of(user));
        when(mockBookingRepository.findAllByBookerIdCurrentBooking(any(), any(), any())).thenReturn(List.of(booking));
        when(mockBookingMapper.toBookingOutputDto(any())).thenReturn(outputBooking);
        when(mockUserMapper.toUserDto(any())).thenReturn(userDto);
        when(mockItemMapper.toItemDto(any())).thenReturn(itemDto);

        List<BookingOutputDto> bookings =
                service.getBookingsByUser(user.getId(), BookingState.CURRENT, 0, 20);

        assertEquals(List.of(outputBooking), bookings);
    }

    @Test
    public void shouldReturnExceptionForGetBookingByUserWithoutBookings() {
        when(mockUserRepository.findById(any())).thenReturn(Optional.of(user));
        when(mockBookingRepository.findAllByBookerId(any(), any())).thenReturn(Collections.emptyList());

        assertThrows(NotFoundException.class,
                () -> service.getBookingsByUser(user.getId(), BookingState.ALL, 0, 20));
    }
}
