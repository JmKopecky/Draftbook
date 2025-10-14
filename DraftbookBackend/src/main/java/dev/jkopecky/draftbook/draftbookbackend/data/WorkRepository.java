package dev.jkopecky.draftbook.draftbook.data;

import org.springframework.data.repository.CrudRepository;

import java.util.Collection;

public interface WorkRepository extends CrudRepository<Work, Integer> {
    Collection<Work> findAllByAccountId(int id);
}
