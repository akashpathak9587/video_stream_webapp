package com.streamapp.stream_app_backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Data
@Table(name = "videos_tbl")
public class Video {
    @Id
    private String id;

    @NotNull(message = "Title cannot be null")
    private String title;

    private String description;

    @NotNull(message = "Content type cannot be null")
    private String contentType;

    @NotNull(message = "File path cannot be null")
    private String filePath;
}
