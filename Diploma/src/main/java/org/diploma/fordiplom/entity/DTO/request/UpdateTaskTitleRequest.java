package org.diploma.fordiplom.entity.DTO.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTaskTitleRequest {
    @NotBlank(message = "Название задачи не может быть пустым")
    private String title;

}
