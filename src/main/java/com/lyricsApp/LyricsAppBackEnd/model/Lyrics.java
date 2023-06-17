package com.lyricsApp.LyricsAppBackEnd.model;

import com.lyricsApp.LyricsAppBackEnd.utils.CountryName;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "lyrics")
public class Lyrics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private CountryName countryName;
    private String anthemLyrics;
    private String anthemAudioLink;
    private String flagLink;
}
