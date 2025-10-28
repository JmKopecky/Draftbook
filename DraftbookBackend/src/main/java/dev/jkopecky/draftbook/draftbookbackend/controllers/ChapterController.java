package dev.jkopecky.draftbook.draftbookbackend.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jkopecky.draftbook.draftbookbackend.data.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.NoSuchElementException;

@Controller
@RequestMapping("/api/chapters")
public class ChapterController {

    //database accessors
    private final ChapterRepository chapterRepository;
    private final AccountRepository accountRepository;
    private final WorkRepository workRepository;
    private final NoteRepository noteRepository;
    public ChapterController(ChapterRepository chapterRepository,
                             AccountRepository accountRepository,
                             WorkRepository workRepository,
                             NoteRepository noteRepository) {
        this.chapterRepository = chapterRepository;
        this.accountRepository = accountRepository;
        this.workRepository = workRepository;
        this.noteRepository = noteRepository;
    }

    /**
     * List all chapters associated with a given work.
     * @param user The user who must own the work in question.
     * @param body The request body, containing a work id.
     * @return A list of chapters for the work.
     */
    @GetMapping("/list")
    public ResponseEntity<List<Chapter>> listChapters(
            @AuthenticationPrincipal Jwt user, @RequestBody String body) {
        Account account = Account.getOrCreateAccount(user.getSubject(), accountRepository);

        //get the target work
        Object[] workContainer = Work.getWorkIfAllowed(body, account, workRepository);
        if (workContainer[0] == null) {
            return new ResponseEntity<>(null, (HttpStatusCode) workContainer[1]);
        }
        Work work = (Work) workContainer[0];

        return new ResponseEntity<>(work.getChapters(chapterRepository), HttpStatus.OK);
    }

    /**
     * Get a specific chapter.
     * @param user The user who must own the work for this chapter.
     * @param body The request body, containing a work and chapter id.
     * @return An object representing the chapter.
     */
    @GetMapping("/get")
    public ResponseEntity<Chapter> getChapter(
            @AuthenticationPrincipal Jwt user, @RequestBody String body) {
        Account account = Account.getOrCreateAccount(user.getSubject(), accountRepository);

        //get the target chapter
        Object[] chapterContainer = Chapter.getChapterIfAllowed(body, account, workRepository, chapterRepository);
        if (chapterContainer[0] == null) {
            return new ResponseEntity<>(null, (HttpStatusCode) chapterContainer[1]);
        }
        Chapter chapter = (Chapter) chapterContainer[0];

        return new ResponseEntity<>(chapter, HttpStatus.OK);
    }

    /**
     * Create a new chapter.
     * @param user The user owning the chapter.
     * @param body The request body, including the work_id, chapter_name, and chapter_number.
     * @return A ResponseEntity that may contain a Chapter object representing the new chapter.
     */
    @PostMapping("/create")
    public ResponseEntity<Chapter> createChapter(
            @AuthenticationPrincipal Jwt user, @RequestBody String body) {
        Account account = Account.getOrCreateAccount(user.getSubject(), accountRepository);

        //get the target work
        Object[] workContainer = Work.getWorkIfAllowed(body, account, workRepository);
        if (workContainer[0] == null) {
            return new ResponseEntity<>(null, (HttpStatusCode) workContainer[1]);
        }
        Work work = (Work) workContainer[0];

        //get request data about the chapter to create
        String chapterName;
        int chapterNumber;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(body);
            chapterName = node.get("chapter_name").asText();
            chapterNumber = node.get("chapter_number").asInt();
        } catch (JsonProcessingException | NullPointerException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }

        Chapter chapter = new Chapter(chapterName, chapterNumber, work,
                chapterRepository, workRepository);

        return new ResponseEntity<>(chapter, HttpStatus.CREATED);
    }

    /**
     * Delete the target chapter.
     * @param user The user who must own the chapter.
     * @param body The body of the request, containing work_id and chapter_id.
     * @return An HttpStatusCode indicating the result.
     */
    @PostMapping("/delete")
    public ResponseEntity<String> deleteChapter(
            @AuthenticationPrincipal Jwt user, @RequestBody String body) {
        Account account = Account.getOrCreateAccount(user.getSubject(), accountRepository);

        //get the target chapter
        Object[] chapterContainer = Chapter.getChapterIfAllowed(body, account, workRepository, chapterRepository);
        if (chapterContainer[0] == null) {
            return new ResponseEntity<>("Failed.", (HttpStatusCode) chapterContainer[1]);
        }
        Chapter chapter = (Chapter) chapterContainer[0];

        chapter.deleteChapter(chapterRepository, noteRepository);

        return new ResponseEntity<>("Success.", HttpStatus.OK);
    }
}
