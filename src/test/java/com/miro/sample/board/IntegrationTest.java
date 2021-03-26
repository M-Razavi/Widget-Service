package com.miro.sample.board;

import static com.miro.sample.board.controller.GeneralExceptionHandler.ERROR_CONCURRENT_MODIFICATION;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miro.sample.board.dto.WidgetDto;
import com.miro.sample.board.mapper.WidgetMapper;
import com.miro.sample.board.repository.WidgetRepository;
import com.miro.sample.board.service.WidgetService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IntegrationTest implements WidgetBoardTest {

    private static final String BASE_URL = "/api/v1/widgets";

    private MockMvc mvc;

    @Autowired
    private WidgetMapper widgetMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WidgetRepository repository;

    @Autowired
    private WidgetService service;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeAll
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @AfterEach
    void teardown() {
        repository.deleteAll();
    }

    @Test
    void createThenGetWidget_success() throws Exception {
        MvcResult postResult = createAWidget();
        String json = postResult.getResponse().getContentAsString();
        WidgetDto widgetDto = objectMapper.readValue(json, WidgetDto.class);

        long id = widgetDto.getId();

        mvc.perform(
            get(BASE_URL + "/" + id))
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.x", is(widgetDto.getX())))
            .andExpect(jsonPath("$.y", is(widgetDto.getY())))
            .andExpect(jsonPath("$.z", is(widgetDto.getZ())))
            .andExpect(jsonPath("$.width", is(widgetDto.getWidth())))
            .andExpect(jsonPath("$.height", is(widgetDto.getHeight())))
            .andExpect(jsonPath("$.version", is(notNullValue())))
            .andExpect(jsonPath("$.version", is(notNullValue())))
            .andExpect(jsonPath("$.modifiedDate", is(notNullValue())));
    }

    @Test
    void createMultiWidgetThenGetAll_success() throws Exception {
        List<WidgetDto> widgetDtoSavedList = new ArrayList<>();
        widgetDtoSavedList.add(objectMapper.readValue(createAWidget().getResponse().getContentAsString(), WidgetDto.class));
        widgetDtoSavedList.add(objectMapper.readValue(createAWidget().getResponse().getContentAsString(), WidgetDto.class));
        widgetDtoSavedList.add(objectMapper.readValue(createAWidget().getResponse().getContentAsString(), WidgetDto.class));
        widgetDtoSavedList.add(objectMapper.readValue(createAWidget().getResponse().getContentAsString(), WidgetDto.class));
        int collectionSize = widgetDtoSavedList.size();

        widgetDtoSavedList.sort(Comparator.comparingInt(WidgetDto::getZ));
        int minZ = widgetDtoSavedList.get(0).getZ();
        int maxZ = widgetDtoSavedList.get(collectionSize - 1).getZ();

        mvc.perform(
            get(BASE_URL)
                .contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.totalElements").value(collectionSize))
            .andExpect(jsonPath("$.totalPages").value(1))
            .andExpect(jsonPath("$.size").value(10))
            .andExpect(jsonPath("$.content[0].z", is(minZ)))
            .andExpect(jsonPath("$.content[" + (collectionSize - 1) + "].z", is(maxZ)))
            .andReturn().getResponse().getContentAsString();
    }

    @Test
    void createMultiWidget_updateOutdatedWidget_fail() throws Exception {
        WidgetDto widgetDto1 = objectMapper.readValue(createAWidget(1).getResponse().getContentAsString(), WidgetDto.class);
        WidgetDto widgetDto2 = objectMapper.readValue(createAWidget(2).getResponse().getContentAsString(), WidgetDto.class);
        WidgetDto widgetDto3 = objectMapper.readValue(createAWidget(3).getResponse().getContentAsString(), WidgetDto.class);

        int newZIndex = 2;
        WidgetDto newWidgetDto = objectMapper.readValue(createAWidget(newZIndex).getResponse().getContentAsString(), WidgetDto.class);

        assertEquals(newZIndex, newWidgetDto.getZ());

        widgetDto3.setZ(2);

        mvc.perform(
            put(BASE_URL + "/" + widgetDto3.getId())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(widgetDto3)))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(content().string(containsString(ERROR_CONCURRENT_MODIFICATION)))
            .andReturn();

        mvc.perform(
            get(BASE_URL + "/" + widgetDto1.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.z").value(widgetDto1.getZ()))
            .andReturn();

        mvc.perform(
            get(BASE_URL + "/" + widgetDto2.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.z").value(3))
            .andReturn();

        mvc.perform(
            get(BASE_URL + "/" + widgetDto3.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.z").value(4))
            .andReturn();
    }

    @Test
    @DisplayName("1)Given - 1,2,3; New - 2; Result - 1,2,3,4; Explanation: 2 and 3 has been shifted;")
    void createMultiWidgetCheckShiftingZIndex_success() throws Exception {
        WidgetDto widgetDto1 = objectMapper.readValue(createAWidget(1).getResponse().getContentAsString(), WidgetDto.class);
        WidgetDto widgetDto2 = objectMapper.readValue(createAWidget(2).getResponse().getContentAsString(), WidgetDto.class);
        WidgetDto widgetDto3 = objectMapper.readValue(createAWidget(3).getResponse().getContentAsString(), WidgetDto.class);

        int newZIndex = 2;
        WidgetDto newWidgetDto = objectMapper.readValue(createAWidget(newZIndex).getResponse().getContentAsString(), WidgetDto.class);

        assertEquals(newZIndex, newWidgetDto.getZ());

        mvc.perform(
            get(BASE_URL + "/" + widgetDto1.getId())
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.z").value(widgetDto1.getZ()))
            .andReturn();

        mvc.perform(
            get(BASE_URL + "/" + newWidgetDto.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.z").value(newZIndex))
            .andReturn();

        mvc.perform(
            get(BASE_URL + "/" + widgetDto2.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.z").value(3))
            .andReturn();

        mvc.perform(
            get(BASE_URL + "/" + widgetDto3.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.z").value(4))
            .andReturn();
    }

    @Test
    @DisplayName("2)Given - 1,5,6; New - 2 ; Result - 1,2,5,6; Explanation: No one shifted;")
    void createMultiWidgetNoShiftingZIndex_success() throws Exception {
        WidgetDto widgetDto1 = objectMapper.readValue(createAWidget(1).getResponse().getContentAsString(), WidgetDto.class);
        WidgetDto widgetDto2 = objectMapper.readValue(createAWidget(5).getResponse().getContentAsString(), WidgetDto.class);
        WidgetDto widgetDto3 = objectMapper.readValue(createAWidget(6).getResponse().getContentAsString(), WidgetDto.class);

        int newZIndex = 2;
        WidgetDto newWidgetDto = objectMapper.readValue(createAWidget(newZIndex).getResponse().getContentAsString(), WidgetDto.class);

        assertEquals(newZIndex, newWidgetDto.getZ());

        mvc.perform(
            get(BASE_URL + "/" + widgetDto1.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.z").value(widgetDto1.getZ()))
            .andReturn();

        mvc.perform(
            get(BASE_URL + "/" + widgetDto2.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.z").value(widgetDto2.getZ()))
            .andReturn();

        mvc.perform(
            get(BASE_URL + "/" + widgetDto3.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.z").value(widgetDto3.getZ()))
            .andReturn();
    }

    @Test
    @DisplayName("3)Given - 1,2,4; New - 2; Result - 1,2,3,4; Explanation: Only 2 has been shifted")
    void createMultiWidgetOneShiftingZIndex_success() throws Exception {

        WidgetDto widgetDto1 = objectMapper.readValue(createAWidget(1).getResponse().getContentAsString(), WidgetDto.class);
        WidgetDto widgetDto2 = objectMapper.readValue(createAWidget(2).getResponse().getContentAsString(), WidgetDto.class);
        WidgetDto widgetDto3 = objectMapper.readValue(createAWidget(4).getResponse().getContentAsString(), WidgetDto.class);

        int newZIndex = 2;
        WidgetDto newWidgetDto = objectMapper.readValue(createAWidget(newZIndex).getResponse().getContentAsString(), WidgetDto.class);

        assertEquals(newZIndex, newWidgetDto.getZ());

        mvc.perform(
            get(BASE_URL + "/" + widgetDto1.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.z").value(widgetDto1.getZ()))
            .andReturn();

        mvc.perform(
            get(BASE_URL + "/" + widgetDto2.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.z").value(3))
            .andReturn();

        mvc.perform(
            get(BASE_URL + "/" + widgetDto3.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.z").value(widgetDto3.getZ()))
            .andReturn();
    }

    @Test
    void updateCreatedWidget_success() throws Exception {
        WidgetDto widgetDto = objectMapper.readValue(createAWidget(1).getResponse().getContentAsString(), WidgetDto.class);

        long id = widgetDto.getId();
        widgetDto.setId(1000L);
        widgetDto.setX(0);
        widgetDto.setY(1);
        widgetDto.setWidth(2);
        widgetDto.setHeight(3);
        widgetDto.setZ(7);

        mvc.perform(
            put(BASE_URL + "/" + id)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(widgetDto)))
            .andExpect(status().isOk());

        this.mvc.perform(
            get(BASE_URL + "/" + id))
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.x", is(0)))
            .andExpect(jsonPath("$.y", is(1)))
            .andExpect(jsonPath("$.z", is(7)))
            .andExpect(jsonPath("$.width", is(2)))
            .andExpect(jsonPath("$.height", is(3)))
            .andExpect(jsonPath("$.version", is(notNullValue())))
            .andExpect(jsonPath("$.modifiedDate", is(notNullValue())));
    }

    @Test
    void updateCreatedWidget_optimisticLockFail_fail() throws Exception {
        WidgetDto widgetDtoFirst = objectMapper.readValue(createAWidget(1).getResponse().getContentAsString(), WidgetDto.class);

        //first user attempt
        long id = widgetDtoFirst.getId();
        widgetDtoFirst.setHeight(widgetDtoFirst.getHeight() + 2);

        mvc.perform(
            put(BASE_URL + "/" + id)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(widgetDtoFirst)))
            .andExpect(status().isOk());

        //second user attempt
        widgetDtoFirst.setWidth(widgetDtoFirst.getWidth() - 1);

        mvc.perform(
            put(BASE_URL + "/" + id)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(widgetDtoFirst)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString(ERROR_CONCURRENT_MODIFICATION)))
            .andReturn();
    }

    @Test
    void updateWidget_NotFound_fail() throws Exception {
        long id = 1L;

        mvc.perform(
            put(BASE_URL + "/" + id)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newWidget())))
            .andExpect(status().isNotFound());
    }

    @Test
    void deleteCreatedWidget_success() throws Exception {
        WidgetDto widgetDto = objectMapper.readValue(createAWidget(1).getResponse().getContentAsString(), WidgetDto.class);

        long id = widgetDto.getId();

        mvc.perform(
            delete(BASE_URL + "/" + id))
            .andExpect(status().isNoContent());

        mvc.perform(
            get(BASE_URL + "/" + id))
            .andExpect(status().isNotFound());
    }

    private MvcResult createAWidget() throws Exception {
        WidgetDto widgetDto = newWidgetDto();
        return mvc.perform(
            post(BASE_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(widgetDto)))
            .andExpect(status().isCreated())
            .andReturn();
    }

    private MvcResult createAWidget(int zIndex) throws Exception {
        WidgetDto widgetDto = newWidgetDtoWithZ(zIndex);
        return mvc.perform(
            post(BASE_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(widgetDto)))
            .andExpect(status().isCreated())
            .andReturn();
    }
}
