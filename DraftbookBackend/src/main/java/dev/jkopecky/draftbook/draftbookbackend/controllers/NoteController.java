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
}
