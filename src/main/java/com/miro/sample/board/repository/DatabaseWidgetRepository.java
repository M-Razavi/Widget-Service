package com.miro.sample.board.repository;

import java.util.Optional;

import com.miro.sample.board.model.Widget;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

/**
 * The interface Widget db repository for H2DB.
 */
@Profile("h2")
@Repository
public interface DatabaseWidgetRepository extends WidgetRepository, PagingAndSortingRepository<Widget, Long> {

    Iterable<Widget> findAllByOrderByZAsc();

    Optional<Widget> findByZ(Integer z);

    @Query(value = "SELECT max(z) FROM WIDGET", nativeQuery = true)
    Integer findMaxZ();

}