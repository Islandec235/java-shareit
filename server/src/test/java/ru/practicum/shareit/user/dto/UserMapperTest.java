package ru.practicum.shareit.user.dto;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class UserMapperTest {
    private final UserMapper mapper = new UserMapper();
    private User user = new User(1L, "Danila", "email@mail.ru");
    private UserDto userDto = new UserDto(1L, "Danila", "email@mail.ru");

    @Test
    public void shouldReturnUserDto() {
        UserDto toDto = mapper.toUserDto(user);

        assertEquals(userDto, toDto);
    }

    @Test
    public void shouldReturnUser() {
        User toEntity = mapper.toUser(userDto);

        assertEquals(user, toEntity);
    }

    @Test
    public void shouldReturnListUserDto() {
        User otherUser = new User(2L, "Danila", "email@mail.ru");
        UserDto otherUserDto = new UserDto(2L, "Danila", "email@mail.ru");
        List<User> userList = List.of(user, otherUser);
        List<UserDto> userDtoList = List.of(userDto, otherUserDto);


        List<UserDto> toDto = mapper.listUserDto(userList);

        assertEquals(toDto, userDtoList);
    }

    @Test
    public void shouldReturnListUser() {
        User otherUser = new User(2L, "Danila", "email@mail.ru");
        UserDto otherUserDto = new UserDto(2L, "Danila", "email@mail.ru");
        List<User> userList = List.of(user, otherUser);
        List<UserDto> userDtoList = List.of(userDto, otherUserDto);


        List<User> toEntity = mapper.listUser(userDtoList);

        assertEquals(toEntity, userList);
    }
}
