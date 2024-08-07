package ru.practicum.shareit.item.storage;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    void deleteById(Long id);

    @Query("SELECT i.id FROM Item i WHERE i.owner.id = ?1 ORDER BY i.id")
    List<Long> findItemIdByOwner(Long ownerId, Pageable pageable);

    List<Item> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndAvailableTrue(
            String name,
            String description,
            Pageable pageable
    );
}
