package org.diploma.fordiplom.entity.DTO;

import lombok.Getter;
import lombok.Setter;
import org.diploma.fordiplom.entity.TaskEntity;
import org.diploma.fordiplom.entity.UserEntity;

import java.util.List;

@Getter
@Setter
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private List<TaskDTO> tasks;

    public UserDTO(UserEntity entity) {
        this.id = entity.getId_user();
        this.username = entity.getUsername();
        this.email = entity.getEmail();
    }
    public UserDTO(UserEntity entity, List<TaskEntity> assignedTasks) {
        this(entity);
        this.tasks = assignedTasks.stream().map(TaskDTO::new).toList();
    }
}
