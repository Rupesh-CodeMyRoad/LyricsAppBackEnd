package com.lyricsApp.LyricsAppBackEnd.dto;

import com.lyricsApp.LyricsAppBackEnd.utils.CountryName;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class LyricsDTO{
    private CountryName countryName;
    private String anthemLyrics;
    private MultipartFile anthemAudio;
    private MultipartFile flag;
}
