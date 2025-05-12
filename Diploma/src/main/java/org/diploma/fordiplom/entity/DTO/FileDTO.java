package org.diploma.fordiplom.entity.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
@Getter
@Setter
@AllArgsConstructor
public class FileDTO {
    private Long id;
    private String fileName;
    private String contentType;
    private Instant uploadedAt;

}
