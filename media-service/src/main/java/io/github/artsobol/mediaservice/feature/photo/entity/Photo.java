package io.github.artsobol.mediaservice.feature.photo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "photos")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Photo {

    @Id
    @Getter
    @SequenceGenerator(name = "photo_id_seq", sequenceName = "photo_id_seq", allocationSize = 50)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "photo_id_seq")
    private Long id;

    @Getter
    @Column(name = "original_image_key", unique = true)
    private String originalImageKey;

    @Getter
    @Column(name = "title", length = 100)
    private String title;

    @Getter
    @Column(name = "description", length = 1000)
    private String description;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "photo_status", nullable = false)
    private PhotoStatus photoStatus;

    @Getter
    @Column(name = "photo_date")
    private LocalDate photoDate;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static Photo create() {
        Photo entity = new Photo();
        entity.photoStatus = PhotoStatus.PENDING_UPLOAD;

        return entity;
    }

    public void updateBody(String title, String description, LocalDate photoDate) {
        if (title != null && !title.isBlank()) {
            updateTitle(title);
        }
        if (description != null && !description.isBlank()) {
            updateDescription(description);
        }
        if (photoDate != null) {
            updatePhotoDate(photoDate);
        }
    }

    public void updateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        this.title = title;
    }

    public void updateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("description must not be blank");
        }
        this.description = description;
    }

    public void updatePhotoDate(LocalDate photoDate) {
        if (photoDate == null) {
            throw new IllegalArgumentException("photoDate must not be null");
        }
        this.photoDate = photoDate;
    }

    public void updateOriginalImageKey(String originalImageKey) {
        if (originalImageKey == null || originalImageKey.isBlank()) {
            throw new IllegalArgumentException("originalImageKey must not be blank");
        }
        this.originalImageKey = originalImageKey;
    }

    public void failUpload() {
        this.photoStatus = PhotoStatus.FAILED;
    }

    public void successUpload() {
        requireStatus(PhotoStatus.PENDING_UPLOAD, "Photo can be uploaded only from PENDING_UPLOAD status");
        this.photoStatus = PhotoStatus.UPLOADED;
    }

    public void processUpload() {
        requireStatus(PhotoStatus.UPLOADED, "Photo processing can start only from UPLOADED status");
        this.photoStatus = PhotoStatus.PROCESSING;
    }

    public void readyUpload() {
        requireStatus(PhotoStatus.PROCESSING, "Photo can be marked as ready only from PROCESSING status");
        this.photoStatus = PhotoStatus.READY;
    }

    private void requireStatus(PhotoStatus expectedStatus, String message) {
        if (this.photoStatus != expectedStatus) {
            throw new IllegalStateException(message);
        }
    }
}
