package ru.practicum.shareit.item.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRepositoryTest {
    private final ItemRepository repository;
    private final UserRepository userRepository;
    private User testUser;
    private Item testItem;

    @BeforeEach
    void setUp() {
        testUser = new User(1L, "Danila", "test@mail.ru");
        testItem = new Item(1L, "test item", "desc", 1, true);
        testItem.setOwner(testUser);
        testUser = userRepository.save(testUser);
        testItem = repository.save(testItem);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteById(testUser.getId());
        repository.deleteById(testItem.getId());
    }

    @Test
    public void shouldFindItemByOwner() {
        List<Long> items = repository.findItemIdByOwner(testUser.getId(), PageRequest.of(0, 10));
        Optional<Item> savedItem = repository.findById(items.get(0));

        assertNotNull(savedItem.get());
        assertEquals(savedItem.get(), testItem);
    }
}
