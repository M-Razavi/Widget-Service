package com.miro.sample.board.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.miro.sample.board.WidgetBoardTest;
import com.miro.sample.board.model.Widget;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("h2")
@SpringBootTest
class DatabaseWidgetRepositoryTest implements WidgetBoardTest {

    @Autowired()
    private WidgetRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void findAllByOrderByZAsc() {
        Widget widget1 = repository.save(newWidget(1));
        Widget widget2 = repository.save(newWidget(2));
        Widget widget3 = repository.save(newWidget(3));
        List<Widget> widgetList = List.of(widget1, widget2, widget3);

        List<Widget> allByOrderByZAsc = IterableUtils.toList(repository.findAllByOrderByZAsc());
        assertTrue(CollectionUtils.isEqualCollection(allByOrderByZAsc, widgetList));
    }

    @Test
    void findByZ() {
        List<Widget> widgets = List.of(newWidget(), newWidget(), newWidget());

        List<Widget> savedWidgets = IterableUtils.toList(repository.saveAll(widgets));

        savedWidgets.sort(Comparator.comparingInt(Widget::getZ));

        Widget firstSaved = savedWidgets.get(0);
        Optional<Widget> byZ = repository.findByZ(firstSaved.getZ());
        assertTrue(byZ.isPresent());
        assertEquals(byZ.get(), firstSaved);
    }

    @Test
    void findMaxZ() {
        Integer max = 30;
        List<Widget> widgets = List.of(newWidget(1), newWidget(max));
        IterableUtils.toList(repository.saveAll(widgets));

        Integer maxZSaved = repository.findMaxZ();
        assertEquals(max, maxZSaved);
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
}