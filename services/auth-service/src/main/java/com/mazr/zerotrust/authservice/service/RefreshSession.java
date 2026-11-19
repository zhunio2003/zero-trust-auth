package com.mazr.zerotrust.authservice.service;

import java.util.UUID;

public record RefreshSession(
    UUID userId,
    UUID familyId
) {}
