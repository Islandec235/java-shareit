package ru.practicum.shareit.item.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAll();

    Item save(Item user);

    void deleteById(Long id);

    Optional<Item> findById(Long id);

    @Query("SELECT i.id FROM Item i WHERE i.owner.id = ?1 ")
    List<Long> findItemIdByOwner(Long ownerId);

    List<Item> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndAvailableTrue(String name,
                                                                                               String description);
}
