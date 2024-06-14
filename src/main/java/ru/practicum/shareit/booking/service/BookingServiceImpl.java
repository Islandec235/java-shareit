package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingOutputDto;
import ru.practicum.shareit.booking.exceptions.BookingNotFoundException;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.exceptions.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingMapper bookingMapper;
    private final ItemMapper itemMapper;
    private final UserMapper userMapper;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingOutputDto create(BookingInputDto bookingInputDto, Long userId) {
        Optional<User> booker = userRepository.findById(userId);

        if (bookingInputDto.getStart().isAfter(bookingInputDto.getEnd())
                || bookingInputDto.getStart().isEqual(bookingInputDto.getEnd())) {
            log.error("Некорректная дата у заказа = {}", bookingInputDto);
            throw new ValidationException("Некорректная дата");
        }

        if (booker.isEmpty()) {
            log.error("Пользователь с id = {} не найден", userId);
            throw new UserNotFoundException("Владелец не найден");
        }

        Booking booking = bookingMapper.toBooking(bookingInputDto);
        booking.setBooker(booker.get());
        Optional<Item> item = itemRepository.findById(bookingInputDto.getItemId());

        if (item.isEmpty()) {
            log.error(String.valueOf(bookingInputDto.getItemId()));
            throw new ItemNotFoundException("Предмет не найден");
        }

        if (!item.get().getAvailable()) {
            log.error(String.valueOf(item.get()));
            throw new ValidationException("Этот предмет уже арендован");
        }

        if (item.get().getOwner().getId().equals(userId)) {
            log.error("Пользователь c id = {}, владелец предмета с id = {}", userId, item.get().getId());
            throw new UserNotFoundException("Пользователь не найден");
        }

        booking.setItem(item.get());
        booking.setStatus(BookingStatus.WAITING);
        BookingOutputDto bookingOutputDto = bookingMapper.toBookingOutputDto(bookingRepository.save(booking));
        bookingOutputDto.setBooker(userMapper.toUserDto(booker.get()));
        bookingOutputDto.setItem(itemMapper.toItemDto(item.get()));
        return bookingOutputDto;
    }

    @Override
    @Transactional
    public BookingOutputDto confirmBooking(Long ownerId, Long bookingId, Boolean approved) {
        Optional<Booking> bookingOptional =
                bookingRepository.findById(bookingId);
        Optional<User> userInStorage = userRepository.findById(ownerId);

        if (userInStorage.isEmpty()) {
            log.error("Пользователь не найден id = {}", ownerId);
            throw new UserNotFoundException("Пользователь не найден");
        }

        if (bookingOptional.isEmpty()) {
            log.error("Заказ с id = {} ownerId = {} не найден", bookingId, ownerId);
            throw new BookingNotFoundException("Заказ не найден");
        }

        Booking booking = bookingOptional.get();

        if (!booking.getItem().getOwner().getId().equals(userInStorage.get().getId())) {
            log.error("Пользователь с id = {} не является владельцем предмета", ownerId);
            throw new UserNotFoundException("Пользователь не найден");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            log.error("Статус не WAITING у заказа = {}", booking);
            throw new ValidationException("Бронирование не ожидает подтверждения");
        }

        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }

        BookingOutputDto bookingOutputDto = bookingMapper.toBookingOutputDto(booking);
        bookingOutputDto.setItem(itemMapper.toItemDto(booking.getItem()));
        bookingOutputDto.setBooker(userMapper.toUserDto(booking.getBooker()));
        return bookingOutputDto;
    }

    @Override
    @Transactional(readOnly = true)
    public BookingOutputDto getBookingById(Long userId, Long bookingId) {
        Optional<Booking> booking = bookingRepository.findById(bookingId);

        if (booking.isEmpty()) {
            log.error("Заказ с id = {} userId = {} не найден", bookingId, userId);
            throw new BookingNotFoundException("Заказ не найден");
        }

        Optional<User> user = userRepository.findById(userId);
        User booker = booking.get().getBooker();
        User owner = booking.get().getItem().getOwner();

        if (user.isEmpty() ||
                (!user.get().getId().equals(booker.getId())
                        && !user.get().getId().equals(owner.getId()))) {
            log.error("Пользователь с id = {} не владелец и не заказчик", userId);
            throw new UserNotFoundException("Пользователь не найден");
        }

        BookingOutputDto bookingOutputDto = bookingMapper.toBookingOutputDto(booking.get());
        bookingOutputDto.setBooker(userMapper.toUserDto(booking.get().getBooker()));
        bookingOutputDto.setItem(itemMapper.toItemDto(booking.get().getItem()));
        return bookingOutputDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingOutputDto> getBookingsByUser(Long userId, String stateString) {
        try {
            BookingState state = BookingState.valueOf(stateString);
            if (userRepository.findById(userId).isEmpty()) {
                log.error("Пользователь не найден id = {}", userId);
                throw new UserNotFoundException("Пользователь не найден");
            }

            List<Booking> bookings;

            switch (state) {
                case ALL:
                    bookings = bookingRepository.findAllByBookerIdOrderByStartDesc(userId);
                    break;
                case PAST:
                    bookings = bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(
                            userId,
                            LocalDateTime.now());
                    break;
                case FUTURE:
                    bookings = bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(
                            userId,
                            LocalDateTime.now());
                    break;
                case WAITING:
                    bookings = bookingRepository.findAllByBookerIdAndStatusIsOrderByStartDesc(
                            userId,
                            BookingStatus.WAITING);
                    break;
                case REJECTED:
                    bookings = bookingRepository.findAllByBookerIdAndStatusIsOrderByStartDesc(
                            userId,
                            BookingStatus.REJECTED);
                    break;
                case CURRENT:
                    bookings = bookingRepository.findAllByBookerIdCurrentBooking(userId, LocalDateTime.now());
                    break;
                default:
                    log.error("Некорректный параметр сортировки: {}", state);
                    throw new IllegalArgumentException("Unknown state: " + state);
            }

            List<BookingOutputDto> bookingsOutputDto = new ArrayList<>();

            if (bookings.isEmpty()) {
                log.error("Заказы с userId = {} и state = {} не найдены", userId, state);
                throw new BookingNotFoundException("Заказы не найдены");
            }

            for (Booking booking : bookings) {
                BookingOutputDto bookingOutputDto = bookingMapper.toBookingOutputDto(booking);
                bookingOutputDto.setBooker(userMapper.toUserDto(booking.getBooker()));
                bookingOutputDto.setItem(itemMapper.toItemDto(booking.getItem()));
                bookingsOutputDto.add(bookingOutputDto);
            }

            return bookingsOutputDto;
        } catch (IllegalArgumentException e) {
            log.error("Некорректное значение state = {}", stateString);
            throw new IllegalArgumentException("Unknown state: " + stateString);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingOutputDto> getBookingsByOwner(Long ownerId, String stateString) {
        try {
            BookingState state = BookingState.valueOf(stateString);
            if (userRepository.findById(ownerId).isEmpty()) {
                log.error("Пользователь не найден id = {}", ownerId);
                throw new UserNotFoundException("Пользователь не найден");
            }

            List<Booking> bookings;

            switch (state) {
                case ALL:
                    bookings = bookingRepository.findAllByItemOwnerIdOrderByStartDesc(ownerId);
                    break;
                case PAST:
                    bookings = bookingRepository.findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(
                            ownerId,
                            LocalDateTime.now());
                    break;
                case FUTURE:
                    bookings = bookingRepository.findAllByItemOwnerIdAndStartAfterOrderByStartDesc(
                            ownerId,
                            LocalDateTime.now());
                    break;
                case WAITING:
                    bookings = bookingRepository.findAllByItemOwnerIdAndStatusIsOrderByStartDesc(
                            ownerId,
                            BookingStatus.WAITING);
                    break;
                case REJECTED:
                    bookings = bookingRepository.findAllByItemOwnerIdAndStatusIsOrderByStartDesc(
                            ownerId,
                            BookingStatus.REJECTED);
                    break;
                case CURRENT:
                    bookings = bookingRepository.findAllByItemOwnerIdCurrentBooking(ownerId, LocalDateTime.now());
                    break;
                default:
                    log.error("Некорректный параметр сортировки: {}", state);
                    throw new IllegalArgumentException("Unknown state: " + state);
            }

            List<BookingOutputDto> bookingsOutputDto = new ArrayList<>();

            if (bookings.isEmpty()) {
                log.error("Заказы с userId = {} и state = {} не найдены", ownerId, state);
                throw new BookingNotFoundException("Заказы не найдены");
            }

            for (Booking booking : bookings) {
                BookingOutputDto bookingOutputDto = bookingMapper.toBookingOutputDto(booking);
                bookingOutputDto.setBooker(userMapper.toUserDto(booking.getBooker()));
                bookingOutputDto.setItem(itemMapper.toItemDto(booking.getItem()));
                bookingsOutputDto.add(bookingOutputDto);
            }

            return bookingsOutputDto;
        } catch (IllegalArgumentException e) {
            log.error("Некорректное значение state = {}", stateString);
            throw new IllegalArgumentException("Unknown state: " + stateString);
        }
    }
}
