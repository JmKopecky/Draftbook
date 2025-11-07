package dev.jkopecky.draftbook.draftbookbackend.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import org.springframework.http.HttpStatus;

import java.util.NoSuchElementException;

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

    /**
     * Provide a note based on a request, assuming the user is permitted to access it.
     * @param requestBody The request from which to identify the work.
     * @param workRepository The table in the database containing works.
     * @param noteRepository The table in the database containing notes.
     * @param noteCategoryRepository the table in the database containing note categories.
     * @param account The account which should own the work in question.
     * @return An array of Objects, where the first element is a HttpStatusCode, the second is a work, and the third is a noteCategory.
     */
    public static Object[] getNoteIfAllowed(
            String requestBody, Account account, WorkRepository workRepository, NoteRepository noteRepository, NoteCategoryRepository noteCategoryRepository) {

        //get work and check that it exists
        Object[] workContainer = Work.getWorkIfAllowed(requestBody, account, workRepository);
        Work work;
        if (workContainer[0] == null) {
            return workContainer;
        } else {
            work = (Work) workContainer[0];
        }

        Object[] result = new Object[3];
        result[0] = workContainer[1];
        result[1] = workContainer[0];

        //get the note
        Note note;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(requestBody);
            int noteId = node.get("note_id").asInt();
            note = noteRepository.findById(noteId).get();
        } catch (JsonProcessingException | NullPointerException e) {
            result[0] = HttpStatus.BAD_REQUEST;
            return result;
        } catch (NoSuchElementException e) {
            result[0] = HttpStatus.NOT_FOUND;
            return result;
        }

        //check that the note's category is for the work
        if (note.getNoteCategory().getWorkId() == work.getId()) {
            if (note.getNoteCategory().getName().equals(NoteCategory.CHAPTER_CATEGORY_NAME)) {
                result[0] = HttpStatus.CONFLICT;
                result[2] = null;
            } else {
                result[2] = note;
            }
            return result;
        }

        result[0] = HttpStatus.UNAUTHORIZED;
        result[2] = null;
        return result;
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
