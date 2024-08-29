package com.streamapp.stream_app_backend.controller;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.streamapp.stream_app_backend.entity.Video;
import com.streamapp.stream_app_backend.service.impl.VideoServiceImpl;

import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/v1/videos")
public class VideoController {

    @Value("${hsl_video.file.directory}")
    String hslDir;

    private VideoServiceImpl videoService;

    public VideoController(VideoServiceImpl videoService) {
        this.videoService = videoService;
    }

    // method to upload video
    @PostMapping
    public ResponseEntity<Video> uploadVideo(@RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("description") String description) {
        try {

            // Save the video to database
            Video video = new Video();
            video.setTitle(title);
            video.setDescription(description);
            video.setId(UUID.randomUUID().toString());

            Video saveVideo = videoService.save(video, file);
            if (saveVideo != null) {
                return ResponseEntity.status(200).body(saveVideo);
            } else {
                return ResponseEntity.status(500).body(null);
            }
        } catch (Exception e) {
            System.out.println("Exception while saving video: " + e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    // method to get all videos
    @GetMapping
    public List<Video> getAllVideo() {
        return videoService.getAll();
    }

    // method to stream video
    @GetMapping("/stream/{videoId}")
    public ResponseEntity<Resource> streamVideo(@PathVariable String videoId) {
        Video video = videoService.get(videoId);
        String contentType = video.getContentType();
        String filePath = video.getFilePath();
        Resource resource = new FileSystemResource(filePath);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.status(200).contentType(MediaType.parseMediaType(contentType)).body(resource);
    }

    // method to stream video of range
    @GetMapping("/stream/range/{videoId}")
    public ResponseEntity<Resource> streamRange(@PathVariable String videoId, @RequestHeader String range) {
        try {
            Video video = videoService.get(videoId);
            String contentType = video.getContentType();
            String filePath = video.getFilePath();
            Resource resource = new FileSystemResource(filePath);

            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            if (range != null) {
                String[] rangeParts = range.split("=")[1].split("-");
                long start = Long.parseLong(rangeParts[0]);
                long end = rangeParts.length > 1 ? Long.parseLong(rangeParts[1]) : resource.contentLength() - 1;
                long contentLength = end - start + 1;
                InputStream inputStream = resource.getInputStream();
                inputStream.skip(start);

                InputStreamResource data = new InputStreamResource(inputStream);

                return ResponseEntity.status(206).contentType(MediaType.parseMediaType(contentType))
                        .header("Content-Range", "bytes " + start + "-" + end + "/" + resource.contentLength())
                        .header("Content-Length", String.valueOf(contentLength)).body(data);
            } else {
                return ResponseEntity.status(200).contentType(MediaType.parseMediaType(contentType))
                        .header("Content-Range", "bytes 0-1000000/1000000").body(resource);
            }
        } catch (Exception e) {
            System.out.println("Error while streaming video: " + e.getMessage());
            return ResponseEntity.status(500).body(null);

        }
    }

    // method to send hls playlist

    @GetMapping("/{videoId}/master.m3u8")
    public ResponseEntity<Resource> streamMasterM3u8(@PathVariable String videoId) {
        try {
            Path path = Paths.get(hslDir, videoId, "master.m3u8");
            Resource resource = new FileSystemResource(path);
            if (Files.exists(path)) {
                return ResponseEntity.ok().body(resource);
            } else {
                return ResponseEntity.status(404).body(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(500).body(null);
    }

    // server the segments
    @GetMapping("/{videoId}/{segment}.ts")
    public ResponseEntity<Resource> streamSegment(@PathVariable String videoId, @PathVariable String segment) {
        try {
            Path path = Paths.get(hslDir, videoId, segment + ".ts");
            Resource resource = new FileSystemResource(path);
            if (Files.exists(path)) {
                return ResponseEntity.ok().body(resource);
            } else {
                return ResponseEntity.status(404).body(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(500).body(null);
    }

}