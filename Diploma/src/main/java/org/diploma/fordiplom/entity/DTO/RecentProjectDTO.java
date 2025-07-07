package org.diploma.fordiplom.entity.DTO;

public record RecentProjectDTO(
        Long id,
        String name,
        String key,
        String description,
        int activeSprintCount
) {}