package service.impl;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.streamapp.stream_app_backend.repository.VideoRepository;

import jakarta.annotation.PostConstruct;
import service.interfac.IVideoService;

@Service
public class VideoServiceImpl implements IVideoService {
   @Value("${video.file.directory}")
    String dir;

    @Value("${hsl_video.file.directory}")
    String hslDir;

    private VideoRepository videoRepository;

    public VideoServiceImpl(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    @PostConstruct
    public void init() {
        File
        
    }
}
