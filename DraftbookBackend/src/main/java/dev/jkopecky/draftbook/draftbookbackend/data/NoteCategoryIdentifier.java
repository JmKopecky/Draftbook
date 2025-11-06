package dev.jkopecky.draftbook.draftbookbackend.data;

import java.util.ArrayList;
import java.util.List;

public class NoteCategoryIdentifier {

    private int id;
    private String name;
    private ArrayList<NoteIdentifier> noteIdentifiers;

    /**
     * Create a NoteContainerIdentifier based on a noteCategory.
     * @param noteCategory The NoteCategory that this identifier should be based off of.
     * @param noteRepository The table in the database containing notes.
     */
    public NoteCategoryIdentifier(NoteCategory noteCategory, NoteRepository noteRepository) {
        List<Note> noteList = noteCategory.getNotes(noteRepository);
        List<NoteIdentifier> noteIdentifiers = noteList.stream().map(Note::createNoteIdentifier).toList();
        this.id = noteCategory.getId();
        this.name = noteCategory.getName();
        this.noteIdentifiers = new ArrayList<>(noteIdentifiers);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<NoteIdentifier> getNoteIdentifiers() {
        return noteIdentifiers;
    }

    public void setNoteIdentifiers(ArrayList<NoteIdentifier> noteIdentifiers) {
        this.noteIdentifiers = noteIdentifiers;
    }
}
