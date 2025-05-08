package org.diploma.fordiplom.entity.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TagDTO {

    private Long id;  // Идентификатор метки
    private String name;  // Имя метки

    // Конструкторы
    public TagDTO() {
    }

    public TagDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
