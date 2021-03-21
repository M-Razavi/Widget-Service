package com.miro.sample.board;

import com.miro.sample.board.dto.WidgetDto;
import com.miro.sample.board.model.Widget;
import org.apache.commons.lang3.RandomUtils;

public interface WidgetBoardTest {

    default Widget newWidget() {
        return Widget.builder()
            .x(RandomUtils.nextInt())
            .y(RandomUtils.nextInt())
            .z(RandomUtils.nextInt())
            .height(RandomUtils.nextInt(1, 1000))
            .width(RandomUtils.nextInt(1, 1000))
            .build();
    }

    default WidgetDto newWidgetDto() {
        return WidgetDto.builder()
            .id(RandomUtils.nextLong())
            .version(RandomUtils.nextLong())
            .x(RandomUtils.nextInt())
            .y(RandomUtils.nextInt())
            .z(RandomUtils.nextInt())
            .height(RandomUtils.nextInt(1, 1000))
            .width(RandomUtils.nextInt(1, 1000))
            .build();
    }

    default WidgetDto newWidgetDto(Long id) {
        return WidgetDto.builder()
            .id(id)
            .version(RandomUtils.nextLong())
            .x(RandomUtils.nextInt())
            .y(RandomUtils.nextInt())
            .z(RandomUtils.nextInt())
            .height(RandomUtils.nextInt(1, 1000))
            .width(RandomUtils.nextInt(1, 1000))
            .build();
    }

    default WidgetDto newWidgetDtoWithZ(int z) {
        return WidgetDto.builder()
            .id(RandomUtils.nextLong())
            .version(RandomUtils.nextLong())
            .x(RandomUtils.nextInt())
            .y(RandomUtils.nextInt())
            .z(z)
            .height(RandomUtils.nextInt(1, 1000))
            .width(RandomUtils.nextInt(1, 1000))
            .build();
    }

    default Widget newWidget(Integer zIndex) {
        return Widget.builder()
            .z(zIndex)
            .x(RandomUtils.nextInt())
            .y(RandomUtils.nextInt())
            .height(RandomUtils.nextInt(1, 1000))
            .width(RandomUtils.nextInt(1, 1000))
            .build();
    }
}
