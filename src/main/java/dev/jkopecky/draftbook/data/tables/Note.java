package dev.jkopecky.draftbook.data.tables;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jkopecky.draftbook.Log;
import jakarta.persistence.*;

import java.io.FileWriter;
import java.io.IOException;

import java.io.File;

@Entity
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String name;
    @ManyToOne
    private NoteCategory category;




    public static Note createNote(String name, String content, NoteCategory category, NoteRepository noteRepository, NoteCategoryRepository noteCategoryRepository) throws IOException {
        Note note = new Note();
        note.name = name;
        note.category = category;
        noteRepository.save(note);
        category.learnNote(note, noteCategoryRepository);
        //todo update noteCategory to reflect the owned note


        //create note files
        String notePath = category.findPath() + "note_" + note.getId();
        File noteBackupFile = new File(notePath + ".json");
        File noteContentFile = new File(notePath + ".txt");
        try {
            note.writeBackupFile();
            note.writeContentFile(content);
        } catch (IOException e) {
            Log.create(e.getMessage(), "Note.createNote()", "error", e);
            note.deleteNote(noteRepository, noteCategoryRepository);
            throw e;
        }
        return note;
    }



    public void renameNote(String newName, NoteRepository repository) {
        this.name = newName;
        repository.save(this);
    }



    public void deleteNote(NoteRepository noteRepository, NoteCategoryRepository noteCategoryRepository) {

        //delete backup and content files
        String notePath = category.findPath() + "note_" + this.getId();
        File noteBackupFile = new File(notePath + ".json");
        File noteContentFile = new File(notePath + ".txt");
        noteBackupFile.delete();
        noteContentFile.delete();

        //remove from category
        this.category.forgetNote(this, noteCategoryRepository);

        //delete from database
        noteRepository.delete(this);
    }



    public void writeBackupFile() throws IOException {
        String notePath = category.findPath() + "note_" + this.getId();
        File noteBackupFile = new File(notePath + ".json");
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(noteBackupFile, this);
    }


    public void writeContentFile(String content) throws IOException {
        String notePath = category.findPath() + "note_" + this.getId();
        File noteContentFile = new File(notePath + ".txt");
        FileWriter fileWriter = new FileWriter(noteContentFile);
        fileWriter.write(content);
        fileWriter.close();
    }



    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public NoteCategory getCategory() {
        return category;
    }
    public void setCategory(NoteCategory category) {
        this.category = category;
    }
}
