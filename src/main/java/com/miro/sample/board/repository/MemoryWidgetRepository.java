package com.miro.sample.board.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityNotFoundException;

import com.miro.sample.board.model.Widget;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


/**
 * The type Widget in memory repository.
 */
@Slf4j
@Repository
@Transactional
@Profile("!h2")
public class MemoryWidgetRepository implements WidgetRepository {

    private static final AtomicLong widgetId = new AtomicLong(0L);

    private final ConcurrentMap<Long, Widget> widgetsById;
    private final ConcurrentNavigableMap<Integer, Widget> widgetsByZ;

    /**
     * Instantiates a new Widget in memory repository.
     */
    public MemoryWidgetRepository() {
        this.widgetsById = new ConcurrentHashMap<>();
        this.widgetsByZ = new ConcurrentSkipListMap<>();
    }

    /**
     * * Save widget in the memory.
     *
     * @param widget widget entity
     * @return saved widget
     */
    @Transactional
    public Widget save(Widget widget) {
        if (widget.getId() == null) {
            widget.setId(generateNewId());
            widget.setVersion(0L);
        } else {
            Widget oldWidget = findById(widget.getId()).orElseThrow(EntityNotFoundException::new);

            if (!oldWidget.getVersion().equals(widget.getVersion())) {
                throw new ObjectOptimisticLockingFailureException(Widget.class, widget.getId());
            }
            widget.setVersion(oldWidget.getVersion() + 1);
            widgetsByZ.remove(oldWidget.getZ());
        }
        widget.setModifiedDate(System.currentTimeMillis());
        widgetsById.put(widget.getId(), widget);
        widgetsByZ.put(widget.getZ(), widget);
        return widget;
    }

    /**
     * Save all given widgets in the memory.
     *
     * @param entities widgets
     * @param <S>      any class which extends widget
     * @return saved widgets
     */
    @Transactional
    public <S extends Widget> Iterable<S> saveAll(Iterable<S> entities) {
        List<S> result = new ArrayList<>();

        for (S entity : entities) {
            result.add((S) this.save(entity));
        }

        return result;
    }

    public Optional<Widget> findByZ(Integer z) {
        return Optional.ofNullable(widgetsByZ.get(z));
    }

    public Optional<Widget> findById(Long id) {
        return Optional.ofNullable(widgetsById.get(id));
    }

    public boolean existsById(Long id) {
        return widgetsById.containsKey(id);
    }

    public Iterable<Widget> findAll() {
        return widgetsById.values();
    }

    @Override
    public Page<Widget> findAll(Pageable pageable) {
        List<Widget> list = List.copyOf(widgetsByZ.values());
        int pageSize = pageable.getPageSize();
        int pageNo = pageable.getPageNumber();
        int size = list.size();

        int totalPages = list.size() / pageSize;
        int max = pageNo >= totalPages ? size : pageSize * (pageNo + 1);
        int min = pageNo > totalPages ? max : pageSize * pageNo;

        log.trace("totalPages{} ;pageSize {} ;pageNo {} ;list size {} ;min {} ;max {}", totalPages, pageSize, pageNo, size, min, max);

        return new PageImpl<>(list.subList(min, max), pageable, size);
    }

    @Override
    public Iterable<Widget> findAllByOrderByZAsc() {
        return widgetsByZ.values();
    }

    /**
     * Find all exited widget in memory by given ids.
     *
     * @param ids given ids
     * @return widgets
     */
    public Iterable<Widget> findAllById(Iterable<Long> ids) {
        List<Widget> results = new ArrayList<>();

        for (Long id : ids) {
            this.findById(id).ifPresent(results::add);
        }

        return results;
    }

    public long count() {
        return widgetsById.size();
    }

    @Transactional
    public void deleteById(Long id) {
        Widget deleted = widgetsById.remove(id);
        widgetsByZ.remove(deleted.getZ());
    }

    @Transactional
    public void delete(Widget widget) {
        Widget deleted = widgetsById.remove(widget.getId());
        widgetsByZ.remove(deleted.getZ());
    }

    /**
     * Delete all given widgets from memory.
     *
     * @param entities widget entities
     */
    @Transactional
    public void deleteAll(Iterable<? extends Widget> entities) {

        for (Widget entity : entities) {
            this.delete(entity);
        }
    }

    /**
     * Delete all widgets from memory.
     */
    @Transactional
    public void deleteAll() {
        widgetsById.clear();
        widgetsByZ.clear();
        widgetId.set(0L);
    }

    public Integer findMaxZ() {
        return widgetsByZ.keySet().stream()
            .max(Integer::compareTo).orElse(0);
    }

    private long generateNewId() {
        return widgetId.incrementAndGet();
    }

}