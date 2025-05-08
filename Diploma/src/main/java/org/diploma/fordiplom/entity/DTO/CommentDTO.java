package org.diploma.fordiplom.entity.DTO;

import lombok.Getter;
import lombok.Setter;
import org.diploma.fordiplom.entity.CommentEntity;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Getter
@Setter
public class CommentDTO {
    private  Long id;
    private  String text;
    private  UserDTO  author;
    private  String createdAt;
    public CommentDTO(CommentEntity entity) {
        this.id = entity.getId();
        this.text = entity.getText();

        if (entity.getAuthor() != null) {
            this.author = new UserDTO(entity.getAuthor());
        } else {
            this.author = null;
        }

        this.createdAt = entity.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
