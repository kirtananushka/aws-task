package com.epam.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.DeletePublicAccessBlockRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.epam.config.AmazonProperties;
import com.epam.exception.PictureProcessorException;
import com.epam.repository.PictureRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class BucketService {

    private final PictureRepository pictureRepository;

    private final AmazonS3 s3client;

    private final AmazonProperties amazonProperties;

    public void createBucket() {
        if (s3client.doesBucketExistV2(amazonProperties.getBucketName())) {
            throw new PictureProcessorException("Bucket already exists");
        }
        s3client.createBucket(amazonProperties.getBucketName());
        try {
            log.info(String.format("Trying to set bucket policy: `%s`, `%s`", amazonProperties.getBucketName(),
                  amazonProperties.getBucketPolicy()));
            s3client.deletePublicAccessBlock(getDeletePublicAccessBlockRequest());
            s3client.setBucketPolicy(amazonProperties.getBucketName(), amazonProperties.getBucketPolicy());
        } catch (AmazonServiceException e) {
            log.error("Error setting bucket policy", e);
        } catch (Exception e) {
            log.error("Error while creating bucket", e);
        }
    }

    public void deleteBucket() {
        if (s3client.doesBucketExistV2(amazonProperties.getBucketName())) {
            ObjectListing objectListing = s3client.listObjects(amazonProperties.getBucketName());
            for (S3ObjectSummary os : objectListing.getObjectSummaries()) {
                String key = os.getKey();
                s3client.deleteObject(amazonProperties.getBucketName(), key);
            }
            try {
                s3client.deleteBucket(amazonProperties.getBucketName());
            } catch (AmazonServiceException e) {
                throw new PictureProcessorException("Error while bucket deleting", e);
            } catch (Exception e) {
                log.error("Error while bucket deleting", e);
            }
        }
        try {
            pictureRepository.findAll().forEach(pic -> pictureRepository.deleteById(pic.getId()));
        } catch (Exception e) {
            log.error("Error while deleting picture metadata", e);
        }
    }

    public List<String> getBucketsList() {
        List<Bucket> buckets = s3client.listBuckets();
        return buckets.stream().map(Bucket::getName).toList();
    }

    public boolean doesBucketExist() {
        return s3client.doesBucketExistV2(amazonProperties.getBucketName());
    }

    public void uploadFile(String fileName, File file) {
        PutObjectResult putObjectResult = s3client.putObject(
              new PutObjectRequest(amazonProperties.getBucketName(), fileName, file));
        putObjectResult.getETag();
    }

    public byte[] getFileContent(String fileName) {
        byte[] content;
        S3Object s3Object = s3client.getObject(amazonProperties.getBucketName(), fileName);
        S3ObjectInputStream stream = s3Object.getObjectContent();
        try {
            content = IOUtils.toByteArray(stream);
            s3Object.close();
        } catch (IOException ex) {
            throw new PictureProcessorException("Error while getting file from S3 bucket", ex);
        }
        return content;
    }

    public void deleteFile(String fileName) {
        try {
            s3client.deleteObject(new DeleteObjectRequest(amazonProperties.getBucketName(), fileName));
        } catch (SdkClientException e) {
            log.error("Error while deleting file from s3 bucket", e);
        }
        log.info("{} was deleted from the bucket", fileName);

    }

    private DeletePublicAccessBlockRequest getDeletePublicAccessBlockRequest() {
        DeletePublicAccessBlockRequest request = new DeletePublicAccessBlockRequest();
        request.setBucketName(amazonProperties.getBucketName());
        request.setExpectedBucketOwner(amazonProperties.getAccountId());
        return request;
    }
}
