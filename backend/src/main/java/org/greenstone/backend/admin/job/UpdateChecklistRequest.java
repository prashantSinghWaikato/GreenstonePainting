package org.greenstone.backend.admin.job;
import jakarta.validation.constraints.NotNull;
public record UpdateChecklistRequest(@NotNull Boolean completed) {}
