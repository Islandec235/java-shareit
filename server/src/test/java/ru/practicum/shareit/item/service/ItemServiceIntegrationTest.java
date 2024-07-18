package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemCommentAndBookingDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceIntegrationTest {
    private final ItemServiceImpl service;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    private User user;
    private Item item;

    @BeforeEach
    void setUp() {
        user = new User(1L, "Test user", "test@mail.ru");
        item = new Item(1L, "Test", "testdesc", 0, true);
        user = userRepository.save(user);
        item.setOwner(user);
        item = itemRepository.save(item);
    }

    @Test
    public void shouldReturnItemsByOwner() {
        List<ItemCommentAndBookingDto> items = service.getItemsByOwner(user.getId(), 0, 20);

        assertThat(items).isNotNull();
        assertThat(items.size()).isEqualTo(1);
        assertThat(items.get(0).getId()).isEqualTo(item.getId());
        assertThat(items.get(0).getName()).isEqualTo("Test");
        assertThat(items.get(0).getDescription()).isEqualTo("testdesc");
        assertThat(items.get(0).getRentals()).isEqualTo(0);
        assertThat(items.get(0).getAvailable()).isEqualTo(true);

        assertThat(items.get(0).getComments()).isEqualTo(Collections.emptyList());
    }
}