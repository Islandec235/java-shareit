package ru.practicum.shareit.booking.storage;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
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

    List<Booking> findAllByBookerIdAndStatusIs(Long userId, BookingStatus status, Pageable pageable);

    @Query("SELECT b " +
            "FROM Booking b " +
            "WHERE b.booker.id = ?1 " +
            "AND b.start < ?2 " +
            "AND b.end > ?2" +
            "ORDER BY b.start DESC")
    List<Booking> findAllByBookerIdCurrentBooking(Long userId, LocalDateTime time, Pageable pageable);

    List<Booking> findAllByBookerIdAndEndBefore(Long userId, LocalDateTime time, Pageable pageable);

    List<Booking> findAllByBookerIdAndStartAfter(Long userId, LocalDateTime time, Pageable pageable);

    List<Booking> findAllByBookerId(Long userId, Pageable pageable);

    List<Booking> findAllByItemOwnerId(Long userId, Pageable pageable);

    List<Booking> findAllByItemOwnerIdAndEndBefore(Long userId, LocalDateTime time, Pageable pageable);

    List<Booking> findAllByItemOwnerIdAndStartAfter(Long userId, LocalDateTime time, Pageable pageable);

    List<Booking> findAllByItemOwnerIdAndStatusIs(Long userId, BookingStatus status, Pageable pageable);

    @Query("SELECT b " +
            "FROM Booking b " +
            "WHERE b.item.owner.id = ?1 " +
            "AND b.start < ?2 " +
            "AND b.end > ?2" +
            "ORDER BY b.start DESC")
    List<Booking> findAllByItemOwnerIdCurrentBooking(Long userId, LocalDateTime time, Pageable pageable);
}
