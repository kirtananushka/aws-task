package com.epam.repository;

import com.epam.model.PictureMetadata;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface PictureRepository extends JpaRepository<PictureMetadata, Long>, CrudRepository<PictureMetadata, Long> {

    PictureMetadata save(PictureMetadata pictureMetadata);

    List<PictureMetadata> findAll();

    List<PictureMetadata> findByName(String name);

    void deleteById(Long id);

    @Query(value = "SELECT * FROM pictures ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    PictureMetadata findRandom();
}
