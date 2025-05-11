package org.diploma.fordiplom.entity.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectDTO {
    private Long id;       // ID проекта
    private String name;

    public ProjectDTO(long id, String name) {
    }
}
