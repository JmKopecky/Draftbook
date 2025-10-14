package dev.jkopecky.draftbook.draftbook.data;

import org.springframework.data.repository.CrudRepository;

public interface NoteRepository extends CrudRepository<Note, Integer> {
}
