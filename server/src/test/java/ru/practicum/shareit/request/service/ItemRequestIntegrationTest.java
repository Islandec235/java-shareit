package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestIntegrationTest {
    private final ItemRequestServiceImpl service;
    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;

    private User user;
    private ItemRequest request;

    @BeforeEach
    void setUp() {
        user = new User(1L, "Test user", "test@mail.ru");
        request = new ItemRequest(1L, "desc", LocalDateTime.now());
        user = userRepository.save(user);
        request.setUser(user);
        request = requestRepository.save(request);
    }

    @Test
    public void shouldReturnRequestById() {
        ItemRequestDto requestDto = service.getItemRequestById(user.getId(), request.getId());

        assertThat(requestDto).isNotNull();
        assertThat(requestDto.getId()).isEqualTo(request.getId());
        assertThat(requestDto.getItems()).isEqualTo(Collections.emptyList());
        assertThat(requestDto.getDescription()).isEqualTo("desc");
        assertThat(requestDto.getCreated()).isEqualTo(request.getCreated());
    }
}
