package dev.jkopecky.draftbook.draftbookbackend.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.NoSuchElementException;

@Entity
public class NoteCategory {

    public static final String CHAPTER_CATEGORY_NAME = "Chapter Notes";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private int workId;
    private String name;

    public NoteCategory() {}

    /**
     * Creates a new noteCategory.
     * @param work The work that owns the category.
     * @param name The display name of the category.
     * @param noteCategoryRepository The table containing noteCategories.
     */
    public NoteCategory(Work work, String name, NoteCategoryRepository noteCategoryRepository) {
        this.workId = work.getId();
        this.name = name;
        noteCategoryRepository.save(this);
    }

    /**
     * Gets the preset chapter noteCategory for a given work.
     * @param noteCategoryRepository The table containing noteCategories.
     * @param work The work that should own the noteCategory.
     * @return A noteCategory explicitly for chapter notes.
     */
    public static NoteCategory getChapterCategory(NoteCategoryRepository noteCategoryRepository, Work work) {
        List<NoteCategory> matches = noteCategoryRepository.findAllByWorkIdAndName(
                work.getId(), CHAPTER_CATEGORY_NAME);
        if (matches.isEmpty()) {
            return new NoteCategory(work, CHAPTER_CATEGORY_NAME, noteCategoryRepository);
        } else {
            return matches.getFirst();
        }
    }

    /**
     * Util method to retrieve all notes from this noteCategory.
     * @param noteRepository The table containing notes.
     * @return A list of Note objects.
     */
    public List<Note> getNotes(NoteRepository noteRepository) {
        return noteRepository.findByNoteCategory(this);
    }

    /**
     * Provide a category based on a request, assuming the user is permitted to access it.
     * @param requestBody The request from which to identify the work.
     * @param workRepository The table in the database containing works.
     * @param noteCategoryRepository The table in the database containing categories.
     * @param account The account which should own the work in question.
     * @return An array of Objects, where the first element is a HttpStatusCode, the second is a work, and the third is a noteCategory.
     */
    public static Object[] getCategoryIfAllowed(
            String requestBody, Account account, WorkRepository workRepository, NoteCategoryRepository noteCategoryRepository) {

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

        //get the noteCategory
        NoteCategory category;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(requestBody);
            int categoryId = node.get("category_id").asInt();
            category = noteCategoryRepository.findById(categoryId).get();
        } catch (JsonProcessingException | NullPointerException e) {
            result[0] = HttpStatus.BAD_REQUEST;
            return result;
        } catch (NoSuchElementException e) {
            result[0] = HttpStatus.NOT_FOUND;
            return result;
        }

        //check that the category is for the work
        if (category.getWorkId() == work.getId()) {
            result[2] = category;
            return result;
        }

        result[0] = HttpStatus.UNAUTHORIZED;
        result[2] = null;
        return result;
    }

    /**
     * Attempts to delete a NoteCategory.
     * @param noteCategoryRepository The table containing noteCategories.
     * @param noteRepository The table containing notes.
     * @return True if deletion was successful, false otherwise.
     * False might occur if the noteCategory is for the chapters of a work.
     */
    public boolean delete(NoteCategoryRepository noteCategoryRepository, NoteRepository noteRepository) {
        if (name.equals(CHAPTER_CATEGORY_NAME)) {
            return false;
        }
        getNotes(noteRepository).forEach(note -> note.delete(noteRepository));
        noteCategoryRepository.delete(this);
        return true;
    }

    /**
     * Forces the deletion of this noteCategory, regardless of whether this is for chapter notes.
     * @param noteCategoryRepository The table containing noteCategories.
     * @param noteRepository The table containing notes.
     */
    public void forceDelete(NoteCategoryRepository noteCategoryRepository, NoteRepository noteRepository) {
        getNotes(noteRepository).forEach(note -> note.delete(noteRepository));
        noteCategoryRepository.delete(this);
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getWorkId() {
        return workId;
    }

    public void setWorkId(int workId) {
        this.workId = workId;
    }

    public String getName() {
        return name;
    }

    /**
     * Renames the current work.
     * @param name The new name to set.
     * @param noteCategoryRepository The table containing noteCategories.
     * @return True if successfully renamed. False if not, which can occur if
     * either this category is the work chapter category or if
     * the name conflicts with the work chapter category.
     */
    public boolean setName(String name, NoteCategoryRepository noteCategoryRepository) {
        if (name.equals(CHAPTER_CATEGORY_NAME) || this.name.equals(CHAPTER_CATEGORY_NAME)) {
            return false;
        }
        this.name = name;
        noteCategoryRepository.save(this);
        return true;
    }
}
