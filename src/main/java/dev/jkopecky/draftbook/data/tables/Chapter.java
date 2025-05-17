package dev.jkopecky.draftbook.data.tables;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jkopecky.draftbook.Log;
import dev.jkopecky.draftbook.data.Util;
import jakarta.persistence.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

@Entity
public class Chapter implements Comparable<Chapter> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String title;
    private Integer number;
    @ManyToOne
    private Work work;
    private String path;



    @Override
    public int compareTo(Chapter o) {
        return this.number.compareTo(o.number);
    }



    public void buildPath() {
        path = work.getPath() + "chapters/" + id + "/";
    }



    public String retrieveAsHTML() throws IOException {
        try {
            String fullPath = path + "chapter_" + id + ".txt";
            File file = new File(fullPath);
            return Files.readString(file.toPath());
        } catch (IOException e) {
            Log.create(e.getMessage(), "Chapter.retrieveAsHTML()", "error", e);
            throw e;
        }
    }



    public void writeHTML(String html) throws IOException {
        try {
            String fullPath = path + "chapter_" + id + ".txt";
            File file = new File(fullPath);
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(html);
            fileWriter.close();
        } catch (IOException e) { //if file does not exist or cannot be created, or if file cannot be written to
            Log.create(e.getMessage(), "Chapter.writeHTML()", "error", e);
            throw e;
        }
    }



    public void writeNotes(String notes) throws IOException {
        try {
            String fullPath = path + "note_" + id + ".txt";
            File file = new File(fullPath);
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(notes);
            fileWriter.close();
        } catch (IOException e) { //if file does not exist or cannot be created, or if file cannot be written to
            Log.create(e.getMessage(), "Chapter.writeNotes()", "error", e);
            throw e;
        }
    }



    public String readNotes() throws IOException {
        try {
            String fullPath = path + "note_" + id + ".txt";
            File file = new File(fullPath);
            return Files.readString(file.toPath());
        } catch (IOException e) {
            Log.create(e.getMessage(), "Chapter.readNotes()", "error", e);
            throw e;
        }
    }



    public String toResource() {
        return Util.toInternalResource(getTitle());
    }



    public boolean delete(ChapterRepository chapterRepository) {
        try {
            Util.recursiveDeleteFiles(path);
            chapterRepository.delete(this);
        } catch (Exception e) {
            Log.create(e.getMessage(), "Chapter.delete()", "error", e);
            return false;
        }
        return true;
    }



    public boolean rename(String name, ChapterRepository chapterRepository) {
        //write to data backup file
        String oldTitle = title;
        title = name;
        ObjectMapper mapper = new ObjectMapper();
        File newDataBackup = new File(path + "chapter_" + id + ".json");
        try {
            mapper.writeValue(newDataBackup, this);
        } catch (IOException e) {
            Log.create("Error while transferring backup content to new file", "Chapter.rename()", "info", null);
            title = oldTitle;
            return false;
        }
        chapterRepository.save(this);
        return true;
    }



    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public Work getWork() {
        return work;
    }
    public void setWork(Work work) {
        this.work = work;
    }
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }


}
