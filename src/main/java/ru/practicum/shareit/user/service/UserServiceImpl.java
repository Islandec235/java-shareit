package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.exception.UserConflictException;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper mapper;
    private final UserRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return mapper.listUserDto(repository.findAll());
    }

    @Override
    @Transactional
    public UserDto create(UserDto userDto) {
        User user = mapper.toUser(userDto);

        if (repository.findByEmail(user.getEmail()) != null) {
            log.error("Дубликат email = {}", user.getEmail());
            throw new UserConflictException("Пользователь уже существует");
        }

        return mapper.toUserDto(repository.save(user));
    }

    @Override
    @Transactional
    public UserDto update(UserDto userDto) {
        User userInStorage = mapper.toUser(getUserById(userDto.getId()));
        User userByEmail = repository.findByEmail(userDto.getEmail());

        if (userByEmail != null && !userByEmail.equals(userInStorage)) {
            log.error(String.valueOf(userDto));
            throw new UserConflictException("Еmail пользователя не совпадает с id");
        }

        if (userDto.getName() != null && userDto.getEmail() != null) {
            return mapper.toUserDto(repository.save(mapper.toUser(userDto)));
        } else if (userDto.getEmail() == null) {
            userDto.setEmail(userInStorage.getEmail());
            return mapper.toUserDto(repository.save(mapper.toUser(userDto)));
        } else {
            userDto.setName(userInStorage.getName());
            return mapper.toUserDto(repository.save(mapper.toUser(userDto)));
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        Optional<User> user = repository.findById(id);

        if (user.isEmpty()) {
            log.error(String.valueOf(id));
            throw new UserNotFoundException("Пользователь не найден");
        }

        return mapper.toUserDto(user.get());
    }
}
