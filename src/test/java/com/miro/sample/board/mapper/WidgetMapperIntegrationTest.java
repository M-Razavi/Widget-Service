package com.miro.sample.board.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.TimeZone;

import com.miro.sample.board.dto.WidgetDto;
import com.miro.sample.board.model.Widget;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class WidgetMapperIntegrationTest {

    private WidgetMapper mapper = Mappers.getMapper(WidgetMapper.class);

    @Test
    void givenWidgetToWidgetDto_whenMaps_thenCorrect() {
        Widget widget = Widget
            .builder()
            .id(1L)
            .version(1065L)
            .x(10)
            .y(20)
            .z(1)
            .height(100)
            .width(200)
            .modifiedDate(System.currentTimeMillis())
            .build();
        WidgetDto widgetDto = mapper.widgetToDto(widget);

        assertEquals(widget.getId(), widgetDto.getId());
        assertEquals(widget.getVersion(), widgetDto.getVersion());
        assertEquals(widget.getX(), widgetDto.getX());
        assertEquals(widget.getY(), widgetDto.getY());
        assertEquals(widget.getZ(), widgetDto.getZ());
        assertEquals(widget.getWidth(), widgetDto.getWidth());
        assertEquals(widget.getHeight(), widgetDto.getHeight());

        assertEquals(LocalDateTime.ofInstant(Instant.ofEpochMilli(widget.getModifiedDate()), TimeZone.getDefault().toZoneId()),
            widgetDto.getModifiedDate());
    }

    @Test
    void givenWidgetToWidgetDtoList_whenMaps_thenCorrect() {
        Widget widget1 = Widget
            .builder()
            .id(1L)
            .version(1065L)
            .x(10)
            .y(20)
            .z(1)
            .height(100)
            .width(200)
            .modifiedDate(System.currentTimeMillis())
            .build();
        Widget widget2 = Widget
            .builder()
            .id(2L)
            .version(3L)
            .x(4)
            .y(5)
            .z(7)
            .height(50)
            .width(70)
            .modifiedDate(System.currentTimeMillis())
            .build();

        List<WidgetDto> widgetDtoList = mapper.widgetToDtoList(List.of(widget1, widget2));

        assertEquals(2, widgetDtoList.size());
        assertEquals(widget1.getId(), widgetDtoList.get(0).getId());
        assertEquals(widget1.getVersion(), widgetDtoList.get(0).getVersion());
        assertEquals(widget1.getX(), widgetDtoList.get(0).getX());
        assertEquals(widget1.getY(), widgetDtoList.get(0).getY());
        assertEquals(widget1.getZ(), widgetDtoList.get(0).getZ());
        assertEquals(widget1.getWidth(), widgetDtoList.get(0).getWidth());
        assertEquals(widget1.getHeight(), widgetDtoList.get(0).getHeight());
        assertEquals(LocalDateTime.ofInstant(Instant.ofEpochMilli(widget1.getModifiedDate()), TimeZone.getDefault().toZoneId()),
            widgetDtoList.get(0).getModifiedDate());

        assertEquals(widget2.getId(), widgetDtoList.get(1).getId());
        assertEquals(widget2.getVersion(), widgetDtoList.get(1).getVersion());
        assertEquals(widget2.getX(), widgetDtoList.get(1).getX());
        assertEquals(widget2.getY(), widgetDtoList.get(1).getY());
        assertEquals(widget2.getZ(), widgetDtoList.get(1).getZ());
        assertEquals(widget2.getWidth(), widgetDtoList.get(1).getWidth());
        assertEquals(widget2.getHeight(), widgetDtoList.get(1).getHeight());
        assertEquals(LocalDateTime.ofInstant(Instant.ofEpochMilli(widget2.getModifiedDate()), TimeZone.getDefault().toZoneId()),
            widgetDtoList.get(0).getModifiedDate());
    }

    @Test
    void givenWidgetDtoToWidgetUpdate_whenMaps_thenCorrect() {
        WidgetDto widgetDto = WidgetDto
            .builder()
            .id(10L)
            .version(1065L)
            .x(20)
            .y(22)
            .z(33)
            .height(50)
            .width(60)
            .modifiedDate(LocalDateTime.now())
            .build();

        Widget widget = mapper.dtoToWidgetUpdate(widgetDto);

        assertNull(widget.getId());
        assertNull(widget.getModifiedDate());
        assertEquals(widgetDto.getVersion(), widget.getVersion());
        assertEquals(widgetDto.getX(), widget.getX());
        assertEquals(widgetDto.getY(), widget.getY());
        assertEquals(widgetDto.getZ(), widget.getZ());
        assertEquals(widgetDto.getWidth(), widget.getWidth());
        assertEquals(widgetDto.getHeight(), widget.getHeight());
    }

    @Test
    void givenWidgetDtoToWidgetCreate_whenMaps_thenCorrect() {
        WidgetDto widgetDto = WidgetDto
            .builder()
            .id(10L)
            .version(1065L)
            .x(20)
            .y(22)
            .z(33)
            .height(50)
            .width(60)
            .modifiedDate(LocalDateTime.now())
            .build();

        Widget widget = mapper.dtoToWidgetCreate(widgetDto);

        assertNull(widget.getId());
        assertNull(widget.getVersion());
        assertNull(widget.getModifiedDate());
        assertEquals(widgetDto.getX(), widget.getX());
        assertEquals(widgetDto.getY(), widget.getY());
        assertEquals(widgetDto.getZ(), widget.getZ());
        assertEquals(widgetDto.getWidth(), widget.getWidth());
        assertEquals(widgetDto.getHeight(), widget.getHeight());
    }
}