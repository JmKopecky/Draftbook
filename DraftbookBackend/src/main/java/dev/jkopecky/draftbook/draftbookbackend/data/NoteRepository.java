package dev.jkopecky.draftbook.draftbookbackend.data;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface NoteRepository extends CrudRepository<Note, Integer> {
    List<Note> findByNoteCategory(NoteCategory noteCategory);
}
