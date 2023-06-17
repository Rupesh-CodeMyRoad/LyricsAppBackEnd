package com.lyricsApp.LyricsAppBackEnd.repo;

import com.lyricsApp.LyricsAppBackEnd.model.Lyrics;
import com.lyricsApp.LyricsAppBackEnd.utils.CountryName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface LyricsRepo extends JpaRepository<Lyrics, Long> {
    Optional<Lyrics> findByCountryName(CountryName countryName);

    @Query(value = "select count(id) from lyrics;", nativeQuery = true)
    int countLyrics();
}
