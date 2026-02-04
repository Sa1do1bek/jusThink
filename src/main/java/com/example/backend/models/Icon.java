package com.example.backend.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Blob;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Icon {
    @Id
    @GeneratedValue
    private UUID id;
    private String fileName;
    private String fileType;

    @Lob
    private Blob icon;
    private String downloadUrl;

    @OneToOne(mappedBy = "icon")
    private Player player;
}
