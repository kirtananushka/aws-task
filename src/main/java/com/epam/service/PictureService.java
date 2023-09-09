package com.epam.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.epam.config.AmazonProperties;
import com.epam.exception.PictureProcessorException;
import com.epam.model.PictureMetadata;
import com.epam.repository.PictureRepository;
import com.epam.service.dto.PictureDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@AllArgsConstructor
public class PictureService {

   private final PictureRepository pictureRepository;

   private final MessageService messageService;

   private final BucketService bucketService;

   private final AmazonProperties amazonProperties;
   //    private final LambdaService lambdaService;

   private static final Logger logger = LoggerFactory.getLogger(PictureService.class);

   public PictureMetadata save(MultipartFile multipartFile) {
      String fileName = multipartFile.getOriginalFilename();
      validateFileDoesNotExist(fileName);
      validateBucketExists();
      String path = uploadFileToBucket(fileName, multipartFile);
      PictureMetadata metadata = createAndSaveMetadata(multipartFile, fileName, path);
      messageService.sendSqsMessage(metadata);
      return metadata;
   }

   public PictureDto getPictureByName(String fileName) {
      return toPictureDto(fileName);
   }

   public List<PictureDto> getAllPictures() {
      return pictureRepository.findAll().stream().map(pictureMetadata -> toPictureDto(pictureMetadata.getName()))
            .toList();
   }

   public PictureDto findRandom() {
      return Optional.ofNullable(pictureRepository.findRandom()).map(PictureMetadata::getName).map(this::toPictureDto)
            .orElseThrow(() -> new PictureProcessorException("There are no pictures in the bucket"));
   }

   public byte[] getPictureData(String fileName) {
      byte[] data;
      try {
         data = findByName(fileName).get();
      } catch (AmazonS3Exception | PictureProcessorException ex) {
         throw new PictureProcessorException("No such picture");
      } catch (Exception ex) {
         throw new PictureProcessorException("Error while getting picture");
      }
      return data;
   }

   @Async
   @Cacheable("pictures")
   public CompletableFuture<byte[]> findByName(String fileName) {

      if (pictureRepository.findByName(fileName).isEmpty()) {
         throw new PictureProcessorException("No such picture: " + fileName);
      }

      byte[] content = bucketService.getFileContent(fileName);
      return CompletableFuture.completedFuture(content);
   }

   public void deleteAll() {
      List<PictureMetadata> pictureMetadataList = pictureRepository.findAll();
      pictureMetadataList.forEach(pictureMetadata -> deleteByName(pictureMetadata.getName()));
   }

   public void deleteByName(String fileName) {
      PictureMetadata pictureMetadata = pictureRepository.findByName(fileName).get(0);
      bucketService.deleteFile(fileName);
      pictureRepository.deleteById(pictureMetadata.getId());
      logger.info("{} was deleted from the DB", pictureMetadata.getName());
   }

   public void deleteAllFromDb() {
      List<PictureMetadata> pictureMetadataList = pictureRepository.findAll();
      pictureMetadataList.forEach(pictureMetadata -> deleteByNameFromDb(pictureMetadata.getName()));
   }

   public String localDateTimeToString(LocalDateTime localDateTime) {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
      return localDateTime.format(formatter);
   }

   private void validateFileDoesNotExist(String fileName) {
      if (!pictureRepository.findByName(fileName).isEmpty()) {
         throw new ServiceException("File already exists");
      }
   }

   private void validateBucketExists() {
      if (!bucketService.doesBucketExist()) {
         deleteAllFromDb();
         throw new ServiceException("The bucket does not exist");
      }
   }

   private String uploadFileToBucket(String fileName, MultipartFile multipartFile) {
      try {
         File file = convertMultipartFileToFile(multipartFile);
         String path = amazonProperties.getBucketUrl() + "/" + amazonProperties.getBucketName() + "/" + fileName;
         bucketService.uploadFile(fileName, file);
         file.delete();
         return path;
      } catch (IOException ex) {
         throw new PictureProcessorException(
               String.format("Service error while uploading picture: fileName=%s. %s", fileName, ex.getMessage()));
      } catch (AmazonServiceException e) {
         throw new PictureProcessorException("Amazon service exception", e);
      }
   }

   private PictureMetadata createAndSaveMetadata(MultipartFile multipartFile, String fileName, String path) {
      PictureMetadata metadata = setMetadata(multipartFile, fileName, path);
      pictureRepository.save(metadata);
      return metadata;
   }

   private PictureMetadata setMetadata(MultipartFile multipartFile, String fileName, String path) {
      PictureMetadata metadata = new PictureMetadata();
      metadata.setName(fileName);
      metadata.setPath(path);
      String url = String.format("https://%s.s3.amazonaws.com/%s", amazonProperties.getBucketName(), fileName);
      metadata.setUrl(url);
      metadata.setSize(multipartFile.getSize());
      metadata.setExtension(getFileExtension(fileName));
      metadata.setLastUpdate(LocalDateTime.now());
      return metadata;
   }

   private PictureDto toPictureDto(String fileName) {
      byte[] data = getPictureData(fileName);
      String pictureData = Base64.getEncoder().encodeToString(data);
      PictureMetadata pictureMetadata = pictureRepository.findByName(fileName).get(0);
      return PictureDto.builder().fileName(fileName).pictureData(pictureData).path(pictureMetadata.getPath())
            .url(pictureMetadata.getUrl()).size(pictureMetadata.getSize()).extension(pictureMetadata.getExtension())
            .lastUpdate(localDateTimeToString(pictureMetadata.getLastUpdate())).build();
   }

   private void deleteByNameFromDb(String fileName) {
      PictureMetadata pictureMetadata = pictureRepository.findByName(fileName).get(0);
      pictureRepository.deleteById(pictureMetadata.getId());
      logger.info("{} was deleted from the DB", pictureMetadata.getName());
   }

   private File convertMultipartFileToFile(MultipartFile file) throws IOException {
      File convertedFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
      try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
         fos.write(file.getBytes());
      }
      return convertedFile;
   }

   private String getFileExtension(String fileName) {
      return fileName.substring(fileName.lastIndexOf("."));
   }
}
