package com.streamapp.stream_app_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.streamapp.stream_app_backend.entity.Video;


public interface VideoRepository extends JpaRepository<Video, String> {

    Optional<Video> findByTitle(String title);
    
}
