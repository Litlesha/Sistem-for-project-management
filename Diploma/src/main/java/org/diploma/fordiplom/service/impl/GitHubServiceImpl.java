package org.diploma.fordiplom.service.impl;

import org.diploma.fordiplom.entity.UserEntity;
import org.diploma.fordiplom.repository.UserRepository;
import org.diploma.fordiplom.service.GitHubService;
import org.diploma.fordiplom.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;


@Service
public class GitHubServiceImpl implements GitHubService {
    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;

    @Override
    public void saveToken(String email, String accessToken) {
        var user = userService.getUserByEmail(email);
        user.setGitHubAccessToken(accessToken); // поле в сущности User
        userRepository.save(user);
    }

    @Override
    public String getToken(String email) {
        var user = userService.getUserByEmail(email);
        return user.getGitHubAccessToken();
    }

    @Override
    public boolean hasToken(String email) {
        String token = userService.getGitHubToken(email);
        return token != null && !token.isBlank();
    }


    @Override
    public List<Map<String, Object>> getUserRepositories(String email) {
        String accessToken = getToken(email);
        if (accessToken == null || accessToken.isBlank()) {
            return List.of(); // или выбрось исключение
        }

        String url = "https://api.github.com/user/repos";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<List> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        return response.getBody();
    }

    @Override
    public void createBranchFromBase(String email, String owner, String repo, String baseBranch, String newBranch) {
        String accessToken = getToken(email);
        if (accessToken == null || accessToken.isBlank()) {
            throw new RuntimeException("GitHub токен не найден");
        }

        // Шаг 1: Получить SHA базовой ветки
        String refUrl = "https://api.github.com/repos/" + owner + "/" + repo + "/git/ref/heads/" + baseBranch;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> refResponse = restTemplate.exchange(refUrl, HttpMethod.GET, entity, Map.class);
        Map<String, Object> object = (Map<String, Object>) refResponse.getBody().get("object");
        String sha = (String) object.get("sha");

        // Шаг 2: Создать новую ветку
        String createUrl = "https://api.github.com/repos/" + owner + "/" + repo + "/git/refs";

        Map<String, Object> request = Map.of(
                "ref", "refs/heads/" + newBranch,
                "sha", sha
        );

        HttpEntity<Map<String, Object>> createEntity = new HttpEntity<>(request, headers);

        restTemplate.exchange(createUrl, HttpMethod.POST, createEntity, Map.class);
    }


    public List<Map<String, Object>> getBranches(String email, String repoOwner, String repoName) {
        String token = getToken(email);

        String url = "https://api.github.com/repos/" + repoOwner + "/" + repoName + "/branches";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<List> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        return response.getBody(); // список веток
    }

}
