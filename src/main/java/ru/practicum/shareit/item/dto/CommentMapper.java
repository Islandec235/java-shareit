package ru.practicum.shareit.item.dto;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Comment;

import java.util.ArrayList;
import java.util.List;

@Component
public class CommentMapper {
    public CommentDto toCommentDto(Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getText(),
                comment.getAuthorName()
        );
    }

    public Comment toComment(CommentDto commentDto) {
        return new Comment(
                commentDto.getId(),
                commentDto.getText(),
                commentDto.getCreated()
        );
    }

    public List<CommentDto> listCommentDto(List<Comment> comments) {
        List<CommentDto> commentsDto = new ArrayList<>();

        for (Comment comment : comments) {
            commentsDto.add(toCommentDto(comment));
        }

        return commentsDto;
    }

    public List<Comment> listComment(List<CommentDto> commentsDto) {
        List<Comment> comments = new ArrayList<>();

        for (CommentDto commentDto : commentsDto) {
            comments.add(toComment(commentDto));
        }

        return comments;
    }

}
