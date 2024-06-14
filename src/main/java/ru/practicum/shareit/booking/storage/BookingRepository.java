package ru.practicum.shareit.booking.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Booking save(Booking booking);

    Optional<Booking> findById(Long id);

    List<Booking> findAllByItemIdAndBookerIdAndStatusIsAndEndBefore(
            Long itemId,
            Long bookerId,
            BookingStatus status,
            LocalDateTime time);

    List<Booking> findByItemIdAndStatusIsAndStartBeforeOrderByStartDesc(Long itemId,
                                                                        BookingStatus status,
                                                                        LocalDateTime time);

    List<Booking> findByItemIdAndStatusIsAndStartAfterOrderByStartAsc(Long itemId,
                                                                      BookingStatus status,
                                                                      LocalDateTime time);

    List<Booking> findAllByBookerIdAndStatusIsOrderByStartDesc(Long userId, BookingStatus status);

    @Query("SELECT b " +
            "FROM Booking b " +
            "WHERE b.booker.id = ?1 " +
            "AND b.start < ?2 " +
            "AND b.end > ?2 " +
            "ORDER BY b.start DESC")
    List<Booking> findAllByBookerIdCurrentBooking(Long userId, LocalDateTime time);

    List<Booking> findAllByBookerIdAndEndBeforeOrderByStartDesc(Long userId, LocalDateTime time);

    List<Booking> findAllByBookerIdAndStartAfterOrderByStartDesc(Long userId, LocalDateTime time);

    List<Booking> findAllByBookerIdOrderByStartDesc(Long userId);

    List<Booking> findAllByItemOwnerIdOrderByStartDesc(Long userId);

    List<Booking> findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(Long userId, LocalDateTime time);

    List<Booking> findAllByItemOwnerIdAndStartAfterOrderByStartDesc(Long userId, LocalDateTime time);

    List<Booking> findAllByItemOwnerIdAndStatusIsOrderByStartDesc(Long userId, BookingStatus status);

    @Query("SELECT b " +
            "FROM Booking b " +
            "WHERE b.item.owner.id = ?1 " +
            "AND b.start < ?2 " +
            "AND b.end > ?2 " +
            "ORDER BY b.start DESC")
    List<Booking> findAllByItemOwnerIdCurrentBooking(Long userId, LocalDateTime time);
}
