package dev.jkopecky.draftbook.draftbookbackend.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import org.springframework.http.HttpStatus;

import java.util.NoSuchElementException;

@Entity
public class Chapter implements Comparable<Chapter> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String title;
    private int number;
    private int workId;
    String content;
    @OneToOne(optional = true)
    Note note;


    /**
     * Create a chapter with provided parameters.
     * @param chapterName The name of the chapter to create.
     * @param chapterNumber The location in the work of the chapter to create.
     * @param work The work associated with this chapter.
     * @param chapterRepository The table in the database containing chapters.
     * @param workRepository The table in the database containing works.
     */
    public Chapter(String chapterName, int chapterNumber, Work work,
                   ChapterRepository chapterRepository, WorkRepository workRepository) {
        this.title = chapterName;
        this.workId = work.getId();
        this.content = "";
        this.note = new Note();
        this.number = work.boundChapterNumber(chapterNumber);
        work.insertChapter(this, workRepository, chapterRepository);
    }

    public Chapter() {}

    /**
     * Delete this chapter.
     * @param chapterRepository The chapter table in the database.
     * @param noteRepository The note table in the database.
     */
    public void deleteChapter(ChapterRepository chapterRepository, NoteRepository noteRepository) {
        //delete the associated note first if it exists
        if (note != null) {
            note.delete(noteRepository);
        }

        //delete this chapter
        chapterRepository.delete(this);
    }

    /**
     * Provide a chapter based on a request, assuming the user is permitted to access it.
     * @param requestBody The request from which to identify the work.
     * @param workRepository The table in the database containing works.
     * @param chapterRepository The table in the database containing chapters.
     * @param account The account which should own the work in question.
     * @return An array of Objects, where the first element is either a chapter or null. If null, the second element is an HttpStatusCode describing why.
     */
    public static Object[] getChapterIfAllowed(
            String requestBody, Account account, WorkRepository workRepository, ChapterRepository chapterRepository) {

        //get work and check that it exists
        Object[] result = Work.getWorkIfAllowed(requestBody, account, workRepository);
        Work work;
        if (result[0] == null) {
            return result;
        } else {
            work = (Work) result[0];
            result[0] = null; //reset result to make room for a chapter
        }

        //get the chapter
        Chapter chapter;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(requestBody);
            int chapterId = node.get("chapter_id").asInt();
            chapter = chapterRepository.findById(chapterId).get();
        } catch (JsonProcessingException e) {
            result[1] = HttpStatus.BAD_REQUEST;
            return result;
        } catch (NoSuchElementException e) {
            result[1] = HttpStatus.NOT_FOUND;
            return result;
        }

        //check that the chapter is associated with the work
        for (Chapter toCheck : work.getChapters(chapterRepository)) {
            if (toCheck.getId() == chapter.getId()) {
                result[0] = chapter;
                result[1] = HttpStatus.OK;
                return result;
            }
        }

        result[1] = HttpStatus.UNAUTHORIZED;
        return result;
    }

    /**
     * Sort chapters in ascending order by chapter number (lower chapters first).
     * @param other The other chapter to compare against.
     * @return A positive int if this chapter should come before the other, negative if it should not.
     */
    @Override
    public int compareTo(Chapter other) {
        return this.number - other.number;
    }

    //getters and setters
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public int getWorkId() {
        return workId;
    }
    public void setWorkId(int workId) {
        this.workId = workId;
    }
    public int getNumber() {
        return number;
    }
    public void setNumber(int number) {
        this.number = number;
    }
}
