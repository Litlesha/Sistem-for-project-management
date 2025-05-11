package org.diploma.fordiplom.entity.DTO;

import lombok.Getter;
import lombok.Setter;
import org.diploma.fordiplom.entity.TeamEntity;

@Getter
@Setter
public class TeamDTO {
    private Long id_team;
    private String team_name;
    private String description;


    public TeamDTO(Long id_team, String team_name) {
        this.id_team = id_team;
        this.team_name = team_name;

    }
    public TeamDTO(TeamEntity entity) {
        this.id_team = entity.getId_team();
        this.team_name = entity.getTeam_name();
        this.description = entity.getDescription();

    }
}
