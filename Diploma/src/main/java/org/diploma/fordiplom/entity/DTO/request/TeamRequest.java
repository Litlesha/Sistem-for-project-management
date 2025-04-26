package org.diploma.fordiplom.entity.DTO.request;

import java.util.List;

public class TeamRequest {
    private String team_name;
    private String description;
    private List<String> emails;
    private String teamImgPath;

    public String getTeamImgPath() {
        return teamImgPath;
    }

    public void setTeamImgPath(String teamImgPath) {
        this.teamImgPath = teamImgPath;
    }

    public List<String> getEmails() {
        return emails;
    }

    public void setEmails(List<String> emails) {
        this.emails = emails;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTeam_name() {
        return team_name;
    }

    public void setTeam_name(String team_name) {
        this.team_name = team_name;
    }
}
