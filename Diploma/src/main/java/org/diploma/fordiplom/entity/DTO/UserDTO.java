package org.diploma.fordiplom.entity.DTO;

import lombok.Getter;
import lombok.Setter;
import org.diploma.fordiplom.entity.UserEntity;
@Getter
@Setter
public class UserDTO {
    private String username;
    private String email;

    public UserDTO(UserEntity entity) {
        this.username = entity.getUsername();
        this.email = entity.getEmail();
    }
}
