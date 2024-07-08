package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentMapper;
import ru.practicum.shareit.item.dto.ItemCommentAndBookingDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.exception.RequestNotFoundException;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.exception.UserConflictException;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;
    private final BookingMapper bookingMapper;
    private final ItemRepository itemRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    @Transactional
    public ItemDto create(ItemDto itemDto, Long ownerId) {
        Item item = itemMapper.toItem(itemDto);

        if (item.getAvailable() == null) {
            log.error(String.valueOf(item));
            throw new ValidationException("Поле аренды не может быть пустым");
        }

        Optional<User> owner = userRepository.findById(ownerId);

        if (owner.isEmpty()) {
            log.error("Пользователь с id = {} не найден", ownerId);
            throw new UserNotFoundException("Не найден владелец с id = " + ownerId);
        }

        item.setOwner(owner.get());
        Long requestId = itemDto.getRequestId();

        if (requestId != null) {
            Optional<ItemRequest> requestOptional = itemRequestRepository.findById(requestId);

            if (requestOptional.isEmpty()) {
                log.error("Запрос с id = {} не найден", requestId);
                throw new RequestNotFoundException("Не найден запрос с id = " + requestId);
            }

            item.setRequest(requestOptional.get());
        }

        ItemDto newItemDto = itemMapper.toItemDto(itemRepository.save(item));
        newItemDto.setRequestId(requestId);

        return newItemDto;
    }

    @Override
    @Transactional
    public ItemDto update(ItemDto itemDto, Long ownerId) {
        Optional<User> ownerOptional = userRepository.findById(ownerId);

        if (ownerOptional.isEmpty()) {
            log.error("Пользователь с id = {} не найден", ownerId);
            throw new UserNotFoundException("Не найден владелец с id = " + ownerId);
        }

        User owner = ownerOptional.get();
        Optional<Item> itemInStorage = itemRepository.findById(itemDto.getId());

        if (itemInStorage.isEmpty()) {
            log.error(String.valueOf(itemDto));
            throw new ItemNotFoundException("Не найден предмет с id = " + itemDto.getId());
        }

        if (!owner.equals(itemInStorage.get().getOwner())) {
            log.error("Пользователь с id = {} не является владельцем предмета = {}", ownerId, itemDto);
            throw new UserConflictException("Пользователь не владелец предмета");
        }

        Item item;

        if (itemDto.getAvailable() != null && itemDto.getDescription() != null && itemDto.getName() != null) {
            item = itemMapper.toItem(itemDto);
            item.setOwner(owner);
            return itemMapper.toItemDto(itemRepository.save(item));
        } else if (itemDto.getAvailable() != null) {
            itemDto.setName(itemInStorage.get().getName());
            itemDto.setDescription(itemInStorage.get().getDescription());
            item = itemMapper.toItem(itemDto);
            item.setOwner(owner);
            return itemMapper.toItemDto(itemRepository.save(item));
        } else if (itemDto.getName() != null) {
            itemDto.setDescription(itemInStorage.get().getDescription());
            itemDto.setAvailable(itemInStorage.get().getAvailable());
            item = itemMapper.toItem(itemDto);
            item.setOwner(owner);
            return itemMapper.toItemDto(itemRepository.save(item));
        } else if (itemDto.getDescription() != null) {
            itemDto.setName(itemInStorage.get().getName());
            itemDto.setAvailable(itemInStorage.get().getAvailable());
            item = itemMapper.toItem(itemDto);
            item.setOwner(owner);
            return itemMapper.toItemDto(itemRepository.save(item));
        } else {
            log.error(String.valueOf(itemDto));
            throw new ItemNotFoundException("Предмет не найден");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ItemCommentAndBookingDto getItemById(Long userId, Long id) {
        Optional<Item> itemOptional = itemRepository.findById(id);

        if (itemOptional.isEmpty()) {
            log.error(String.valueOf(id));
            throw new ItemNotFoundException("Не найден предмет с id = " + id);
        }

        ItemCommentAndBookingDto itemDto = itemMapper.toItemWithCommentDto(itemOptional.get());

        if (itemOptional.get().getOwner().getId().equals(userId)) {
            Optional<Booking> lastBooking = bookingRepository.findByItemIdAndStatusIsAndStartBeforeOrderByStartDesc(
                    itemDto.getId(),
                    BookingStatus.APPROVED,
                    LocalDateTime.now()
            ).stream().findFirst();
            Optional<Booking> nextBooking = bookingRepository.findByItemIdAndStatusIsAndStartAfterOrderByStartAsc(
                    itemDto.getId(),
                    BookingStatus.APPROVED,
                    LocalDateTime.now()
            ).stream().findFirst();

            if (lastBooking.isEmpty()) {
                itemDto.setLastBooking(null);
            } else {
                itemDto.setLastBooking(bookingMapper.toBookingWithBookerIdDto(lastBooking.get()));
            }

            if (nextBooking.isEmpty()) {
                itemDto.setNextBooking(null);
            } else {
                itemDto.setNextBooking(bookingMapper.toBookingWithBookerIdDto(nextBooking.get()));
            }
        }

        List<CommentDto> comments = commentMapper.listCommentDto(commentRepository.findByItemId(itemDto.getId()));
        itemDto.setComments(comments);
        return itemDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemCommentAndBookingDto> getItemsByOwner(Long ownerId, Integer from, Integer size) {
        List<Long> itemsId = itemRepository.findItemIdByOwner(ownerId, PageRequest.of(from / size, size));
        List<ItemCommentAndBookingDto> itemsDto = new ArrayList<>();

        for (Long id : itemsId) {
            itemsDto.add(getItemById(ownerId, id));
        }

        return itemsDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> search(String text, Integer from, Integer size) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }

        return itemMapper.listItemDto(
                itemRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndAvailableTrue(
                        text,
                        text,
                        PageRequest.of(from / size, size)
                )
        );
    }

    @Override
    @Transactional
    public CommentDto createComment(Long bookerId, Long itemId, CommentDto commentDto) {
        Optional<Booking> booking = bookingRepository.findAllByItemIdAndBookerIdAndStatusIsAndEndBefore(
                itemId,
                bookerId,
                BookingStatus.APPROVED,
                LocalDateTime.now()).stream().findFirst();

        if (booking.isEmpty()) {
            log.error("Не найден заказ с itemId = {}, bookerId = {}", itemId, bookerId);
            throw new ValidationException("Отзыв нельзя оставить без заказа");
        }

        Comment comment = commentMapper.toComment(commentDto);
        User user = booking.get().getBooker();
        Item item = booking.get().getItem();
        comment.setUser(user);
        comment.setItem(item);
        return commentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByItem(Long itemId) {
        return commentMapper.listCommentDto(commentRepository.findByItemId(itemId));
    }
}
