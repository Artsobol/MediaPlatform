package io.github.artsobol.mediaservice.feature.photo.repository;

import io.github.artsobol.mediaservice.feature.photo.entity.Photo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PhotoRepository extends JpaRepository<Photo, Long> {

  @Query(
      """
                select p
                    from Photo p
                        where p.id = :photoId
                            and p.deletedAt IS NULL
            """)
  Optional<Photo> findActiveById(@Param("photoId") Long photoId);

  @Query(
      """
                    select p
                    from Photo p
                    where p.deletedAt IS NULL
            """)
  List<Photo> findAllActive();
}
