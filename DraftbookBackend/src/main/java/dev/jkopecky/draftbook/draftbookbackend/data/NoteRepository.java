package dev.jkopecky.draftbook.draftbookbackend.data;

import org.springframework.data.repository.CrudRepository;

public interface NoteRepository extends CrudRepository<Note, Integer> {
}
