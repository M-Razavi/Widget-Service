package com.miro.sample.board.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;

import com.miro.sample.board.WidgetBoardTest;
import com.miro.sample.board.model.Widget;
import org.apache.commons.collections4.IterableUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class MemoryWidgetRepositoryTest implements WidgetBoardTest {

    private WidgetRepository repository = new MemoryWidgetRepository();

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }


    @Test
    void save_success() {
        Widget widget = newWidget();

        Widget savedWidget = repository.save(widget);

        assertEquals(1L, savedWidget.getId());
        assertEquals(widget.getZ(), savedWidget.getZ());
    }

    @Test
    void save_concurrent_success() throws Exception {
        int threadCount = 10;
        int insertPerThread = 100;
        List<CompletableFuture<Void>> futures = new ArrayList<>(threadCount * insertPerThread);
        for (int i = 0; i < threadCount; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (int z = 0; z < insertPerThread; z++) {
                    Widget widget = newWidget(z);
                    repository.save(widget);
                }
            });
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).get(5, TimeUnit.SECONDS);
        assertEquals(1000, IterableUtils.size(repository.findAll()));
    }

    @Test
    void update_success() {
        Integer updatedZ = 7;
        Widget savedWidget = repository.save(newWidget(5));
        Long firstOpVersion = savedWidget.getVersion();
        savedWidget.setZ(updatedZ);

        Widget updatedWidget = repository.save(savedWidget);
        Long secondOpVersion = updatedWidget.getVersion();

        assertEquals(updatedZ, updatedWidget.getZ());
        assertNotEquals(firstOpVersion, secondOpVersion);
    }

    @Test
    void updateCheckOptimisticLock_fail() {
        Integer updatedZ = 7;
        Widget widgetLoadedByUser1 = repository.save(newWidget(5));
        Widget widgetLoadedByUser2 = new Widget(widgetLoadedByUser1);
        widgetLoadedByUser1.setZ(updatedZ);
        repository.save(widgetLoadedByUser1);

        widgetLoadedByUser1.setY(9);

        assertThrows(ObjectOptimisticLockingFailureException.class, () -> repository.save(widgetLoadedByUser2));
    }

    @Test
    void saveAll() {
        List<Widget> widgets = List.of(newWidget(), newWidget(), newWidget());

        List<Widget> savedWidgets = IterableUtils.toList(repository.saveAll(widgets));

        for (int i = 1; i <= savedWidgets.size(); i++) {
            assertNotNull(savedWidgets.get(i - 1).getId());
            assertEquals(savedWidgets.get(i - 1).getId(), i);
        }
    }

    @Test
    void findByZ() {
        List<Widget> widgets = List.of(newWidget(), newWidget(), newWidget());

        List<Widget> savedWidgets = IterableUtils.toList(repository.saveAll(widgets));

        Widget firstSaved = savedWidgets.iterator().next();
        Optional<Widget> byZ = repository.findByZ(firstSaved.getZ());
        assertTrue(byZ.isPresent());
        assertEquals(byZ.get(), firstSaved);
    }

    @Test
    void findById() {
        List<Widget> widgets = List.of(newWidget(), newWidget(), newWidget());

        List<Widget> savedWidgets = IterableUtils.toList(repository.saveAll(widgets));

        Widget firstSaved = savedWidgets.iterator().next();
        Optional<Widget> byId = repository.findById(firstSaved.getId());
        assertTrue(byId.isPresent());
        assertEquals(byId.get(), firstSaved);
    }

    @Test
    void existsById() {
        List<Widget> widgets = List.of(newWidget(), newWidget(), newWidget());

        List<Widget> savedWidgets = IterableUtils.toList(repository.saveAll(widgets));

        Widget firstSaved = savedWidgets.iterator().next();
        assertTrue(repository.existsById(firstSaved.getId()));
    }

    @Test
    void findAll() {
        Widget saveWidget1 = repository.save(newWidget());
        Widget saveWidget2 = repository.save(newWidget());

        List<Widget> savedWidgetList = IterableUtils.toList(repository.findAll());

        assertTrue(savedWidgetList.contains(saveWidget1));
        assertTrue(savedWidgetList.contains(saveWidget2));
    }

    @Test
    void findAllById() {
        List<Widget> widgets = List.of(newWidget(), newWidget(), newWidget());

        List<Widget> savedWidgets = IterableUtils.toList(repository.saveAll(widgets));

        List<@NotNull Long> ids = savedWidgets.stream().map(Widget::getId).collect(Collectors.toList());
        Iterable<Widget> allById = repository.findAllById(ids);
        assertEquals(allById, savedWidgets);
    }

    @Test
    void count() {
        List<Widget> widgets = List.of(newWidget(), newWidget(), newWidget());

        List<Widget> savedWidgets = IterableUtils.toList(repository.saveAll(widgets));

        assertEquals(savedWidgets.size(), repository.count());
    }

    @Test
    void deleteById() {
        List<Widget> widgets = List.of(newWidget(), newWidget(), newWidget());
        List<Widget> savedWidgets = IterableUtils.toList(repository.saveAll(widgets));
        Widget firstSaved = savedWidgets.iterator().next();

        repository.deleteById(firstSaved.getId());

        assertFalse(repository.existsById(firstSaved.getId()));
    }

    @Test
    void delete() {
        List<Widget> widgets = List.of(newWidget(), newWidget(), newWidget());
        List<Widget> savedWidgets = IterableUtils.toList(repository.saveAll(widgets));
        Widget firstSaved = savedWidgets.iterator().next();

        repository.delete(firstSaved);

        assertFalse(repository.existsById(firstSaved.getId()));
    }

    @Test
    void deleteAllGivenWidgets() {
        List<Widget> widgets = List.of(newWidget(), newWidget(), newWidget());
        IterableUtils.toList(repository.saveAll(widgets));
        repository.deleteAll();

        Iterable<Widget> all = repository.findAll();
        assertFalse(all.iterator().hasNext());
    }

    @Test
    void deleteAll() {
        List<Widget> widgets = List.of(newWidget(), newWidget(), newWidget());
        List<Widget> savedWidgets = IterableUtils.toList(repository.saveAll(widgets));

        repository.deleteAll(savedWidgets);

        Iterable<Widget> all = repository.findAll();
        assertFalse(all.iterator().hasNext());
    }

    @Test
    void findMaxZ() {
        int max = 30;
        List<Widget> widgets = List.of(newWidget(1), newWidget(max));
        IterableUtils.toList(repository.saveAll(widgets));

        Integer maxZSaved = repository.findMaxZ();
        assertEquals(max, maxZSaved);
    }

    @Test
    void findAllByOrderByZAsc() {
        List<Widget> widgets = List.of(newWidget(1), newWidget(0), newWidget(-1));
        List<Widget> savedWidgets = IterableUtils.toList(repository.saveAll(widgets));

        Iterable<Widget> allByOrderByZAsc = repository.findAllByOrderByZAsc();
        Iterator<Widget> iterator = allByOrderByZAsc.iterator();
        for (int index = -1; iterator.hasNext(); index++) {
            Widget next = iterator.next();
            assertEquals(index, next.getZ());
        }
    }
}