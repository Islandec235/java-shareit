package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.user.model.User;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CommentMapperTest {
    private final CommentMapper mapper = new CommentMapper();
    private CommentDto commentDto;
    private Comment comment;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User(1L, "testAuthor", "email@email.ru");
        commentDto = new CommentDto(1L, "test", "testAuthor", Instant.now());
        comment = new Comment(1L, "test", commentDto.getCreated());
        comment.setUser(user);
    }

    @Test
    public void shouldReturnCommentDto() {
        CommentDto toDto = mapper.toCommentDto(comment);

        assertEquals(toDto, commentDto);
    }

    @Test
    public void shouldReturnComment() {
        Comment toEntity = mapper.toComment(commentDto);
        toEntity.setUser(user);

        assertEquals(toEntity, comment);
    }

    @Test
    public void shouldReturnListCommentDto() {
        CommentDto otherCommentDto = new CommentDto(2L, "test2", "testAuthor", Instant.now());
        Comment otherComment = new Comment(2L, "test2", otherCommentDto.getCreated());
        otherComment.setUser(user);
        List<Comment> commentList = List.of(comment, otherComment);
        List<CommentDto> commentDtoList = List.of(commentDto, otherCommentDto);


        List<CommentDto> toDto = mapper.listCommentDto(commentList);

        assertEquals(toDto, commentDtoList);
    }
}
