package dev.jkopecky.draftbook.data.tables;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface NoteCategoryRepository extends CrudRepository<NoteCategory, Integer> {
    List<NoteCategory> findByWork_Id(Integer id);
}
