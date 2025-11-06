package dev.jkopecky.draftbook.draftbookbackend.data;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface NoteCategoryRepository extends CrudRepository<NoteCategory, Integer> {
    List<NoteCategory> findAllByWorkIdAndName(int workId, String name);
    List<NoteCategory> findAllByWorkId(int workId);
}
