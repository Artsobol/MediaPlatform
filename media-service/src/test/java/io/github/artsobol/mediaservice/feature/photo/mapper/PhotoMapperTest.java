package io.github.artsobol.mediaservice.feature.photo.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.artsobol.mediaservice.feature.photo.dto.response.PhotoResponse;
import io.github.artsobol.mediaservice.feature.photo.entity.Photo;
import io.github.artsobol.mediaservice.feature.photo.entity.PhotoStatus;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class PhotoMapperTest {

  private final PhotoMapper mapper = new PhotoMapperImpl();

  @Test
  void toResponse_photoAndUrl_mapsAllResponseFields() {
    Photo photo = Photo.create();
    LocalDate photoDate = LocalDate.of(2024, 6, 15);
    photo.updateBody("Title", "Description", photoDate);
    ReflectionTestUtils.setField(photo, "id", 42L);

    PhotoResponse response = mapper.toResponse(photo, "https://storage.example/photo.jpg");

    assertThat(response)
        .isEqualTo(
            new PhotoResponse(
                42L,
                "https://storage.example/photo.jpg",
                "Title",
                "Description",
                photoDate,
                PhotoStatus.PENDING_UPLOAD));
  }

  @Test
  void toResponse_nullPhotoAndUrl_returnsNull() {
    assertThat(mapper.toResponse(null, null)).isNull();
  }

  @Test
  void toResponse_nullPhoto_mapsUrlAndLeavesPhotoFieldsNull() {
    PhotoResponse response = mapper.toResponse(null, "https://storage.example/photo.jpg");

    assertThat(response)
        .isEqualTo(
            new PhotoResponse(null, "https://storage.example/photo.jpg", null, null, null, null));
  }
}
