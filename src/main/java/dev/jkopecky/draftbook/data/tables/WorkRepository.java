package dev.jkopecky.draftbook.data.tables;

import org.springframework.data.repository.CrudRepository;

import java.util.ArrayList;
import java.util.List;

public interface WorkRepository extends CrudRepository<Work, Integer> {
    List<Work> findByAccount_Id(Integer accountId);
}
