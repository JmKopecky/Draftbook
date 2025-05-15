package dev.jkopecky.draftbook.data.tables;


import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jkopecky.draftbook.Log;
import jakarta.persistence.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Entity
public class NoteCategory {
    //  One noteCategory serves as a container for multiple individual notes
    //  Example: noteCategory 'Character Sheets' could contain individual notes:
    //          - Harry Potter
    //          - Hermione Granger
    //          - Ron Weasley
    //          - Neville Longbottom
    //  Each of these is represented by its own file, containing the actual content.

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @ElementCollection
    public List<String> notes; //list of note ids
    private String categoryName;
    @ManyToOne
    private Work work;




    public String findPath() {
        return work.getPath() + "notes/" + id + "/";
    }



    public static NoteCategory create(NoteCategoryRepository noteCategoryRepository, Work work, String categoryName) throws IOException {
        //create category
        NoteCategory category = new NoteCategory();
        category.setWork(work);
        category.setNotes(new ArrayList<>());
        category.setCategoryName(categoryName);
        noteCategoryRepository.save(category);

        //todo implement automatic refresh of data from backup files if a recovery variable/setting is enabled

        try {
            String backupFilePath = category.findPath();
            Files.createDirectories(Paths.get(backupFilePath));
            backupFilePath += "category_" + category.getId() + ".json";
            File rootFile = new File(backupFilePath);
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(rootFile, category);
        } catch (IOException e) {
            Log.create(e.getMessage(), "NoteCategory.create() {unspecified log 2}", "error", e);
            noteCategoryRepository.delete(category);
            throw e;
        }

        return category;
    }


    public boolean delete(NoteRepository noteRepository, NoteCategoryRepository noteCategoryRepository) {
        //delete
        for (String noteid : this.getNotes()) {
            try {
                Note note = noteRepository.findById(Integer.parseInt(noteid)).get();
                note.deleteNote(noteRepository, noteCategoryRepository);
            } catch (NumberFormatException e) {
                Log.create("Attempted to delete note " + noteid + ", but it cannot be converted to an id",
                        "NoteController.deleteCategory()", "info", null);
                return false;
            } catch (NoSuchElementException e) {
                Log.create("Attempted to resolve note " + noteid + ", but it does not exist",
                        "NoteController.deleteCategory()", "info", null);
                return false;
            }
        }
        new File(this.findPath() + ".json").delete();
        new File(this.findPath()).delete();
        noteCategoryRepository.delete(this);
        return true;
    }



    public void learnNote(Note note, NoteCategoryRepository noteCategoryRepository) {
        if (!notes.contains("" + note.getId())) {
            notes.addLast("" + note.getId());
        }
        noteCategoryRepository.save(this);
    }


    public void forgetNote(Note note, NoteCategoryRepository noteCategoryRepository) {
        if (!notes.contains("" + note.getId())) {
            notes.remove("" + note.getId());
        }
        noteCategoryRepository.save(this);
    }


    public void rename(String newName, NoteCategoryRepository noteCategoryRepository) {
        setCategoryName(newName);
        noteCategoryRepository.save(this);
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public List<String> getNotes() {
        return notes;
    }

    public void setNotes(List<String> notes) {
        this.notes = notes;
    }

    public Work getWork() {
        return work;
    }

    public void setWork(Work work) {
        this.work = work;
    }
}
