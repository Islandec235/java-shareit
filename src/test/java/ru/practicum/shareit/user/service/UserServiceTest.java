package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.exception.UserConflictException;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@Transactional
@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceTest {
    @Mock
    UserRepository mockRepository;
    @Mock
    UserMapper mockMapper;
    @InjectMocks
    UserServiceImpl userService;

    private UserDto userDto;
    private User user;

    @BeforeEach
    void setUp() {
        userDto = new UserDto(1L, "Danila", "email@mail.ru");
        user = new User(1L, "Danila", "email@mail.ru");
    }

    @Test
    public void shouldGetAllUsers() {
        when(mockRepository.findAll()).thenReturn(List.of(user));
        when(mockMapper.listUserDto(List.of(user))).thenReturn(List.of(userDto));
        List<UserDto> users = userService.getAllUsers();

        assertEquals(users, List.of(userDto));
    }

    @Test
    public void shouldCreateUser() {
        User createdUser = new User(1L, "Danila", "email@mail.ru");
        when(mockMapper.toUser(userDto)).thenReturn(user);
        when(mockMapper.toUserDto(user)).thenReturn(userDto);
        when(mockRepository.save(user)).thenReturn(createdUser);
        when(mockRepository.findByEmail(user.getEmail())).thenReturn(null);

        UserDto createdUserDto = userService.create(userDto);

        assertEquals(createdUserDto, userDto);
    }

    @Test
    public void shouldCreateUserWhenEmailExist() {
        User otherUser = new User(2L, "Gena", "email@mail.ru");
        when(mockMapper.toUser(userDto)).thenReturn(user);
        when(mockRepository.findByEmail(user.getEmail())).thenReturn(otherUser);

        assertThrows(UserConflictException.class,
                () -> userService.create(userDto));
    }

    @Test
    public void shouldUpdateUserWithoutName() {
        userDto.setName(null);
        userDto.setEmail("test@mail.ru");
        user.setName(null);
        user.setEmail("test@mail.ru");
        User userDb = new User(1L, "Albert", "email@mail.ru");
        UserDto userDbDto = new UserDto(1L, "Albert", "email@mail.ru");
        when(mockMapper.toUser(userDto)).thenReturn(user);
        when(mockMapper.toUser(userDbDto)).thenReturn(userDb);
        when(mockMapper.toUserDto(user)).thenReturn(userDto);
        when(mockMapper.toUserDto(userDb)).thenReturn(userDbDto);
        when(mockRepository.findById(1L)).thenReturn(Optional.of(userDb));
        when(mockRepository.save(user)).thenReturn(user);
        when(mockRepository.findByEmail(user.getEmail())).thenReturn(userDb);

        UserDto updatedUserDto = userService.update(userDto);
        userDto.setName("Albert");

        assertEquals(updatedUserDto, userDto);
    }

    @Test
    public void shouldReturnExceptionForUpdateWith() {
        User userDb = new User(1L, "Albert", "test@mail.ru");
        User userByEmail = new User(2L, "Gena", "email@mail.ru");
        UserDto userDbDto = new UserDto(1L, "Albert", "test@mail.ru");
        when(mockMapper.toUser(userDbDto)).thenReturn(userDb);
        when(mockMapper.toUserDto(userDb)).thenReturn(userDbDto);
        when(mockRepository.findById(1L)).thenReturn(Optional.of(userDb));
        when(mockRepository.findByEmail(user.getEmail())).thenReturn(userByEmail);

        assertThrows(UserConflictException.class,
                () -> userService.update(userDto));
    }

    @Test
    public void shouldUpdateUserWithoutEmail() {
        userDto.setEmail(null);
        userDto.setName("Danila");
        user.setEmail(null);
        user.setName("Danila");
        User userDb = new User(1L, "Albert", "email@mail.ru");
        UserDto userDbDto = new UserDto(1L, "Albert", "email@mail.ru");
        when(mockMapper.toUser(userDto)).thenReturn(user);
        when(mockMapper.toUser(userDbDto)).thenReturn(userDb);
        when(mockMapper.toUserDto(user)).thenReturn(userDto);
        when(mockMapper.toUserDto(userDb)).thenReturn(userDbDto);
        when(mockRepository.findById(1L)).thenReturn(Optional.of(userDb));
        when(mockRepository.save(user)).thenReturn(user);
        when(mockRepository.findByEmail(userDto.getEmail())).thenReturn(null);

        UserDto updatedUserDto = userService.update(userDto);
        userDto.setEmail("email@mail.ru");

        assertEquals(updatedUserDto, userDto);
    }

    @Test
    public void shouldDeleteUser() {
        userService.delete(1L);
        verify(mockRepository, times(1)).deleteById(1L);
    }

    @Test
    public void shouldGetUserById() {
        when(mockRepository.findById(1L)).thenReturn(Optional.of(user));
        when(mockMapper.toUserDto(user)).thenReturn(userDto);

        UserDto foundUser = userService.getUserById(1L);

        assertEquals(foundUser, userDto);
    }

    @Test
    public void shouldNotFoundUserById() {
        when(mockRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.getUserById(1L));
    }
}
