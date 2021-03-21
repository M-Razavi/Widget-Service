package com.miro.sample.board.repository;

import java.util.Optional;

import com.miro.sample.board.model.Widget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * The interface Widget repository.
 */
@NoRepositoryBean
public interface WidgetRepository extends CrudRepository<Widget, Long> {

    /**
     * Find by z optional.
     *
     * @param z the z
     * @return the optional
     */
    Optional<Widget> findByZ(Integer z);

    /**
     * Find max z integer.
     *
     * @return the integer
     */
    Integer findMaxZ();

    /**
     * Find all by order by z asc iterable.
     *
     * @return the iterable
     */
    Iterable<Widget> findAllByOrderByZAsc();


    /**
     * Find all widget with pagination.
     *
     * @param pageable the pageable
     * @return the page
     */
    Page<Widget> findAll(Pageable pageable);

}
