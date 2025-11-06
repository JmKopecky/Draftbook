package dev.jkopecky.draftbook.draftbookbackend.data;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.List;

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

    public void setName(String name) {
        this.name = name;
    }
}
