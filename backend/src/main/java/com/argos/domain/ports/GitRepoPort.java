package com.argos.domain.ports;

/**
 * Port for fetching repository content (e.g. PR diff). Implemented by infrastructure; used by the application layer.
 */
public interface GitRepoPort {

    /**
     * Fetches the raw diff text for the given pull request.
     *
     * @param prId pull request number
     * @return raw diff body, or empty string if PR not found / no access
     */
    String getRawDiff(int prId);
}
