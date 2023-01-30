package com.miro.sample.board.service;

import com.miro.sample.board.exceptions.ConcurrentWidgetModificationException;
import com.miro.sample.board.exceptions.NotFoundException;
import com.miro.sample.board.model.Widget;
import com.miro.sample.board.repository.WidgetRepository;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The Widget service.
 */
@Slf4j
@Service
@Validated
public class WidgetService {
    private final Map<Long, Lock> locks = new ConcurrentHashMap<>();
    private WidgetRepository repository;

    /**
     * Sets repository.
     *
     * @param repository the repository
     */
    @Autowired
    public void setRepository(WidgetRepository repository) {
        this.repository = repository;
    }

    /**
     * Create widget.
     *
     * @param widget the widget
     * @return the widget
     */
    public Widget create(@Valid Widget widget) {
        log.trace("Creating a widget {}", widget);
        Widget result = saveWidget(widget, null);
        log.trace("Widget created {}", result);
        return result;
    }

    /**
     * Update widget.
     *
     * @param id        the id
     * @param newWidget the new widget
     * @return the widget
     */
    public Widget update(@NotNull @Min(1) Long id, @Valid Widget newWidget) {
        log.trace("Updating a widget {}", newWidget);
        if (newWidget.getZ() == null) {
            throw new ConstraintViolationException("The Z index could not be null.", null);
        }

        Widget result = saveWidget(newWidget, id);
        log.trace("Widget updated {}", newWidget);

        return result;
    }

    /**
     * Delete.
     *
     * @param id the id
     */
    public void delete(@NotNull @Min(1) Long id) {
        log.debug("Removing widget. id={}", id);
        repository.deleteById(id);
    }

    /**
     * Find by id optional.
     *
     * @param id the id
     * @return the optional
     */
    public Optional<Widget> findById(@NotNull @Min(1) Long id) {
        log.debug("Find widget by id {}", id);
        return repository.findById(id);
    }

    /**
     * Exist boolean.
     *
     * @param id the id
     * @return the boolean
     */
    public boolean exist(@NotNull @Min(1) Long id) {
        return repository.existsById(id);
    }

    /**
     * Find all widget and return paginated.
     *
     * @param page page number
     * @param size size of each page
     * @return page of Widget
     */
    public Page<Widget> findAll(int page, int size) {
        log.debug("Find all Widgets");
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("z"));
        return repository.findAll(pageRequest);
    }

    private synchronized Widget saveWidget(@Valid Widget widget, Long id) {
        boolean needCheckShiftingZ;
        Widget oldWidget;

        if (id == null) { // insert
            needCheckShiftingZ = true;
        } else { // update
            oldWidget = repository.findById(id).orElseThrow(NotFoundException::new);
            if (!oldWidget.getVersion().equals(widget.getVersion())) {
                throw new ObjectOptimisticLockingFailureException(Widget.class, oldWidget.getId());
            }
            needCheckShiftingZ = !oldWidget.getZ().equals(widget.getZ());
            widget.setId(oldWidget.getId());
        }

        if (widget.getZ() == null) {
            Integer maxZ = repository.findMaxZ();
            checkIntOverflow(maxZ);
            widget.setZ(maxZ + 1);
        } else if (needCheckShiftingZ) {
            shiftWidgets(widget.getZ());
        }
        return repository.save(widget);
    }

    private void shiftWidgets(Integer z) {
        log.trace("Check Shifting for Z Index:{}", z);

        checkIntOverflow(z);
        Widget match = repository.findByZ(z).orElse(null);
        if (match == null) {
            return;
        }
        log.trace("Shifting widget:{}", match);

        Widget updatedMatch = new Widget(match);
        updatedMatch.setZ(updatedMatch.getZ() + 1);

        Lock lock = locks.computeIfAbsent(updatedMatch.getId(), k -> new ReentrantLock());
        if (!lock.tryLock()) {
            throw new ConcurrentWidgetModificationException("Another request is concurrently modifying the Widget with id "
                + updatedMatch.getId());
        }

        try {
            shiftWidgets(z + 1);
            repository.save(updatedMatch);
        } finally {
            lock.unlock();
        }
    }

    private void checkIntOverflow(Integer maxZ) {
        if (maxZ.equals(Integer.MAX_VALUE)) {
            throw new ArithmeticException("The maximum size of z index is reached.");
        }
    }
}
