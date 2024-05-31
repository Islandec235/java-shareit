package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import javax.validation.ValidationException;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    @Override
    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    @Override
    public User create(User user) {
        validation(user);
        return userStorage.createUser(user);
    }

    @Override
    public User update(Long id, User user) {
        return userStorage.updateUser(id, user);
    }

    @Override
    public void delete(Long id) {
        userStorage.deleteUser(id);
    }

    @Override
    public User getUserById(Long id) {
        return userStorage.getUserById(id);
    }

    private void validation(User user) {
        if (user.getEmail() == null) {
            ValidationException e = new ValidationException("Ошибка валидации");
            log.error(String.valueOf(user), e);
            throw e;
        }
    }
}
