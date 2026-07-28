package io.github.artsobol.mediaservice.exception.business;

import io.github.artsobol.mediaservice.exception.base.BaseException;
import java.util.Map;
import org.springframework.http.HttpStatus;

public class FileStorageException extends BaseException {

  public FileStorageException(String messageKey, Throwable cause, Object... args) {
    super(messageKey, messageKey, HttpStatus.NOT_FOUND, Map.of(), cause, args);
  }
}
