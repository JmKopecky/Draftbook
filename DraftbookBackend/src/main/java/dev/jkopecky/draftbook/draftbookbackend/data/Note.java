package dev.jkopecky.draftbook.draftbookbackend.data;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int Id;

    /**
     * Deletes this note.
     * @param noteRepository The database table for notes.
     */
    public void delete(NoteRepository noteRepository) {
        noteRepository.delete(this);
    }

    public int getId() {
        return Id;
    }
    public void setId(int id) {
        Id = id;
    }
}
