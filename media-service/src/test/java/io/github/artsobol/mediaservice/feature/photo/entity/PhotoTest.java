package io.github.artsobol.mediaservice.feature.photo.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class PhotoTest {

  @Test
  void createAndUpdateBody_validValues_updatesPhoto() {
    Photo photo = Photo.create();
    LocalDate photoDate = LocalDate.of(2024, 6, 15);

    photo.updateBody("Title", "Description", photoDate);
    photo.updateOriginalImageKey("photos/1/original/photo.jpg");

    assertThat(photo.getPhotoStatus()).isEqualTo(PhotoStatus.PENDING_UPLOAD);
    assertThat(photo.getTitle()).isEqualTo("Title");
    assertThat(photo.getDescription()).isEqualTo("Description");
    assertThat(photo.getPhotoDate()).isEqualTo(photoDate);
    assertThat(photo.getOriginalImageKey()).isEqualTo("photos/1/original/photo.jpg");
  }

  @Test
  void updateBody_nullAndBlankValues_keepsCurrentValues() {
    Photo photo = Photo.create();
    LocalDate photoDate = LocalDate.of(2024, 6, 15);
    photo.updateBody("Title", "Description", photoDate);

    photo.updateBody(" ", null, null);

    assertThat(photo.getTitle()).isEqualTo("Title");
    assertThat(photo.getDescription()).isEqualTo("Description");
    assertThat(photo.getPhotoDate()).isEqualTo(photoDate);
  }

  @Test
  void updateMethods_invalidValues_throwIllegalArgumentException() {
    Photo photo = Photo.create();

    assertThatThrownBy(() -> photo.updateTitle(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("title must not be blank");
    assertThatThrownBy(() -> photo.updateTitle(" "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("title must not be blank");
    assertThatThrownBy(() -> photo.updateDescription(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("description must not be blank");
    assertThatThrownBy(() -> photo.updateDescription(" "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("description must not be blank");
    assertThatThrownBy(() -> photo.updatePhotoDate(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("photoDate must not be null");
    assertThatThrownBy(() -> photo.updateOriginalImageKey(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("originalImageKey must not be blank");
    assertThatThrownBy(() -> photo.updateOriginalImageKey(" "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("originalImageKey must not be blank");
  }

  @Test
  void uploadLifecycle_validTransitions_reachesReadyStatus() {
    Photo photo = Photo.create();

    photo.successUpload();
    assertThat(photo.getPhotoStatus()).isEqualTo(PhotoStatus.UPLOADED);

    photo.processUpload();
    assertThat(photo.getPhotoStatus()).isEqualTo(PhotoStatus.PROCESSING);

    photo.readyUpload();
    assertThat(photo.getPhotoStatus()).isEqualTo(PhotoStatus.READY);
  }

  @Test
  void uploadLifecycle_invalidTransitions_throwIllegalStateException() {
    Photo pendingPhoto = Photo.create();

    assertThatThrownBy(pendingPhoto::processUpload)
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Photo processing can start only from UPLOADED status");
    assertThatThrownBy(pendingPhoto::readyUpload)
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Photo can be marked as ready only from PROCESSING status");

    pendingPhoto.successUpload();

    assertThatThrownBy(pendingPhoto::successUpload)
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Photo can be uploaded only from PENDING_UPLOAD status");
  }

  @Test
  void failUpload_setsFailedStatus() {
    Photo photo = Photo.create();

    photo.failUpload();

    assertThat(photo.getPhotoStatus()).isEqualTo(PhotoStatus.FAILED);
  }

  @Test
  void delete_alreadyDeletedPhoto_throwsIllegalStateException() {
    Photo photo = Photo.create();
    photo.delete();

    assertThatThrownBy(photo::delete)
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("entity already is deleted");
  }
}
