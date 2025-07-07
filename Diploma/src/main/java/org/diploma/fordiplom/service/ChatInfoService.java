package org.diploma.fordiplom.service;

import org.diploma.fordiplom.entity.DTO.response.ChatInfoResponse;
import org.springframework.stereotype.Service;

@Service
public interface ChatInfoService {
    ChatInfoResponse getChatInfo(String type, Long id);
}
