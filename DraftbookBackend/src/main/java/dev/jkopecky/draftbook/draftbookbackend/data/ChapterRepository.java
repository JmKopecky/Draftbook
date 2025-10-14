package dev.jkopecky.draftbook.draftbookbackend.data;

import org.springframework.data.repository.CrudRepository;

public interface ChapterRepository extends CrudRepository<Chapter, Integer> {
}
