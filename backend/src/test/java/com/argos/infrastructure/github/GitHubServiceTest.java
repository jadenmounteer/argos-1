package com.argos.infrastructure.github;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GitHubServiceTest {

    private static final String TARGET_REPO = "owner/repo";
    private static final String AUTH_TOKEN = "test-token";
    private static final String DIFF_URL = "https://github.com/owner/repo/pull/1.diff";

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private GitHub gitHub;
    @Mock
    private GHRepository repository;
    @Mock
    private GHPullRequest pullRequest;

    private GitHubService service;

    @Test
    void getRawDiff_throwsWhenRepoBlank() {
        service = new GitHubService("", AUTH_TOKEN, restTemplate, gitHub);
        assertThatThrownBy(() -> service.getRawDiff(1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("GitHub not configured");
    }

    @Test
    void getRawDiff_throwsWhenTokenBlank() {
        service = new GitHubService(TARGET_REPO, "", restTemplate, gitHub);
        assertThatThrownBy(() -> service.getRawDiff(1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("GitHub not configured");
    }

    @Test
    void getRawDiff_returnsDiffBodyWhenConfigured() throws Exception {
        when(gitHub.getRepository(TARGET_REPO)).thenReturn(repository);
        when(repository.getPullRequest(1)).thenReturn(pullRequest);
        when(pullRequest.getDiffUrl()).thenReturn(new URL(DIFF_URL));
        when(restTemplate.exchange(
                any(URI.class),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class))).thenReturn(ResponseEntity.ok("diff line 1\ndiff line 2"));

        service = new GitHubService(TARGET_REPO, AUTH_TOKEN, restTemplate, gitHub);
        String result = service.getRawDiff(1);
        assertThat(result).isEqualTo("diff line 1\ndiff line 2");
        verify(restTemplate).exchange(
                any(URI.class),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class));
    }

    @Test
    void getRawDiff_usesCorrectAuthHeader() throws Exception {
        when(gitHub.getRepository(TARGET_REPO)).thenReturn(repository);
        when(repository.getPullRequest(1)).thenReturn(pullRequest);
        when(pullRequest.getDiffUrl()).thenReturn(new URL(DIFF_URL));
        when(restTemplate.exchange(
                any(URI.class),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class))).thenReturn(ResponseEntity.ok("diff content"));

        service = new GitHubService(TARGET_REPO, AUTH_TOKEN, restTemplate, gitHub);
        service.getRawDiff(1);
        verify(restTemplate).exchange(
                any(URI.class),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class));
    }
}
