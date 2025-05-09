package org.diploma.fordiplom.entity.DTO.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddTeamToProjectRequest {
    private Long projectId;
    private Long teamId;

}
