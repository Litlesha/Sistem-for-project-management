package org.diploma.fordiplom.controller;

import jakarta.servlet.http.HttpSession;
import org.diploma.fordiplom.config.MyUserDetailService;
import org.diploma.fordiplom.service.GitHubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
public class GitHubController {
    @Autowired
    GitHubService gitHubService;

    @Value("${github.client.id}")
    private String clientId;

    @Value("${github.client.secret}")
    private String clientSecret;

    @Value("${github.redirect.uri}")
    private String redirectUri;

    @GetMapping("/connect/github")
    public RedirectView redirectToGitHub(@RequestParam(name = "return_to", required = false) String returnTo,
                                         HttpSession session) {
        if (returnTo != null) {
            session.setAttribute("github_return_to", returnTo);
        }

        String url = "https://github.com/login/oauth/authorize" +
                "?client_id=" + clientId +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                "&scope=repo user";
        return new RedirectView(url);
    }


    @GetMapping("/connect/github/callback")
    public RedirectView handleGithubCallback(@RequestParam String code, HttpSession session, Principal principal) {
        String tokenUrl = "https://github.com/login/oauth/access_token";

        String user = principal.getName();

        RestTemplate restTemplate = new RestTemplate();

        Map<String, String> response = restTemplate.postForObject(
                tokenUrl,
                Map.of(
                        "client_id", clientId,
                        "client_secret", clientSecret,
                        "code", code,
                        "redirect_uri", redirectUri
                ),
                Map.class
        );

        String accessToken = (String) response.get("access_token");

        gitHubService.saveToken(user, accessToken);

        String returnTo = (String) session.getAttribute("github_return_to");
        if (returnTo == null || returnTo.isBlank()) {
            returnTo = "/"; // запасной путь
        }

        return new RedirectView(returnTo);
    }

    @GetMapping("/github/repos")
    @ResponseBody
    public ResponseEntity<?> getRepos(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Пользователь не авторизован");
        }

        String userEmail = principal.getName();
        List<Map<String, Object>> repos = gitHubService.getUserRepositories(userEmail);
        return ResponseEntity.ok(repos);
    }

    @GetMapping("/github/check-auth")
    public ResponseEntity<?> checkGithubAuth(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Пользователь не авторизован");
        }

        String email = principal.getName();
        boolean isConnected = gitHubService.hasToken(email);

        if (!isConnected) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("GitHub не подключён");
        }

        return ResponseEntity.ok("OK");
    }


    @PostMapping("/github/create-branch")
    public ResponseEntity<?> createBranch(
            @RequestBody Map<String, String> payload,
            Principal principal) {
        String email = principal.getName();
        String owner = payload.get("repoOwner");
        String repo = payload.get("repoName");
        String base = payload.get("baseBranch");
        String newBranch = payload.get("newBranch");

        gitHubService.createBranchFromBase(email, owner, repo, base, newBranch);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/github/{owner}/{repo}/branches")
    @ResponseBody
    public List<Map<String, Object>> getBranches(@PathVariable String owner,
                                    @PathVariable String repo,
                                    Principal principal) {
        String email = principal.getName();
        return gitHubService.getBranches(email, owner, repo);
    }



}
