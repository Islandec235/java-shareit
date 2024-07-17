package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceIntegrationTest {
    private final UserServiceImpl service;
    private final UserRepository repository;
    private final UserMapper mapper;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User(1L, "Test user", "test@mail.ru");
        user = repository.save(user);
    }

    @Test
    public void shouldUpdateUser() {
        user.setName("Update user");
        user.setEmail("new@mail.ru");
        User updatedUser = mapper.toUser(service.update(user.getId(), mapper.toUserDto(user)));

        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getId()).isEqualTo(user.getId());
        assertThat(updatedUser.getName()).isEqualTo("Update user");
        assertThat(updatedUser.getEmail()).isEqualTo("new@mail.ru");

        User userInStorage = repository.findById(user.getId()).orElse(null);
        assertThat(userInStorage).isNotNull();
        assertThat(userInStorage.getName()).isEqualTo("Update user");
        assertThat(userInStorage.getEmail()).isEqualTo("new@mail.ru");
    }

}
