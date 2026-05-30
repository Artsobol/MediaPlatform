package io.github.artsobol.mediaservice.feature.photo.service;

import io.github.artsobol.mediaservice.feature.photo.entity.Photo;

public interface PhotoFinder {

    Photo getByIdOrThrow(Long id);
}
