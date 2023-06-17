package com.lyricsApp.LyricsAppBackEnd.repo;

import com.lyricsApp.LyricsAppBackEnd.model.Lyrics;
import com.lyricsApp.LyricsAppBackEnd.utils.CountryName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LyricsRepo extends JpaRepository<Lyrics, Long> {
    Optional<Lyrics> findByCountryName(CountryName countryName);
}
