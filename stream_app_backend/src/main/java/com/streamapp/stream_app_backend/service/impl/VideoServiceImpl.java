package com.streamapp.stream_app_backend.service.impl;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.streamapp.stream_app_backend.entity.Video;
import com.streamapp.stream_app_backend.repository.VideoRepository;
import com.streamapp.stream_app_backend.service.interfac.IVideoService;

import jakarta.annotation.PostConstruct;

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
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }
        try {
            Files.createDirectories(Paths.get(hslDir));
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    @Override
    public Video save(Video video, MultipartFile file) {
        try {
            String filename = file.getOriginalFilename();
            String contentType = file.getContentType();
            InputStream inputStream = file.getInputStream();

            String cleanFileName = StringUtils.cleanPath(filename);
            String cleanFolder = StringUtils.cleanPath(dir);
            Path path = Paths.get(cleanFolder, cleanFileName);

            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);

            video.setContentType(contentType);
            video.setFilePath(path.toString());
            Video savedVideo = videoRepository.save(video);
            processVideo(savedVideo.getId());
            return savedVideo;

        } catch (Exception e) {
            throw new RuntimeException("Error while saving file: " + e.getMessage());
        }
    }

    @Override
    public Video get(String videoId) {
        return videoRepository.findById(videoId).orElseThrow(() -> new RuntimeException("Video not found"));
    }

    @Override
    public Video getByTitle(String title) {
        return null;
    }

    @Override
    public List<Video> getAll() {
        return videoRepository.findAll();
    }

    @Override
    public String processVideo(String videoId) {
        try {
            Video video = this.get(videoId);
            String filePath = video.getFilePath();
            Path videoPath = Paths.get(filePath);
            Path outputPath = Paths.get(hslDir, videoId);
            Files.createDirectory(outputPath);
            String ffmpegCmd = "ffmpeg -i " + videoPath.toString()
                    + " -c:v libx264 -b:v 3000k -maxrate 3000k -bufsize 6000k -vf \"format=yuv420p\" -c:a aac -b:a 128k -f  hls -hls_time 6 -hls_playlist_type vod -hls_segment_filename \""
                    + outputPath.toString() + "/segment_%03d.ts\" " + outputPath.toString() + "/master.m3u8";
            System.out.println(ffmpegCmd);
           ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", ffmpegCmd);
           Process process = processBuilder.start();
           int exit = process.waitFor();
           if(exit != 0){
            throw new RuntimeException("Error while processing video: " + exit);
           }
           return videoId;
        } catch (Exception e) {
            throw new RuntimeException("Error while processing video: " + e.getMessage());
        }
    }
}
