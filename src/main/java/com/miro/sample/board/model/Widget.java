package com.miro.sample.board.model;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "WIDGET")
@EntityListeners(AuditingEntityListener.class)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode
public class Widget {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Version
    private Long version;

    @Positive()
    @NotNull
    private Integer height;

    @Positive()
    @NotNull
    private Integer width;

    @NotNull
    private Integer x;

    @NotNull
    private Integer y;

    private Integer z;

    @LastModifiedDate
    private Long modifiedDate;

    public Widget(Widget widget) {
        this(widget.getId(), widget.getVersion(), widget.getHeight(), widget.getWidth(), widget.getX(), widget.getY(), widget.getZ(),
            widget.getModifiedDate());
    }

}
