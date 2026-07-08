package io.github.artsobol.mediaservice.exception.http;

import io.github.artsobol.mediaservice.exception.base.BaseException;
import java.util.Map;
import org.springframework.http.HttpStatus;

public class BadRequestException extends BaseException {

  public BadRequestException(String errorCode, String messageKey, Object... args) {
    super(errorCode, messageKey, HttpStatus.BAD_REQUEST, Map.of(), null, args);
  }
}
