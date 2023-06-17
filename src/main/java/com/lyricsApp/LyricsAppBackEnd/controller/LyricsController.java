package com.lyricsApp.LyricsAppBackEnd.controller;

import com.lyricsApp.LyricsAppBackEnd.dto.LyricsDTO;
import com.lyricsApp.LyricsAppBackEnd.model.Lyrics;
import com.lyricsApp.LyricsAppBackEnd.service.LyricsService;
import com.lyricsApp.LyricsAppBackEnd.utils.CountryName;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/lyrics")
@AllArgsConstructor
public class LyricsController {
    private final LyricsService lyricsService;

    @PostMapping("/add")
    public ResponseEntity<Lyrics> addLyrics(@ModelAttribute LyricsDTO lyricsDto) {
        return lyricsService.saveLyrics(lyricsDto);
    }

    @GetMapping
    public List<Lyrics> getAllLyrics() {
        return lyricsService.getAllLyrics();
    }

    @GetMapping("/{countryName}")
    public Lyrics getLyricsByCountryName(@PathVariable CountryName countryName) {
        return lyricsService.getLyricsByCountryName(countryName);
    }

    @PutMapping("/update")
    public ResponseEntity<Lyrics> updateLyrics(@ModelAttribute LyricsDTO lyricsDto) {
        return lyricsService.updateLyrics(lyricsDto);
    }

    @DeleteMapping("/delete/{id}")
    public String deleteLyrics(@PathVariable Long id) {
        lyricsService.deleteLyrics(id);
        return "Lyrics removed !! " + id;
    }

    @GetMapping("/mp3/{country}")
    public ResponseEntity<Resource> getMP3ByCountryName(@PathVariable("country") CountryName countryName) {
        Lyrics lyrics = lyricsService.getLyricsByCountryName(countryName);
        Path path = Paths.get(lyrics.getAnthemAudioLink());

        Resource resource;
        try {
            resource = (Resource) new UrlResource(path.toUri());
        } catch (Exception e) {
            // handle the error here.
            return ResponseEntity.notFound().build();
        }

        if (resource.exists() || resource.isReadable()) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + resource.getFilename())
                    .body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
