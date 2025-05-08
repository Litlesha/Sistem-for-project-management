package org.diploma.fordiplom.entity.DTO.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentRequest {
    private Long taskId;
    private String text;
}
