package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    List<UserDto> getAllUsers();

    UserDto create(UserDto userDto);

    UserDto update(Long userId, UserDto userDto);

    void delete(Long id);

    UserDto getUserById(Long id);
}
