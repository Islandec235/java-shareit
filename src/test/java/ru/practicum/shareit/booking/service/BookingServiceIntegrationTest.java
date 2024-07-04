package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingOutputDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingServiceIntegrationTest {
    private final BookingService service;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;

    private User owner;
    private User booker;
    private Item item;
    private Booking booking;

    @BeforeEach
    void setUp() {
        owner = new User(1L, "Test owner", "test@mail.ru");
        booker = new User(2L, "Test booker", "booker@mail.ru");
        owner = userRepository.save(owner);
        booker = userRepository.save(booker);
        item = new Item(1L, "test item", "desc", 0, true);
        item.setOwner(owner);
        item = itemRepository.save(item);
        booking = new Booking(
                1L,
                LocalDateTime.of(2024, 4, 1, 18, 18),
                LocalDateTime.of(2024, 4, 2, 18, 18));
        booking.setStatus(BookingStatus.WAITING);
        booking.setItem(item);
        booking.setBooker(booker);
        booking = bookingRepository.save(booking);
    }

    @Test
    public void shouldReturnBookingsByOwner() {
        List<BookingOutputDto> bookings = service.getBookingsByOwner(owner.getId(), "PAST", 0, 1);

        assertThat(bookings).isNotNull();
        assertThat(bookings.isEmpty()).isFalse();
        assertThat(bookings.size()).isEqualTo(1);
        assertThat(bookings.get(0).getId()).isEqualTo(1L);
        assertThat(bookings.get(0).getBooker().getId()).isEqualTo(booker.getId());
        assertThat(bookings.get(0).getBooker().getName()).isEqualTo("Test booker");
        assertThat(bookings.get(0).getBooker().getEmail()).isEqualTo("booker@mail.ru");
        assertThat(bookings.get(0).getItem().getId()).isEqualTo(item.getId());
        assertThat(bookings.get(0).getItem().getName()).isEqualTo("test item");
        assertThat(bookings.get(0).getItem().getDescription()).isEqualTo("desc");
        assertThat(bookings.get(0).getItem().getRentals()).isEqualTo(0);
        assertThat(bookings.get(0).getItem().getAvailable()).isTrue();
        assertThat(bookings.get(0).getStart())
                .isEqualTo(LocalDateTime.of(2024, 4, 1, 18, 18));
        assertThat(bookings.get(0).getEnd())
                .isEqualTo(LocalDateTime.of(2024, 4, 2, 18, 18));
        assertThat(bookings.get(0).getStatus()).isEqualTo(BookingStatus.WAITING);
    }
}
