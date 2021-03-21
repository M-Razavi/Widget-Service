package com.miro.sample.board.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import javax.validation.ConstraintViolationException;

import com.miro.sample.board.WidgetBoardTest;
import com.miro.sample.board.exceptions.NotFoundException;
import com.miro.sample.board.model.Widget;
import com.miro.sample.board.repository.WidgetRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

@SpringBootTest
class WidgetServiceTest implements WidgetBoardTest {

    @MockBean
    private WidgetRepository repository;

    @Autowired
    private WidgetService service;

    @Test
    void create_success() {
        Widget widget = newWidget();
        widget.setId(1L);
        Mockito.doReturn(widget).when(repository).save(any());

        assertNotNull(service.create(widget));
    }

    @Test
    void createWithZ_zIndexOverflow_fail() {
        int zIndex = Integer.MAX_VALUE;
        Widget widget = newWidget(zIndex);
        Mockito.doReturn(Optional.of(widget)).when(repository).findByZ(zIndex);

        assertThrows(ArithmeticException.class, () -> service.create(widget));
    }

    @Test
    void createWithoutZ_zIndexOverflow_fail() {
        int zIndex = Integer.MAX_VALUE;
        Widget widget = newWidget();
        widget.setZ(null);
        Mockito.doReturn(zIndex).when(repository).findMaxZ();

        assertThrows(ArithmeticException.class, () -> service.create(widget));
    }

    @Test
    void updateWithZ_zIndexOverflow_fail() {
        Long id = 1L;
        int zIndex = Integer.MAX_VALUE;
        Widget newWidget = newWidget(zIndex);
        newWidget.setId(id);
        newWidget.setVersion(0L);
        Widget oldWidget = newWidget(10);
        oldWidget.setId(id);
        oldWidget.setVersion(0L);
        Mockito.doReturn(Optional.of(oldWidget)).when(repository).findById(id);
        Mockito.doReturn(Optional.of(oldWidget)).when(repository).findByZ(zIndex);
        Mockito.doReturn(zIndex).when(repository).findMaxZ();
        Mockito.doReturn(newWidget).when(repository).save(any());

        assertThrows(ArithmeticException.class, () -> service.update(id, newWidget));
    }

    @Test
    void updateWithoutZ_fail() {
        Long id = 1L;
        Widget widget = newWidget();
        widget.setZ(null);
        widget.setId(id);
        Mockito.doReturn(Optional.of(widget)).when(repository).findById(id);

        assertThrows(ConstraintViolationException.class, () -> service.update(id, widget));
    }

    @Test
    void update_optimisticLockFail_fail() {
        Long id = 1L;
        Widget widgetOld = newWidget();
        widgetOld.setId(id);
        widgetOld.setVersion(1L);
        Widget widgetToUpdate = new Widget(widgetOld);
        widgetToUpdate.setVersion(0L);
        widgetToUpdate.setZ(2);
        Mockito.doReturn(Optional.of(widgetOld)).when(repository).findById(id);

        assertThrows(ObjectOptimisticLockingFailureException.class, () -> service.update(id, widgetToUpdate));
    }

    @SuppressWarnings("checkstyle:ParenPad")
    @Test
    void save_concurrent_success() throws Exception {
        int threadCount = 3;
        int insertPerThread = 2;
        AtomicLong widgetId = new AtomicLong(0);

        Widget widget1 = newWidget(1);
        widget1.setId(1L);
        Widget widget2 = newWidget(2);
        widget2.setId(2L);
        Widget widget3 = newWidget(3);
        widget3.setId(3L);
        Widget widget4 = newWidget(4);
        widget4.setId(4L);
        Widget widget5 = newWidget(5);
        widget5.setId(5L);
        Widget widget6 = newWidget(6);
        widget6.setId(6L);

        Mockito.doReturn(Optional.of(widget1)).when(repository).findByZ(1);
        Mockito.doReturn(Optional.of(widget2)).when(repository).findByZ(2);
        Mockito.doReturn(Optional.of(widget3)).when(repository).findByZ(3);
        Mockito.doReturn(Optional.of(widget4)).when(repository).findByZ(4);
        Mockito.doReturn(Optional.of(widget5)).when(repository).findByZ(5);
        Mockito.doReturn(Optional.of(widget6)).when(repository).findByZ(6);


        List<Widget> widgetList = List.of(widget1, widget2, widget3, widget4, widget5, widget6);

        System.out.println(widgetList.toString());
        List<CompletableFuture<Void>> futures = new ArrayList<>(threadCount * insertPerThread);
        for (int i = 0; i < threadCount; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (int z = 0; z < insertPerThread; z++) {
                    System.out.println(z);
                    final long id = widgetId.getAndIncrement();
                    Widget widget = widgetList.get((int) id);
                    System.out.println("widget" + id);
                    service.create(widget);
                }
            });
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).get(5, TimeUnit.SECONDS);
        verify(repository, times(27)).save(any());
    }

    @Test
    void create_invalidWidget_fail() {
        Widget widget = new Widget();

        assertThrows(ConstraintViolationException.class, () -> service.create(widget));
    }

    @Test
    void delete_success() {
        service.delete(1L);

        verify(repository, times(1)).deleteById(any());

    }

    @Test
    void delete_invalidId_fail() {
        assertThrows(ConstraintViolationException.class, () -> service.delete(0L));

        assertThrows(ConstraintViolationException.class, () -> service.delete(null));
    }

    @Test
    void findById() {
        Optional<Widget> widget = Optional.of(newWidget());
        Mockito.doReturn(widget).when(repository).findById(any());

        assertTrue(service.findById(1L).isPresent());

        Mockito.doReturn(Optional.empty()).when(repository).findById(any());

        assertTrue(service.findById(1L).isEmpty());
    }

    @Test
    void exist() {
        Mockito.doReturn(true).when(repository).existsById(any());

        assertTrue(service.exist(1L));

        Mockito.doReturn(false).when(repository).existsById(any());

        assertFalse(service.exist(1L));
    }

    @Test
    void findAll() {
        int pageNumber = 1;
        int pageSize = 10;
        service.findAll(pageNumber, pageSize);
        ArgumentCaptor<Pageable> argument = ArgumentCaptor.forClass(Pageable.class);

        verify(repository).findAll(argument.capture());
        assertEquals(pageNumber, argument.getValue().getPageNumber());
        assertEquals(pageSize, argument.getValue().getPageSize());
    }


    @Test
    void update_success() {
        Long updateId = 10L;
        Widget widget = newWidget();
        widget.setId(10L);
        widget.setVersion(0L);

        Mockito.doReturn(Optional.of(widget)).when(repository).findById(updateId);
        Mockito.doReturn(widget).when(repository).save(any());
        Widget toUpdate = new Widget(widget);
        toUpdate.setId(20L);

        Widget updated = service.update(updateId, toUpdate);
        assertEquals(updateId, updated.getId());
    }

    @Test
    void update_idNotFound_fail() {
        Long updateId = 10L;
        Widget widget = newWidget();
        widget.setId(10L);

        Mockito.doReturn(Optional.empty()).when(repository).findById(any());

        assertThrows(NotFoundException.class, () -> service.update(updateId, widget));
    }
}