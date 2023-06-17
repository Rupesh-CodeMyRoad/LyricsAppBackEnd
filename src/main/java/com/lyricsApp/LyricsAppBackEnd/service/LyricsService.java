package com.lyricsApp.LyricsAppBackEnd.service;

import com.lyricsApp.LyricsAppBackEnd.dto.LyricsDTO;
import com.lyricsApp.LyricsAppBackEnd.model.Lyrics;
import com.lyricsApp.LyricsAppBackEnd.repo.LyricsRepo;
import com.lyricsApp.LyricsAppBackEnd.utils.CountryName;
import lombok.AllArgsConstructor;
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
import java.util.List;

@Service
@AllArgsConstructor
@Transactional
public class LyricsService {
    private final LyricsRepo lyricsRepository;


    public ResponseEntity<Lyrics> saveLyrics(LyricsDTO lyricsDto) {
        // Check if a Lyrics object with the same countryName already exists
        if (lyricsRepository.findByCountryName(lyricsDto.getCountryName()).isPresent()) {
            // If it exists, return a CONFLICT (409) status code
            throw new RuntimeException("Sorry Country Lyrics is already present. Please Update the existing one");
        }

        MultipartFile file = lyricsDto.getAnthemAudio();
        String fileName = StringUtils.cleanPath(file.getOriginalFilename()); // spring utility method to clean the path

        // Use File.separator to ensure the correct file separator is used
        String directory = "uploads" + File.separator;
        Path dirPath = Paths.get(directory);

        // Check if the directory exists, if not, create it
        if (!Files.exists(dirPath)) {
            try {
                Files.createDirectories(dirPath);
            } catch (IOException e) {
                // Handle the situation when the directory cannot be created.
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }

        Path path = dirPath.resolve(fileName); // this will get the path of file in uploads directory

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            // Add proper error handling here.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        Lyrics lyrics = new Lyrics();
        lyrics.setCountryName(lyricsDto.getCountryName());
        lyrics.setAnthemLyrics(lyricsDto.getAnthemLyrics());
        lyrics.setAnthemAudioLink(path.toString());

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
}
