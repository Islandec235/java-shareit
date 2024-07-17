package ru.practicum.shareit.booking.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:test"})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingRepositoryTest {
    private final BookingRepository repository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    private User testOwner;
    private User testBooker;
    private Item testItem;
    private Booking booking;

    @BeforeEach
    void setUp() {
        testOwner = new User(null, "owner", "owner@email.ru");
        testBooker = new User(null, "booker", "booker@email.ru");
        testOwner = userRepository.save(testOwner);
        testBooker = userRepository.save(testBooker);
        testItem = new Item(null, "item", "desc", 0, true);
        testItem.setOwner(testOwner);
        testItem = itemRepository.save(testItem);
        booking = new Booking(
                LocalDateTime.of(2024, 7, 4, 4, 4),
                LocalDateTime.of(2024, 8, 4, 4, 4));
        booking.setStatus(BookingStatus.WAITING);
        booking.setBooker(testBooker);
        booking.setItem(testItem);
        booking = repository.save(booking);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteById(testBooker.getId());
        userRepository.deleteById(testOwner.getId());
        itemRepository.deleteById(testItem.getId());
        repository.deleteById(booking.getId());
    }

    @Test
    public void shouldFindAllByBookerIdCurrentBooking() {
        List<Booking> bookings = repository.findAllByBookerIdCurrentBooking(
                testBooker.getId(),
                LocalDateTime.now(),
                PageRequest.of(0, 2));

        assertFalse(bookings.isEmpty());
        assertEquals(bookings.get(0), booking);
    }

    @Test
    public void shouldFindAllByItemOwnerIdCurrentBooking() {
        List<Booking> bookings = repository.findAllByItemOwnerIdCurrentBooking(
                testOwner.getId(),
                LocalDateTime.now(),
                PageRequest.of(0, 2));

        assertFalse(bookings.isEmpty());
        assertEquals(bookings.get(0), booking);
    }
}
