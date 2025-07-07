package org.diploma.fordiplom.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public interface GitHubService {
    void saveToken(String email, String accessToken);
    String getToken(String email);
    List<Map<String, Object>> getUserRepositories(String email);
    void createBranchFromBase(String email, String owner, String repo, String baseBranch, String newBranch);
    List<Map<String, Object>> getBranches(String email, String repoOwner, String repoName);
    boolean hasToken(String email);
}
