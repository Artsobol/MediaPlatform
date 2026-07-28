package io.github.artsobol.mediaservice.infrastructure.error.localization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Locale;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.FieldError;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

  private static final Locale LOCALE = Locale.ENGLISH;

  @Mock private MessageSource messageSource;

  private MessageService messageService;

  @BeforeEach
  void setUp() {
    LocaleContextHolder.setLocale(LOCALE);
    messageService = new MessageService(messageSource);
  }

  @AfterEach
  void tearDown() {
    LocaleContextHolder.resetLocaleContext();
  }

  @Test
  void createMessage_knownKey_returnsLocalizedMessage() {
    Object[] arguments = {10};
    when(messageSource.getMessage("file.too.large", arguments, LOCALE))
        .thenReturn("Maximum file size is 10 MB");

    String message = messageService.createMessage("file.too.large", arguments);

    assertThat(message).isEqualTo("Maximum file size is 10 MB");
  }

  @Test
  void createMessage_unknownKey_returnsUnexpectedErrorMessage() {
    when(messageSource.getMessage("missing.key", null, LOCALE))
        .thenThrow(new NoSuchMessageException("missing.key"));
    when(messageSource.getMessage("unexpected.error", null, LOCALE)).thenReturn("Unexpected error");

    String message = messageService.createMessage("missing.key", null);

    assertThat(message).isEqualTo("Unexpected error");
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", " "})
  void resolveValidationMessage_missingDefaultMessage_returnsGenericMessage(String defaultMessage) {
    FieldError error = new FieldError("photo", "title", defaultMessage);

    assertThat(messageService.resolveValidationMessage(error)).isEqualTo("Validation error");
  }

  @Test
  void resolveValidationMessage_knownMessage_returnsLocalizedMessage() {
    Object[] arguments = {100};
    FieldError error =
        new FieldError("photo", "title", "value", false, null, arguments, "photo.title.size");
    when(messageSource.getMessage("photo.title.size", arguments, LOCALE))
        .thenReturn("Title must not exceed 100 characters");

    String message = messageService.resolveValidationMessage(error);

    assertThat(message).isEqualTo("Title must not exceed 100 characters");
  }

  @Test
  void resolveValidationMessage_unknownMessage_returnsDefaultMessage() {
    FieldError error = new FieldError("photo", "title", "photo.title.unknown");
    when(messageSource.getMessage("photo.title.unknown", null, LOCALE))
        .thenThrow(new NoSuchMessageException("photo.title.unknown"));

    String message = messageService.resolveValidationMessage(error);

    assertThat(message).isEqualTo("photo.title.unknown");
  }
}
