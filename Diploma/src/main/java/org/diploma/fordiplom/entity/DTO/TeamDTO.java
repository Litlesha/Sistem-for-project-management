package org.diploma.fordiplom.entity.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeamDTO {
    private Long id_team;
    private String team_name;

    public TeamDTO(Long id_team, String team_name) {
        this.id_team = id_team;
        this.team_name = team_name;
    }
}
