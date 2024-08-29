package com.streamapp.stream_app_backend.service.interfac;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.streamapp.stream_app_backend.entity.Video;

public interface IVideoService {
    Video save(Video video, MultipartFile file);
    
    Video get(String videoId);

    Video getByTitle(String title);

    List<Video> getAll();

    String processVideo(String videoId);
}