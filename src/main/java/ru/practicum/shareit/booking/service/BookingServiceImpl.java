package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingOutputDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.dto.UserMapper;
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
        User booker = userValidation(userId);

        if (bookingInputDto.getStart().isAfter(bookingInputDto.getEnd())
                || bookingInputDto.getStart().isEqual(bookingInputDto.getEnd())) {
            log.error("Некорректная дата у заказа = {}", bookingInputDto);
            throw new ValidationException("Некорректная дата");
        }

        Booking booking = bookingMapper.toBooking(bookingInputDto);
        booking.setBooker(booker);
        Optional<Item> item = itemRepository.findById(bookingInputDto.getItemId());

        if (item.isEmpty()) {
            log.error(String.valueOf(bookingInputDto.getItemId()));
            throw new NotFoundException("Не найден предмет с id = " + bookingInputDto.getItemId());
        }

        if (!item.get().getAvailable()) {
            log.error(String.valueOf(item.get()));
            throw new ValidationException("Этот предмет уже арендован");
        }

        if (item.get().getOwner().getId().equals(userId)) {
            log.error("Пользователь c id = {}, владелец предмета с id = {}", userId, item.get().getId());
            throw new NotFoundException("Не найден пользователь с id = " + userId);
        }

        booking.setItem(item.get());
        booking.setStatus(BookingStatus.WAITING);
        BookingOutputDto bookingOutputDto = bookingMapper.toBookingOutputDto(bookingRepository.saveAndFlush(booking));
        bookingOutputDto.setBooker(userMapper.toUserDto(booker));
        bookingOutputDto.setItem(itemMapper.toItemDto(item.get()));
        return bookingOutputDto;
    }

    @Override
    @Transactional
    public BookingOutputDto confirmBooking(Long ownerId, Long bookingId, Boolean approved) {
        Optional<Booking> bookingOptional =
                bookingRepository.findById(bookingId);
        userValidation(ownerId);

        if (bookingOptional.isEmpty()) {
            log.error("Заказ с id = {} ownerId = {} не найден", bookingId, ownerId);
            throw new NotFoundException("Не найден заказ с id = " + bookingId);
        }

        Booking booking = bookingOptional.get();

        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            log.error("Пользователь с id = {} не является владельцем предмета", ownerId);
            throw new NotFoundException("Не найден пользователь с id = " + ownerId);
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

        BookingOutputDto bookingOutputDto = bookingMapper.toBookingOutputDto(bookingRepository.saveAndFlush(booking));
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
            throw new NotFoundException("Не найден заказ с id = " + bookingId);
        }

        User user = userValidation(userId);
        User booker = booking.get().getBooker();
        User owner = booking.get().getItem().getOwner();

        if (!user.getId().equals(booker.getId())
                && !user.getId().equals(owner.getId())) {
            log.error("Пользователь с id = {} не владелец и не заказчик", userId);
            throw new NotFoundException("Не найден пользователь с id = " + userId);
        }

        BookingOutputDto bookingOutputDto = bookingMapper.toBookingOutputDto(booking.get());
        bookingOutputDto.setBooker(userMapper.toUserDto(booking.get().getBooker()));
        bookingOutputDto.setItem(itemMapper.toItemDto(booking.get().getItem()));
        return bookingOutputDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingOutputDto> getBookingsByUser(Long userId, String stateString, Integer from, Integer size) {
        try {
            BookingState state = BookingState.valueOf(stateString);
            userValidation(userId);

            List<Booking> bookings;
            Pageable pageable = PageRequest.of(from / size, size, Sort.by("start").descending());

            switch (state) {
                case PAST:
                    bookings = bookingRepository.findAllByBookerIdAndEndBefore(
                            userId,
                            LocalDateTime.now(),
                            pageable);
                    break;
                case FUTURE:
                    bookings = bookingRepository.findAllByBookerIdAndStartAfter(
                            userId,
                            LocalDateTime.now(),
                            pageable);
                    break;
                case WAITING:
                    bookings = bookingRepository.findAllByBookerIdAndStatusIs(
                            userId,
                            BookingStatus.WAITING,
                            pageable);
                    break;
                case REJECTED:
                    bookings = bookingRepository.findAllByBookerIdAndStatusIs(
                            userId,
                            BookingStatus.REJECTED,
                            pageable);
                    break;
                case CURRENT:
                    bookings = bookingRepository.findAllByBookerIdCurrentBooking(
                            userId,
                            LocalDateTime.now(),
                            pageable);
                    break;
                default:
                    bookings = bookingRepository.findAllByBookerId(userId, pageable);
                    break;
            }

            List<BookingOutputDto> bookingsOutputDto = new ArrayList<>();

            if (bookings.isEmpty()) {
                log.error("Заказы с userId = {} и state = {} не найдены", userId, state);
                throw new NotFoundException("Заказы не найдены");
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
    public List<BookingOutputDto> getBookingsByOwner(Long ownerId, String stateString, Integer from, Integer size) {
        try {
            BookingState state = BookingState.valueOf(stateString);
            userValidation(ownerId);

            List<Booking> bookings;
            Pageable pageable = PageRequest.of(from / size, size, Sort.by("start").descending());

            switch (state) {
                case PAST:
                    bookings = bookingRepository.findAllByItemOwnerIdAndEndBefore(
                            ownerId,
                            LocalDateTime.now(),
                            pageable);
                    break;
                case FUTURE:
                    bookings = bookingRepository.findAllByItemOwnerIdAndStartAfter(
                            ownerId,
                            LocalDateTime.now(),
                            pageable);
                    break;
                case WAITING:
                    bookings = bookingRepository.findAllByItemOwnerIdAndStatusIs(
                            ownerId,
                            BookingStatus.WAITING,
                            pageable);
                    break;
                case REJECTED:
                    bookings = bookingRepository.findAllByItemOwnerIdAndStatusIs(
                            ownerId,
                            BookingStatus.REJECTED,
                            pageable);
                    break;
                case CURRENT:
                    bookings = bookingRepository.findAllByItemOwnerIdCurrentBooking(
                            ownerId,
                            LocalDateTime.now(),
                            pageable);
                    break;
                default:
                    bookings = bookingRepository.findAllByItemOwnerId(ownerId, pageable);
                    break;
            }

            List<BookingOutputDto> bookingsOutputDto = new ArrayList<>();

            if (bookings.isEmpty()) {
                log.error("Заказы с userId = {} и state = {} не найдены", ownerId, state);
                throw new NotFoundException("Заказы не найдены");
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

    private User userValidation(Long userId) {
        Optional<User> user = userRepository.findById(userId);

        if (user.isEmpty()) {
            log.error("Пользователь не найден id = {}", userId);
            throw new NotFoundException("Не найден пользователь с id = " + userId);
        }

        return user.get();
    }
}
