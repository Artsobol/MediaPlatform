package io.github.artsobol.mediaservice.feature.photo.repository;

import io.github.artsobol.mediaservice.feature.photo.entity.Photo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhotoRepository extends JpaRepository<Photo, Long> {
}
