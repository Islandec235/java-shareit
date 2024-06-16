package ru.practicum.shareit.user.dto;

import lombok.NonNull;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserMapper {
    public UserDto toUserDto(@NonNull User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    public User toUser(@NonNull UserDto userDto) {
        return new User(
                userDto.getId(),
                userDto.getName(),
                userDto.getEmail()
        );
    }

    public List<UserDto> listUserDto(@NonNull List<User> users) {
        List<UserDto> usersDto = new ArrayList<>();

        for (User user : users) {
            usersDto.add(toUserDto(user));
        }

        return usersDto;
    }

    public List<User> listUser(@NonNull List<UserDto> usersDto) {
        List<User> users = new ArrayList<>();

        for (UserDto userDto : usersDto) {
            users.add(toUser(userDto));
        }

        return users;
    }
}