package com.argos.infrastructure.github;

import com.argos.domain.ports.GitRepoPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;

/**
 * Infrastructure: fetches raw PR diff text from GitHub using the kohsuke API
 * for the diff URL and RestTemplate for the HTTP GET.
 */
@Service
public class GitHubService implements GitRepoPort {

    private static final Logger log = LoggerFactory.getLogger(GitHubService.class);
    private static final String AUTH_HEADER_PREFIX = "token ";

    private final String targetRepo;
    private final String authToken;
    private final RestTemplate restTemplate;
    private volatile GitHub gitHub;

    @Autowired
    public GitHubService(
            @Value("${github.target.repo:}") String targetRepo,
            @Value("${github.auth.token:}") String authToken,
            RestTemplate restTemplate) {
        this(targetRepo, authToken, restTemplate, null);
    }

    /** Constructor for tests: pass a pre-built GitHub instance to avoid real API calls. */
    GitHubService(String targetRepo, String authToken, RestTemplate restTemplate, GitHub gitHub) {
        this.targetRepo = targetRepo != null ? targetRepo.trim() : "";
        this.authToken = authToken != null ? authToken.trim() : "";
        this.restTemplate = restTemplate;
        this.gitHub = gitHub;
    }

    /**
     * Fetches the raw diff text for the given pull request.
     *
     * @param prId pull request number
     * @return raw diff body, or empty string if PR not found / no access
     * @throws IllegalStateException if repo or token is not configured
     */
    public String getRawDiff(int prId) {
        if (targetRepo.isBlank() || authToken.isBlank()) {
            throw new IllegalStateException("GitHub not configured: set github.target.repo and github.auth.token");
        }
        try {
            GitHub client = getGitHub();
            GHRepository repository = client.getRepository(targetRepo);
            String diffUrl = repository.getPullRequest(prId).getDiffUrl().toString();
            return fetchDiffFromUrl(diffUrl);
        } catch (IOException e) {
            log.warn("Failed to get PR {} from {}: {}", prId, targetRepo, e.getMessage());
            throw new IllegalStateException("Failed to fetch PR diff: " + e.getMessage(), e);
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("PR {} not found or no access in {}: {}", prId, targetRepo, e.getMessage());
            return "";
        }
    }

    private GitHub getGitHub() throws IOException {
        if (gitHub == null) {
            synchronized (this) {
                if (gitHub == null) {
                    gitHub = new GitHubBuilder().withOAuthToken(authToken).build();
                }
            }
        }
        return gitHub;
    }

    private String fetchDiffFromUrl(String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, AUTH_HEADER_PREFIX + authToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        try {
            return restTemplate.exchange(URI.create(url), HttpMethod.GET, entity, String.class).getBody();
        } catch (HttpClientErrorException.NotFound e) {
            return "";
        }
    }
}
