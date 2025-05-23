package dev.jkopecky.draftbook.data.tables;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jkopecky.draftbook.DraftbookApplication;
import dev.jkopecky.draftbook.Log;
import dev.jkopecky.draftbook.data.Util;
import jakarta.persistence.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;

@Entity
public class Work {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String title;
    private String path;
    @ManyToOne
    private Account account;


    public static Work createWork(Account owner, String title, WorkRepository workRepository) throws IOException {
        Work work = new Work();
        work.setTitle(title);
        work.setAccount(owner);
        workRepository.save(work);
        work.buildPath();
        work.createWorkFile();
        workRepository.save(work);
        return work;
    }



    public void buildPath() {
        //directory should be the user's default root, in a folder based on their user id. Changing the username or work name should force a refactor after checking.
        path = DraftbookApplication.retrieveRoot() + account.getId() + "/works/" + id + "/";
        System.out.println(path);
    }



    public void createWorkFile() throws IOException {
        try {
            //create initial file based on id, and save the work object for data redundancy.
            ObjectMapper mapper = new ObjectMapper();
            String fullPath = path + id + ".json";
            Files.createDirectories(Paths.get(path));
            File file = new File(fullPath);
            mapper.writeValue(file, this);
        } catch (IOException e) {
            Log.create(e.getMessage(), "Work.createWorkFile()", "error", e);
            throw e;
        }
    }



    public Chapter createChapter(String title, int index, ChapterRepository chapterRepository) throws IOException {
        //retrieve all chapters and determine the location to add the new one.
        ArrayList<Chapter> chapters = getChapters(chapterRepository);

        if (index > chapters.size()) { //if index is too large, clamp to adding a chapter to the end
            index = chapters.size() + 1;
        }

        //create initial chapter object
        Chapter chapter = new Chapter();
        chapter.setTitle(title);
        chapter.setWork(this);
        chapter.setNumber(index);
        chapterRepository.save(chapter);
        chapter.buildPath();

        //create files for chapters
        ObjectMapper mapper = new ObjectMapper();

        //save chapter object file
        try {
            String path = chapter.getPath() + "chapter_" + chapter.getId() + ".json";
            Files.createDirectories(Paths.get(chapter.getPath()));
            File file = new File(path);
            mapper.writeValue(file, chapter);
        } catch (IOException e) {
            String message = "Failed to create chapter data copy.";
            String source = "Work.createChapter()";
            Log.create(message, source, "warn", null);
            throw new IOException("Warn in " + source + ": " + message);
        }

        //create file for the chapter's body
        try {
            String path = chapter.getPath() + "chapter_" + chapter.getId() + ".txt";
            Files.createDirectories(Paths.get(chapter.getPath()));
            File file = new File(path);
            file.createNewFile();
        } catch (IOException e) {
            String message = "Failed to create chapter content file.";
            String source = "Work.createChapter()";
            Log.create(message, source, "warn", null);
            throw new IOException("Warn in " + source + ": " + message);
        }

        //create file for chapter-specific notes
        try {
            String path = chapter.getPath() + "note_" + chapter.getId() + ".txt";
            Files.createDirectories(Paths.get(chapter.getPath()));
            File file = new File(path);
            file.createNewFile();
        } catch (IOException e) {
            String message = "Failed to create chapter note file.";
            String source = "Work.createChapter()";
            Log.create(message, source, "warn", null);
            throw new IOException("Warn in " + source + ": " + message);
        }

        chapterRepository.save(chapter);

        //log success
        String chapterConfirmationLog = "Created new chapter: ";
        chapterConfirmationLog += "\n\t - Chapter: " + chapter.getTitle();
        chapterConfirmationLog += "\n\t - Work: " + this.getTitle();
        chapterConfirmationLog += "\n\t - Account: " + this.getAccount().getEmail();
        Log.create(chapterConfirmationLog, "Work.createChapter", "debug", null);

        return chapter;
    }



    public ArrayList<Chapter> getChapters(ChapterRepository chapterRepository) {
        ArrayList<Chapter> output = new ArrayList<>(chapterRepository.findByWork_Id(this.getId()));
        Collections.sort(output); //sort output in increasing order by number.
        return output;
    }



    public String toResource() {
        return "workid_" + id;
    }



    public boolean changeName(String newName, WorkRepository workRepository) {
        String oldName = title; //save old title in case of error

        try {
            title = newName;
            createWorkFile(); //refresh stored copy
            workRepository.save(this); //save changes to database
            return true;
        } catch (IOException e) {
            title = oldName;
            try {
                createWorkFile();
                workRepository.save(this);
            } catch (IOException e2) {
                Log.create(e.getMessage(), "Work.changeName()", "error", e);
                return false;
            }
            Log.create("Failed to change work name from " + oldName + " to " + newName, "Work.changeName()", "info", null);
            return false;
        }
    }



    public boolean delete(WorkRepository workRepository) {
        try {
            Util.recursiveDeleteFiles(this.getPath());
            workRepository.delete(this);
        } catch (IOException e) {
            Log.create("Failed to delete work: " + e.getMessage(), "Work.delete()", "error", e);
            return false;
        }
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
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public Account getAccount() {
        return account;
    }
    public void setAccount(Account account) {
        this.account = account;
    }
}
