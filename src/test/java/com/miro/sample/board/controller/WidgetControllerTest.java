package com.miro.sample.board.controller;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miro.sample.board.WidgetBoardTest;
import com.miro.sample.board.dto.WidgetDto;
import com.miro.sample.board.mapper.WidgetMapper;
import com.miro.sample.board.model.Widget;
import com.miro.sample.board.service.WidgetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = WidgetController.class)
class WidgetControllerTest implements WidgetBoardTest {

    private static final String BASE_URL = "/api/v1/widgets";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private WidgetMapper widgetMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WidgetService service;

    @BeforeEach
    void setup() {
    }

    @Test
    void createWidget_success() throws Exception {
        WidgetDto widgetDto = newWidgetDto();
        Widget widget = new Widget(widgetDto.getId(), widgetDto.getVersion(), widgetDto.getHeight(), widgetDto.getWidth(),
            widgetDto.getX(), widgetDto.getY(), widgetDto.getZ(), System.currentTimeMillis());

        Mockito.when(service.create(any())).thenReturn(widget);
        Mockito.when(widgetMapper.widgetToDto(any())).thenReturn(widgetDto);

        String contentAsString = mvc.perform(post(BASE_URL)
            .content(objectMapper.writeValueAsBytes(widgetDto))
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn().getResponse().getContentAsString();
        WidgetDto createdWidgetDto = objectMapper.readValue(contentAsString, WidgetDto.class);

        assertNotNull(createdWidgetDto.getId());
        assertNotNull(createdWidgetDto.getVersion());
        assertEquals(widgetDto.getX(), createdWidgetDto.getX());
        assertEquals(widgetDto.getY(), createdWidgetDto.getY());
        assertEquals(widgetDto.getZ(), createdWidgetDto.getZ());
        assertEquals(widgetDto.getWidth(), createdWidgetDto.getWidth());
        assertEquals(widgetDto.getHeight(), createdWidgetDto.getHeight());
    }

    @Test
    void createWidget_invalidRequestBody() throws Exception {
        WidgetDto widgetDto = new WidgetDto();

        mvc.perform(post(BASE_URL)
            .content(objectMapper.writeValueAsBytes(widgetDto))
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn();
    }

    @Test
    void updateWidget_success() throws Exception {
        Long id = 1L;
        WidgetDto widgetDto = newWidgetDto(id);
        Widget widget = new Widget(widgetDto.getId(), widgetDto.getVersion(), widgetDto.getHeight(), widgetDto.getWidth(),
            widgetDto.getX(), widgetDto.getY(), widgetDto.getZ(), System.currentTimeMillis());

        Mockito.when(service.update(id, widget)).thenReturn(widget);
        Mockito.when(widgetMapper.widgetToDto(any())).thenReturn(widgetDto);
        Mockito.when(widgetMapper.dtoToWidgetUpdate(any())).thenReturn(widget);

        String contentAsString = mvc.perform(put(BASE_URL + "/" + id)
            .content(objectMapper.writeValueAsBytes(widgetDto))
            .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn().getResponse().getContentAsString();
        WidgetDto createdWidgetDto = objectMapper.readValue(contentAsString, WidgetDto.class);

        assertNotNull(createdWidgetDto.getId());
        assertNotNull(createdWidgetDto.getVersion());
        assertEquals(widgetDto.getId(), createdWidgetDto.getId());
        assertEquals(widgetDto.getX(), createdWidgetDto.getX());
        assertEquals(widgetDto.getY(), createdWidgetDto.getY());
        assertEquals(widgetDto.getZ(), createdWidgetDto.getZ());
        assertEquals(widgetDto.getWidth(), createdWidgetDto.getWidth());
        assertEquals(widgetDto.getHeight(), createdWidgetDto.getHeight());
    }

    @Test
    void updateWidget_invalidId() throws Exception {
        Long id = 0L;
        WidgetDto widgetDto = newWidgetDto(id);

        mvc.perform(put(BASE_URL + "/" + id)
            .content(objectMapper.writeValueAsBytes(widgetDto))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn();
    }

    @Test
    void getWidget_success() throws Exception {
        WidgetDto widgetDto = newWidgetDto();
        Widget widget = new Widget(widgetDto.getId(), widgetDto.getVersion(), widgetDto.getHeight(), widgetDto.getWidth(),
            widgetDto.getX(), widgetDto.getY(), widgetDto.getZ(), System.currentTimeMillis());

        Mockito.when(service.findById(widgetDto.getId())).thenReturn(java.util.Optional.of(widget));
        Mockito.when(widgetMapper.widgetToDto(any())).thenReturn(widgetDto);

        String contentAsString = mvc.perform(get(BASE_URL + "/" + widgetDto.getId())
            .content(objectMapper.writeValueAsBytes(widgetDto))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn().getResponse().getContentAsString();
        WidgetDto createdWidgetDto = objectMapper.readValue(contentAsString, WidgetDto.class);

        assertNotNull(createdWidgetDto.getId());
        assertNotNull(createdWidgetDto.getVersion());
        assertEquals(widgetDto.getId(), createdWidgetDto.getId());
        assertEquals(widgetDto.getX(), createdWidgetDto.getX());
        assertEquals(widgetDto.getY(), createdWidgetDto.getY());
        assertEquals(widgetDto.getZ(), createdWidgetDto.getZ());
        assertEquals(widgetDto.getWidth(), createdWidgetDto.getWidth());
        assertEquals(widgetDto.getHeight(), createdWidgetDto.getHeight());
    }

    @Test
    void getAllWidget_success() throws Exception {
        long id = 1;
        int size = 10;
        int pageNumber = 0;
        WidgetDto widgetDto = newWidgetDto(id);
        Widget widget = new Widget(widgetDto.getId(), widgetDto.getVersion(), widgetDto.getHeight(), widgetDto.getWidth(),
            widgetDto.getX(), widgetDto.getY(), widgetDto.getZ(), System.currentTimeMillis());
        Page<Widget> widgetPage = new PageImpl<>(List.of(widget), PageRequest.of(pageNumber, size), 1);
        Mockito.when(service.findAll(anyInt(), anyInt())).thenReturn(widgetPage);
        Mockito.when(widgetMapper.widgetToDtoList(any())).thenReturn(List.of(widgetDto));

        mvc.perform(get(BASE_URL)
            .content(objectMapper.writeValueAsBytes(widgetDto))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.totalPages").value(1))
            .andExpect(jsonPath("$.size").value(size))
            .andExpect(jsonPath("$.content[0].z", is(widgetDto.getZ())))
            .andReturn().getResponse().getContentAsString();

        ArgumentCaptor<Integer> pageArg = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> sizeArg = ArgumentCaptor.forClass(Integer.class);

        verify(service).findAll(pageArg.capture(), sizeArg.capture());
        assertEquals(pageNumber, pageArg.getValue());
        assertEquals(size, sizeArg.getValue());
    }

    @Test
    void getWidget_notFound() throws Exception {
        Long id = 1L;
        Mockito.when(service.findById(id)).thenReturn(java.util.Optional.empty());

        mvc.perform(get(BASE_URL + "/" + id)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn();
    }

    @Test
    void deleteWidget_success() throws Exception {
        Mockito.doReturn(true).when(service).exist(any());
        mvc.perform(delete(BASE_URL + "/1")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isNoContent())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn();
    }

    @Test
    void deleteWidget_notFound() throws Exception {
        Mockito.doReturn(false).when(service).exist(any());
        mvc.perform(delete(BASE_URL + "/1")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn();
    }

    @Test
    void deleteWidget_invalidId() throws Exception {
        Mockito.doReturn(false).when(service).exist(any());
        mvc.perform(delete(BASE_URL + "/0")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn();
    }
}
