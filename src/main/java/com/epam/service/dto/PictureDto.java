package com.epam.service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PictureDto {

    private String fileName;

    private String pictureData;

    private String path;

    private String url;

    private Long size;

    private String extension;

    private String lastUpdate;
}
