package com.argos.domain.model;

/**
 * Value object representing a user command to ARGOS-1.
 * No identity; immutable.
 */
public record ArgosCommand(String input) {
}
