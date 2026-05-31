package io.github.artsobol.mediaservice.exception.business;

import io.github.artsobol.mediaservice.exception.base.BaseException;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class FileStorageException extends BaseException {

    public FileStorageException(String messageKey, Object... args) {
        super(messageKey, messageKey, HttpStatus.NOT_FOUND, Map.of(), null, args);
    }
}
