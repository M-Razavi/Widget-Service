package com.miro.sample.board.mapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.TimeZone;

import com.miro.sample.board.dto.WidgetDto;
import com.miro.sample.board.model.Widget;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface WidgetMapper {

    WidgetDto widgetToDto(Widget widget);

    List<WidgetDto> widgetToDtoList(List<Widget> widgets);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    Widget dtoToWidgetUpdate(WidgetDto widgetDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    Widget dtoToWidgetCreate(WidgetDto widgetDto);

    default LocalDateTime map(Long value) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(value), TimeZone.getDefault().toZoneId());
    }
}