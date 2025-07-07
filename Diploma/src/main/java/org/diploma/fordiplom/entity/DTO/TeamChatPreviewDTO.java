package org.diploma.fordiplom.entity.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeamChatPreviewDTO {
    private Long id;
    private String teamName;
    private String lastMessage;

    public TeamChatPreviewDTO(Long id, String teamName, String lastMessage) {
        this.id = id;
        this.teamName = teamName;
        this.lastMessage = lastMessage;
    }
}
