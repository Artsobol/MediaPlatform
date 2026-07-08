package io.github.artsobol.mediaservice.feature.photo.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.artsobol.mediaservice.exception.http.NotFoundException;
import io.github.artsobol.mediaservice.feature.photo.dto.request.PhotoCreateRequest;
import io.github.artsobol.mediaservice.feature.photo.dto.request.PhotoUpdateRequest;
import io.github.artsobol.mediaservice.feature.photo.dto.response.PhotoResponse;
import io.github.artsobol.mediaservice.feature.photo.entity.PhotoStatus;
import io.github.artsobol.mediaservice.feature.photo.service.PhotoService;
import io.github.artsobol.mediaservice.infrastructure.error.file.FileValidationService;
import io.github.artsobol.mediaservice.infrastructure.error.localization.MessageService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(PhotoController.class)
class PhotoControllerTest {

  @MockitoBean private PhotoService photoService;
  @MockitoBean private MessageService messageService;
  @MockitoBean private FileValidationService fileValidationService;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("GET /{photoId}: photo exists - returns 200 and body")
  void getPhotoById_photoExists_returns200AndBody() throws Exception {
    // given
    Long photoId = 1L;
    PhotoResponse response =
        new PhotoResponse(
            photoId,
            "https://ya.ru",
            "Photo title",
            "Photo desc",
            LocalDate.of(2020, 2, 2),
            PhotoStatus.UPLOADED);
    when(photoService.getById(photoId)).thenReturn(response);

    // when + then
    mockMvc
        .perform(get("/photos/{photoId}", photoId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(response.id()))
        .andExpect(jsonPath("$.url").value(response.url()))
        .andExpect(jsonPath("$.title").value(response.title()))
        .andExpect(jsonPath("$.description").value(response.description()))
        .andExpect(jsonPath("$.photoDate").value(response.photoDate().toString()))
        .andExpect(jsonPath("$.photoStatus").value(response.photoStatus().toString()));

    verify(photoService).getById(photoId);
  }

  @Test
  @DisplayName("GET /{photoId}: photo not exists - returns 404 and body")
  void getPhotoById_photoNotExists_returns404AndBody() throws Exception {
    // given
    Long photoId = 1L;
    when(photoService.getById(photoId))
        .thenThrow(new NotFoundException("photo.not.found", photoId));

    String message = "Photo with id=" + photoId + " not found";
    when(messageService.createMessage(eq("photo.not.found"), any())).thenReturn(message);

    // when + then
    mockMvc
        .perform(get("/photos/{photoId}", photoId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.errorCode").value("photo.not.found"))
        .andExpect(jsonPath("$.message").value(message));

    verify(photoService).getById(photoId);
    verify(messageService).createMessage(any(), any());
  }

  @Test
  @DisplayName("GET: photo exists - returns 200 and Body")
  void getAllPhoto_photoExists_returns200AndBody() throws Exception {
    // given
    List<PhotoResponse> response =
        List.of(
            new PhotoResponse(
                1L,
                "https://ya.ru",
                "Photo title 1",
                "Photo desc 1",
                LocalDate.of(2020, 2, 2),
                PhotoStatus.UPLOADED),
            new PhotoResponse(
                2L,
                "https://ya.ru",
                "Photo title 2",
                "Photo desc 2",
                LocalDate.of(2020, 2, 2),
                PhotoStatus.UPLOADED));
    when(photoService.getAll()).thenReturn(response);

    // when + then
    mockMvc
        .perform(get("/photos"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(response.getFirst().id()))
        .andExpect(jsonPath("$[0].url").value(response.getFirst().url()))
        .andExpect(jsonPath("$[0].title").value(response.getFirst().title()))
        .andExpect(jsonPath("$[0].description").value(response.getFirst().description()))
        .andExpect(jsonPath("$[0].photoDate").value(response.getFirst().photoDate().toString()))
        .andExpect(jsonPath("$[0].photoStatus").value(response.getFirst().photoStatus().toString()))
        .andExpect(jsonPath("$[1].id").value(response.getLast().id()))
        .andExpect(jsonPath("$[1].url").value(response.getLast().url()))
        .andExpect(jsonPath("$[1].title").value(response.getLast().title()))
        .andExpect(jsonPath("$[1].description").value(response.getLast().description()))
        .andExpect(jsonPath("$[1].photoDate").value(response.getLast().photoDate().toString()))
        .andExpect(jsonPath("$[1].photoStatus").value(response.getLast().photoStatus().toString()));

    verify(photoService).getAll();
  }

  @Test
  @DisplayName("POST: valid request - returns 201 and body")
  void createPhoto_validRequest_returns201AndBody() throws Exception {
    // given
    Long photoId = 1L;
    PhotoResponse response =
        new PhotoResponse(
            photoId,
            "https://ya.ru",
            "Photo title",
            "Photo desc",
            LocalDate.of(2020, 2, 2),
            PhotoStatus.UPLOADED);
    PhotoCreateRequest request =
        new PhotoCreateRequest(response.title(), response.description(), response.photoDate());
    when(photoService.create(eq(request), any(MultipartFile.class))).thenReturn(response);
    MockMultipartFile metadata =
        new MockMultipartFile(
            "metadata",
            "",
            MediaType.APPLICATION_JSON_VALUE,
            objectMapper.writeValueAsBytes(request));
    MockMultipartFile file =
        new MockMultipartFile(
            "file", "photo.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[] {1, 2, 3});

    // when + then
    mockMvc
        .perform(multipart("/photos").file(metadata).file(file))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(response.id()))
        .andExpect(jsonPath("$.url").value(response.url()))
        .andExpect(jsonPath("$.title").value(response.title()))
        .andExpect(jsonPath("$.description").value(response.description()))
        .andExpect(jsonPath("$.photoDate").value(response.photoDate().toString()))
        .andExpect(jsonPath("$.photoStatus").value(response.photoStatus().toString()));

    verify(photoService).create(eq(request), any(MultipartFile.class));
  }

  @Test
  @DisplayName("POST: invalid request - returns 400 and body")
  void createPhoto_invalidRequest_returns400AndBody() throws Exception {
    // given
    PhotoCreateRequest request = new PhotoCreateRequest("", "", LocalDate.of(2020, 2, 2));
    when(messageService.resolveValidationMessage(any())).thenReturn("Title must not be blank");
    when(messageService.createMessage(eq("validation.error"), any()))
        .thenReturn("Validation error");
    MockMultipartFile metadata =
        new MockMultipartFile(
            "metadata",
            "",
            MediaType.APPLICATION_JSON_VALUE,
            objectMapper.writeValueAsBytes(request));
    MockMultipartFile file =
        new MockMultipartFile(
            "file", "photo.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[] {1, 2, 3});

    // when + then
    mockMvc
        .perform(multipart("/photos").file(metadata).file(file))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.message").value("Validation error"))
        .andExpect(jsonPath("$.path").value("/photos"))
        .andExpect(jsonPath("$.errors[0].field").value("title"))
        .andExpect(jsonPath("$.errors[0].message").value("Title must not be blank"));

    verifyNoInteractions(photoService);
  }

  @Test
  @DisplayName("PATCH /{photoId}: photo exists - returns 200")
  void partiallyUpdate_validRequest_returns200AndBody() throws Exception {
    // given
    Long photoId = 1L;
    PhotoResponse response =
        new PhotoResponse(
            photoId,
            "https://ya.ru",
            "New title",
            "Photo desc",
            LocalDate.of(2020, 2, 2),
            PhotoStatus.UPLOADED);
    PhotoUpdateRequest request = new PhotoUpdateRequest("New title", null, null);
    when(photoService.updateBody(photoId, request)).thenReturn(response);

    // when + then
    mockMvc
        .perform(
            patch("/photos/{photoId}", photoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(response.id()))
        .andExpect(jsonPath("$.url").value(response.url()))
        .andExpect(jsonPath("$.title").value(response.title()))
        .andExpect(jsonPath("$.description").value(response.description()))
        .andExpect(jsonPath("$.photoDate").value(response.photoDate().toString()))
        .andExpect(jsonPath("$.photoStatus").value(response.photoStatus().toString()));

    verify(photoService).updateBody(photoId, request);
  }

  @Test
  @DisplayName("DELETE /{photoId}: photo exists - returns 204")
  void deletePhoto_photoExists_returns204() throws Exception {
    // give
    Long photoId = 1L;

    // when + then
    mockMvc.perform(delete("/photos/{photoId}", photoId)).andExpect(status().isNoContent());

    verify(photoService).delete(photoId);
  }
}
