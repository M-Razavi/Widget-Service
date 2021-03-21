package com.miro.sample.board.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import com.miro.sample.board.dto.WidgetDto;
import com.miro.sample.board.exceptions.NotFoundException;
import com.miro.sample.board.mapper.WidgetMapper;
import com.miro.sample.board.model.Widget;
import com.miro.sample.board.service.WidgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(path = {"api/v1/widgets"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class WidgetController {

    private static final String ID = "widgetId";
    private static final String NEW_WIDGET_LOG = "New widget was created id:{}";
    private static final String WIDGET_UPDATED_LOG = "Widget:{} was updated";

    private WidgetService service;
    private WidgetMapper mapper;

    @Autowired
    public void setMapper(WidgetMapper mapper) {
        this.mapper = mapper;
    }

    @Autowired
    public void setService(WidgetService service) {
        this.service = service;
    }

    @Operation(summary = "Create a new widget")
    @ApiResponse(responseCode = "201", description = "widget is created",
        content = {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = WidgetDto.class))})
    @PostMapping()
    public ResponseEntity<WidgetDto> create(@Valid @RequestBody WidgetDto widgetDto) {
        Widget widget = mapper.dtoToWidgetCreate(widgetDto);
        final Widget createdWidget = service.create(widget);
        log.info(NEW_WIDGET_LOG, createdWidget.toString());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.widgetToDto(createdWidget));
    }

    @Operation(summary = "Delete an widget by its id")
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Deleted the Widget",
        content = {@Content(mediaType = APPLICATION_JSON_VALUE)}),
        @ApiResponse(responseCode = "404", description = "Widget not found", content = @Content)})
    @DeleteMapping(path = "/{widgetId}")
    public ResponseEntity<Void> delete(@PathVariable(value = ID) @Min(1) Long widgetId) {
        if (service.exist(widgetId)) {
            service.delete(widgetId);
            final HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            return ResponseEntity.noContent().headers(httpHeaders).build();
        }
        throw new NotFoundException();
    }

    @Operation(summary = "Update an widget by its id")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Widget was updated",
        content = {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = WidgetDto.class))}),
        @ApiResponse(responseCode = "404", description = "Widget not found", content = @Content)})
    @PutMapping(path = "/{widgetId}")
    public ResponseEntity<WidgetDto> update(
        @PathVariable(value = ID) @Min(1) Long id,
        @Valid @RequestBody WidgetDto widgetDto) {
        Widget widget = mapper.dtoToWidgetUpdate(widgetDto);
        Widget updatedWidget = service.update(id, widget);
        log.info(WIDGET_UPDATED_LOG, updatedWidget.toString());
        return ResponseEntity.ok(mapper.widgetToDto(updatedWidget));
    }

    @Operation(summary = "Get an widget by its id")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Found the Widget",
        content = {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = WidgetDto.class))}),
        @ApiResponse(responseCode = "404", description = "Widget not found", content = @Content)})
    @GetMapping(path = "/{widgetId}")
    public ResponseEntity<WidgetDto> getWidget(@PathVariable(value = ID) @Min(1) Long widgetId) {
        final Widget widget = service.findById(widgetId).orElseThrow(NotFoundException::new);
        return ResponseEntity.ok(mapper.widgetToDto(widget));
    }

    @Operation(summary = "Returns a list of widgets")
    @ApiResponse(responseCode = "200", description = "Returns a list of widgets",
        content = {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = WidgetDto.class))})
    @GetMapping
    public ResponseEntity<Page<WidgetDto>> getWidgets(@RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "10") Integer size) {

        Page<Widget> all = service.findAll(page, size);
        Page<WidgetDto> resultPage = new PageImpl<>(mapper.widgetToDtoList(all.getContent()), all.getPageable(), all.getTotalElements());

        return ResponseEntity.status(HttpStatus.OK).body(resultPage);
    }
}