package org.greenstone.backend.admin.job;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record RecordSignoffRequest(@NotBlank @Size(max=200) String customerName) {}
