package com.lyricsApp.LyricsAppBackEnd.service;

import com.lyricsApp.LyricsAppBackEnd.dto.LyricsDTO;
import com.lyricsApp.LyricsAppBackEnd.model.Lyrics;
import com.lyricsApp.LyricsAppBackEnd.repo.LyricsRepo;
import com.lyricsApp.LyricsAppBackEnd.utils.CountryName;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class LyricsService {
    private final LyricsRepo lyricsRepository;

    @Value("${env.host.port}")
    private String envPort;

    @Value("${env.host.name}")
    private String envHost;


    public LyricsService(LyricsRepo lyricsRepository) {
        this.lyricsRepository = lyricsRepository;
    }

    public ResponseEntity<Lyrics> saveLyrics(LyricsDTO lyricsDto) {
        // Check if a Lyrics object with the same countryName already exists
        if (lyricsRepository.findByCountryName(lyricsDto.getCountryName()).isPresent()) {
            // If it exists, return a CONFLICT (409) status code
            throw new RuntimeException("Sorry Country Lyrics is already present. Please Update the existing one");
        }

        //upload audio file

        MultipartFile audioFile = lyricsDto.getAnthemAudio();
        String audioFileName = StringUtils.cleanPath(audioFile.getOriginalFilename()); // spring utility method to clean the path

        // Use File.separator to ensure the correct file separator is used
        String audioDirectory = "audioUploads" + File.separator;
        Path audioDirPath = Paths.get(audioDirectory);

        // Check if the directory exists, if not, create it
        if (!Files.exists(audioDirPath)) {
            try {
                Files.createDirectories(audioDirPath);
            } catch (IOException e) {
                // Handle the situation when the directory cannot be created.
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }

        Path audioPath = audioDirPath.resolve(audioFileName); // this will get the path of file in uploads directory

        try (InputStream inputStream = audioFile.getInputStream()) {
            Files.copy(inputStream, audioPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            // Add proper error handling here.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        //upload flag
        MultipartFile flagFile = lyricsDto.getFlag();
        String flagFileName = StringUtils.cleanPath(flagFile.getOriginalFilename()); // spring utility method to clean the path

        // Use File.separator to ensure the correct file separator is used
        String flagDirectory = "flagUploads" + File.separator;
        Path flagDirPath = Paths.get(flagDirectory);

        // Check if the directory exists, if not, create it
        if (!Files.exists(flagDirPath)) {
            try {
                Files.createDirectories(flagDirPath);
            } catch (IOException e) {
                // Handle the situation when the directory cannot be created.
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }

        Path flagPath = flagDirPath.resolve(flagFileName); // this will get the path of file in uploads directory

        try (InputStream inputStream = flagFile.getInputStream()) {
            Files.copy(inputStream, flagPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            // Add proper error handling here.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }


        Lyrics lyrics = new Lyrics();
        lyrics.setCountryName(lyricsDto.getCountryName());
        lyrics.setAnthemLyrics(lyricsDto.getAnthemLyrics());
        lyrics.setAnthemAudioLink(audioPath.toString());
        lyrics.setFlagLink(flagPath.toString());

        Lyrics savedLyrics = lyricsRepository.save(lyrics);

        if (savedLyrics != null) {
            return ResponseEntity.ok(savedLyrics);
        } else {
            // Something went wrong while saving the lyrics
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public List<Lyrics> getAllLyrics() {
        return lyricsRepository.findAll();
    }

    public Lyrics getLyricsByCountryName(CountryName countryName) {
        return lyricsRepository.findByCountryName(countryName)
                .orElseThrow(() -> new RuntimeException(
                        "Error: Lyrics for the country: " + countryName + " is not found."
                ));
    }

    public ResponseEntity<Lyrics> updateLyrics(LyricsDTO lyricsDto) {
        // Find the existing Lyrics
        Lyrics existingLyrics = lyricsRepository.findByCountryName(lyricsDto.getCountryName())
                .orElseThrow(() -> new RuntimeException(
                        "Error: Lyrics for the country: " + lyricsDto.getCountryName() + " is not found."
                ));

        // Save the new file and delete the old one
        MultipartFile file = lyricsDto.getAnthemAudio();
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        String directory = "uploads" + File.separator;
        Path dirPath = Paths.get(directory);

        if (!Files.exists(dirPath)) {
            try {
                Files.createDirectories(dirPath);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }

        // Delete the old file
        Path oldFilePath = Paths.get(existingLyrics.getAnthemAudioLink());
        try {
            Files.deleteIfExists(oldFilePath);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        // Save the new file
        Path newFilePath = dirPath.resolve(fileName);
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, newFilePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        // Update the Lyrics object and save it
        existingLyrics.setAnthemLyrics(lyricsDto.getAnthemLyrics());
        existingLyrics.setAnthemAudioLink(newFilePath.toString());
        Lyrics updatedLyrics = lyricsRepository.save(existingLyrics);

        if (updatedLyrics != null) {
            return ResponseEntity.ok(updatedLyrics);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public void deleteLyrics(Long id) {
        lyricsRepository.deleteById(id);
    }

    public Map<String, Object> getDashboardInfo() {
        int count = lyricsRepository.countLyrics();
        Map<String, Object> data = new HashMap<>();
        data.put("count", count);
        return data;
    }

    public List<Map<String, String>> getAllCountryList() {
        List<Map<String, String>> data = new ArrayList<>();
        List<Lyrics> lyricsData = lyricsRepository.findAll();
        for (Lyrics lyric : lyricsData) {
            String flag = "http://" + envHost + ":" + envPort + "/api/lyrics/flag/" + lyric.getCountryName();
            Map<String, String> mapData = new HashMap<>();
            mapData.put("name", lyric.getCountryName().toString());
            mapData.put("flag", flag);
            data.add(mapData);
        }
        return data;
    }
}
