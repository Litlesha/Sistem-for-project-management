package org.diploma.fordiplom.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "action_type", schema = "diploma")
public class ActionTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code; // Например: "create", "update", "delete"

    @Column(nullable = false)
    private String name; // Локализованное имя: "Создание", "Обновление", "Удаление"
}
