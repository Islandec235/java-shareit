package ru.practicum.shareit.user.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.exceptions.UserConflictException;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Repository
public class InMemoryUserStorage implements UserStorage {
    private long id = 1L;
    private final HashMap<Long, User> users = new HashMap<>();

    @Override
    public List<User> getAllUsers() {
        log.info("Запрос на получение всех пользователей");
        return new ArrayList<>(users.values());
    }

    @Override
    public User createUser(User user) {
        for (User userInStorage : users.values()) {
            String email = user.getEmail();
            if (userInStorage.getEmail().equals(email)) {
                String message = String.format("Пользователь с Email = {}, уже существует", email);
                UserConflictException e = new UserConflictException(message);
                log.error(String.valueOf(user), e);
                throw e;
            }
        }

        user.setId(id);
        this.id++;
        users.put(user.getId(), user);
        log.info("Запрос на создание пользователя");
        return users.get(user.getId());
    }

    @Override
    public User updateUser(Long id, User user) {
        if (!users.containsKey(id)) {
            UserNotFoundException e = new UserNotFoundException("Пользователь не найден");
            log.error(String.valueOf(id), e);
            throw e;
        }

        User userInStorage = users.get(id);

        for (User otherUser : users.values()) {
            if (otherUser != userInStorage && otherUser.getEmail().equals(user.getEmail())) {
                String message = String.format("Email пользователя совпадает с id = {}", otherUser.getId());
                UserConflictException e = new UserConflictException(message);
                log.error(String.valueOf(user), e);
                throw e;
            }
        }

        if (user.getName() != null && user.getEmail() != null) {
            users.put(id, user);
            log.info("Запрос на обновление пользователя");
            return users.get(id);
        } else if (user.getEmail() != null) {
            userInStorage.setEmail(user.getEmail());
            return users.get(id);
        } else if (user.getName() != null) {
            userInStorage.setName(user.getName());
            return users.get(id);
        } else {
            UserNotFoundException e = new UserNotFoundException("Пользователь не найден");
            log.error(String.valueOf(user), e);
            throw e;
        }
    }

    @Override
    public void deleteUser(Long id) {
        if (!users.containsKey(id)) {
            UserNotFoundException e = new UserNotFoundException("Пользователь не найден");
            log.error(String.valueOf(id), e);
            throw e;
        } else {
            log.info("Запрос на удаление пользователя");
            users.remove(id);
        }
    }

    @Override
    public User getUserById(Long id) {
        if (!users.containsKey(id)) {
            UserNotFoundException e = new UserNotFoundException("Пользователь не найден");
            log.error(String.valueOf(id), e);
            throw e;
        } else {
            log.info("Запрос на получение пользователя по id");
            return users.get(id);
        }
    }
}
