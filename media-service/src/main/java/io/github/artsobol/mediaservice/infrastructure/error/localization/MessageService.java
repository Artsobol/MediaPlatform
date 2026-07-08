package io.github.artsobol.mediaservice.infrastructure.error.localization;

import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.validation.FieldError;

@Service
@RequiredArgsConstructor
public class MessageService {

  private final MessageSource messageSource;

  public String createMessage(String key, Object[] args) {
    try {
      Locale locale = LocaleContextHolder.getLocale();
      return messageSource.getMessage(key, args, locale);
    } catch (Exception e) {
      return messageSource.getMessage("unexpected.error", null, LocaleContextHolder.getLocale());
    }
  }

  public String resolveValidationMessage(FieldError error) {
    String defaultMessage = error.getDefaultMessage();
    if (defaultMessage == null || defaultMessage.isBlank()) {
      return "Validation error";
    }

    try {
      return messageSource.getMessage(
          defaultMessage, error.getArguments(), LocaleContextHolder.getLocale());
    } catch (Exception e) {
      return defaultMessage;
    }
  }
}
