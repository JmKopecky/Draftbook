package dev.jkopecky.draftbook.draftbookbackend.data;

import jakarta.persistence.*;

@Entity
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int Id;
    private String descriptor;
    @ManyToOne
    private NoteCategory noteCategory;
    @Lob
    String content;


    public Note() {}

    public Note(String descriptor, NoteCategory noteCategory, NoteRepository noteRepository) {
        this.descriptor = descriptor;
        this.noteCategory = noteCategory;
        this.content = "";
        noteRepository.save(this);
    }

    /**
     * Util method to create an identifier representation of this note.
     * @return A NoteIdentifier that can be easily returned to the client without content.
     */
    public NoteIdentifier createNoteIdentifier() {
        NoteIdentifier noteIdentifier = new NoteIdentifier();
        noteIdentifier.setId(this.Id);
        noteIdentifier.setDescriptor(descriptor);
        noteIdentifier.setCategoryId(noteCategory.getId());
        noteIdentifier.setCategoryName(noteCategory.getName());
        return noteIdentifier;
    }

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

    public String getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(String content) {
        this.descriptor = content;
    }
    public NoteCategory getNoteCategory() {
        return noteCategory;
    }
    public void setNoteCategory(NoteCategory noteCategory) {
        this.noteCategory = noteCategory;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
}
