package dev.jkopecky.draftbook.data.tables;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ChapterRepository extends CrudRepository<Chapter, Integer> {
    List<Chapter> findByWork_Id(Integer id);
}
