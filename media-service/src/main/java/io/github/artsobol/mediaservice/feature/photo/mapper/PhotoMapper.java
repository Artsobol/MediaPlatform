package io.github.artsobol.mediaservice.feature.photo.mapper;

import io.github.artsobol.mediaservice.config.persistence.MapStructConfig;
import io.github.artsobol.mediaservice.feature.photo.dto.response.PhotoResponse;
import io.github.artsobol.mediaservice.feature.photo.entity.Photo;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface PhotoMapper {

    @BeanMapping(ignoreByDefault = true, ignoreUnmappedSourceProperties = "originalImageKey")
    @Mapping(target = "id")
    @Mapping(target = "title")
    @Mapping(target = "description")
    @Mapping(target = "photoDate")
    @Mapping(target = "photoStatus")
    @Mapping(target = "url")
    PhotoResponse toResponse(Photo entity, String url);
}
