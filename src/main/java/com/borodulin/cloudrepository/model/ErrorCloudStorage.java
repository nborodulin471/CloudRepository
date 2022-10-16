package com.borodulin.cloudrepository.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ErrorCloudStorage {
    private final String message;
    private final String id;
}
