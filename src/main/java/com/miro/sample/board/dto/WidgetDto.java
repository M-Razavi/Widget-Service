package com.miro.sample.board.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WidgetDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 7860506206722471520L;

    private Long id;

    private Long version;

    @NotNull
    @Positive
    private Integer height;

    @NotNull
    @Positive
    private Integer width;

    @NotNull
    private Integer x;

    @NotNull
    private Integer y;

    private Integer z;

    private LocalDateTime modifiedDate;

}
