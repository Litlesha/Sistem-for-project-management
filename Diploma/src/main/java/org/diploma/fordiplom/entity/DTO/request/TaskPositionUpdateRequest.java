package org.diploma.fordiplom.entity.DTO.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskPositionUpdateRequest {
    private Long taskId;
    private Integer position;
}