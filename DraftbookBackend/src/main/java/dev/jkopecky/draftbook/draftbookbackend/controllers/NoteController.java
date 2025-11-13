package dev.jkopecky.draftbook.draftbookbackend.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jkopecky.draftbook.draftbookbackend.data.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Controller
@RequestMapping("/api/notes")
public class NoteController {

    //database accessors
    private final ChapterRepository chapterRepository;
    private final AccountRepository accountRepository;
    private final WorkRepository workRepository;
    private final NoteRepository noteRepository;
    private final NoteCategoryRepository noteCategoryRepository;
    public NoteController(ChapterRepository chapterRepository,
                             AccountRepository accountRepository,
                             WorkRepository workRepository,
                             NoteRepository noteRepository,
                             NoteCategoryRepository noteCategoryRepository) {
        this.chapterRepository = chapterRepository;
        this.accountRepository = accountRepository;
        this.workRepository = workRepository;
        this.noteRepository = noteRepository;
        this.noteCategoryRepository = noteCategoryRepository;
    }

    /**
     * List all NoteCategories associated with a work.
     * @param user The user who must own the work.
     * @param body The json body, including a workID.
     * @return A list of NoteCategories associated with the target work.
     */
    @PostMapping("/list")
    public ResponseEntity<List<NoteCategory>> list(
            @AuthenticationPrincipal Jwt user, @RequestBody String body) {
        Account account = Account.getOrCreateAccount(user.getSubject(), accountRepository);

        //get the target work
        Object[] workContainer = Work.getWorkIfAllowed(body, account, workRepository);
        if (workContainer[0] == null) {
            return new ResponseEntity<>((HttpStatusCode) workContainer[1]);
        }

        Work work = (Work) workContainer[0];
        List<NoteCategory> categories = noteCategoryRepository.findAllByWorkId(work.getId());
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    /**
     * Return the structure of the notes for a work.
     * @param user The user who must own the work.
     * @param body The json body, including a workID.
     * @return A list of NoteIdContainers, each representing a NoteCategory.
     */
    @PostMapping("/structure")
    public ResponseEntity<List<NoteCategoryIdentifier>> listStructure(
            @AuthenticationPrincipal Jwt user, @RequestBody String body) {
        Account account = Account.getOrCreateAccount(user.getSubject(), accountRepository);

        //get the target work
        Object[] workContainer = Work.getWorkIfAllowed(body, account, workRepository);
        if (workContainer[0] == null) {
            return new ResponseEntity<>((HttpStatusCode) workContainer[1]);
        }

        Work work = (Work) workContainer[0];

        ArrayList<NoteCategoryIdentifier> noteCategoryIdentifiers = new ArrayList<>();
        noteCategoryRepository.findAllByWorkId(work.getId()).forEach(noteCategory ->
                noteCategoryIdentifiers.add(
                        new NoteCategoryIdentifier(noteCategory, noteRepository)));

        return new ResponseEntity<>(noteCategoryIdentifiers, HttpStatus.OK);
    }

    /**
     * Create a new note category.
     * @param user The user who must own the work.
     * @param body The json body, including a workID and a category_name
     * @return The NoteCategory created, if applicable.
     */
    @PostMapping("/category/create")
    public ResponseEntity<NoteCategory> createNoteCategory(
            @AuthenticationPrincipal Jwt user, @RequestBody String body) {
        Account account = Account.getOrCreateAccount(user.getSubject(), accountRepository);

        //get the target work
        Object[] workContainer = Work.getWorkIfAllowed(body, account, workRepository);
        if (workContainer[0] == null) {
            return new ResponseEntity<>((HttpStatusCode) workContainer[1]);
        }

        Work work = (Work) workContainer[0];

        //get other request data
        String categoryName;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(body);
            categoryName = node.get("category_name").asText();
        } catch (JsonProcessingException | NullPointerException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }

        NoteCategory noteCategory = new NoteCategory(work, categoryName, noteCategoryRepository);
        return new ResponseEntity<>(noteCategory, HttpStatus.OK);
    }

    /**
     * Renames an existing noteCategory
     * @param user The user who must own the work.
     * @param body The json body, including a WorkID, CategoryID and a category_name
     * @return The NoteCategory renamed, if applicable.
     */
    @PostMapping("/category/rename")
    public ResponseEntity<NoteCategory> renameCategory(
            @AuthenticationPrincipal Jwt user, @RequestBody String body) {
        Account account = Account.getOrCreateAccount(user.getSubject(), accountRepository);

        //get the target work and NoteCategory
        Object[] container = NoteCategory.getCategoryIfAllowed(
                body, account, workRepository, noteCategoryRepository);
        if (container[1] == null || container[2] == null) {
            return new ResponseEntity<>((HttpStatusCode) container[0]);
        }

        Work work = (Work) container[1];
        NoteCategory noteCategory = (NoteCategory) container[2];

        //get other request data
        String categoryName;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(body);
            categoryName = node.get("category_name").asText();
        } catch (JsonProcessingException | NullPointerException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }

        boolean result = noteCategory.setName(categoryName, noteCategoryRepository);
        if (result) {
            return new ResponseEntity<>(noteCategory, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    /**
     * Deletes an existing noteCategory
     * @param user The user who must own the work.
     * @param body The json body, including a WorkID and a CategoryId
     * @return The result of the deletion attempt.
     */
    @PostMapping("/category/delete")
    public ResponseEntity<NoteCategory> deleteCategory(
            @AuthenticationPrincipal Jwt user, @RequestBody String body) {
        Account account = Account.getOrCreateAccount(user.getSubject(), accountRepository);

        //get the target work and NoteCategory
        Object[] container = NoteCategory.getCategoryIfAllowed(
                body, account, workRepository, noteCategoryRepository);
        if (container[1] == null || container[2] == null) {
            return new ResponseEntity<>((HttpStatusCode) container[0]);
        }

        Work work = (Work) container[1];
        NoteCategory noteCategory = (NoteCategory) container[2];

        boolean result = noteCategory.delete(noteCategoryRepository, noteRepository);
        //if the operation succeeded
        if (result) {
            return new ResponseEntity<>(noteCategory, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    /**
     * Creates a new note under the given category.
     * @param user The user who must own the work of the category.
     * @param body The request body, containing work_id, category_id, and note_name.
     * @return A String representing the result.
     */
    @PostMapping("/create")
    public ResponseEntity<String> createNote(
            @AuthenticationPrincipal Jwt user, @RequestBody String body) {
        Account account = Account.getOrCreateAccount(user.getSubject(), accountRepository);

        //get the target work and NoteCategory
        Object[] container = NoteCategory.getCategoryIfAllowed(
                body, account, workRepository, noteCategoryRepository);
        if (container[1] == null || container[2] == null) {
            return new ResponseEntity<>((HttpStatusCode) container[0]);
        }

        Work work = (Work) container[1];
        NoteCategory noteCategory = (NoteCategory) container[2];

        //get other request data
        String noteName;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(body);
            noteName = node.get("note_name").asText();
        } catch (JsonProcessingException | NullPointerException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }

        new Note(noteName, noteCategory, noteRepository);

        return new ResponseEntity<>("Success",  HttpStatus.OK);
    }

    /**
     * Creates a new note under the given category.
     * @param user The user who must own the work of the category.
     * @param body The request body, containing work_id and note_name.
     * @return A String representing the result.
     */
    @PostMapping("/delete")
    public ResponseEntity<String> deleteNote(
            @AuthenticationPrincipal Jwt user, @RequestBody String body) {
        Account account = Account.getOrCreateAccount(user.getSubject(), accountRepository);

        //get the target work and NoteCategory
        Object[] container = Note.getNoteIfAllowed(
                body, account, workRepository, noteRepository);
        if (container[1] == null || container[2] == null) {
            return new ResponseEntity<>((HttpStatusCode) container[0]);
        }

        Work work = (Work) container[1];
        Note note = (Note) container[2];

        if (note.isChapterNote()) {
            return new ResponseEntity<>("Not allowed",  HttpStatus.CONFLICT);
        }

        note.delete(noteRepository);

        return new ResponseEntity<>("Success",  HttpStatus.OK);
    }

    /**
     * Renames an existing note.
     * @param user The user who must own the work of the category.
     * @param body The request body, containing work_id, category_id, and note_name.
     * @return A String representing the result.
     */
    @PostMapping("/rename")
    public ResponseEntity<String> renameNote(
            @AuthenticationPrincipal Jwt user, @RequestBody String body) {
        Account account = Account.getOrCreateAccount(user.getSubject(), accountRepository);

        //get the target work and NoteCategory
        Object[] container = Note.getNoteIfAllowed(
                body, account, workRepository, noteRepository);
        if (container[1] == null || container[2] == null) {
            return new ResponseEntity<>((HttpStatusCode) container[0]);
        }

        Note note = (Note) container[2];

        //get other request data
        String noteName;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(body);
            noteName = node.get("note_name").asText();
        } catch (JsonProcessingException | NullPointerException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }

        //check that this note is allowed to be renamed.
        if (note.isChapterNote()) {
            return new ResponseEntity<>("Not allowed",  HttpStatus.CONFLICT);
        }

        //make the name change
        note.setDescriptor(noteName);
        noteRepository.save(note);

        return new ResponseEntity<>("Success",  HttpStatus.OK);
    }

    /**
     * Get the content of a note.
     * @param user The user who must own the work of the category.
     * @param body The request body, containing work_id and note_id.
     * @return A responseEntity containing the html content of the note.
     */
    @PostMapping("/get/content")
    public ResponseEntity<String> getNoteContent(
            @AuthenticationPrincipal Jwt user, @RequestBody String body) {
        Account account = Account.getOrCreateAccount(user.getSubject(), accountRepository);

        //get the target work and NoteCategory
        Object[] container = Note.getNoteIfAllowed(
                body, account, workRepository, noteRepository);
        if (container[1] == null || container[2] == null) {
            return new ResponseEntity<>((HttpStatusCode) container[0]);
        }

        Note note = (Note) container[2];

        return new  ResponseEntity<>(note.getContent(), HttpStatus.OK);
    }

    /**
     * Saves the HTML content to the note.
     * @param user The user who must own the work of the category.
     * @param body The request body, containing work_id, note_id, and note_content
     * @return A String representing the result.
     */
    @PostMapping("/save/html")
    public ResponseEntity<String> saveNoteHTML(
            @AuthenticationPrincipal Jwt user, @RequestBody String body) {
        Account account = Account.getOrCreateAccount(user.getSubject(), accountRepository);

        //get the target work and NoteCategory
        Object[] container = Note.getNoteIfAllowed(
                body, account, workRepository, noteRepository);
        if (container[1] == null || container[2] == null) {
            return new ResponseEntity<>((HttpStatusCode) container[0]);
        }

        Note note = (Note) container[2];

        //get other request data
        String noteContent;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(body);
            noteContent = node.get("note_content").asText();
        } catch (JsonProcessingException | NullPointerException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }

        //make the name change
        note.setContent(noteContent);
        noteRepository.save(note);

        return new ResponseEntity<>("Success",  HttpStatus.OK);
    }
}
