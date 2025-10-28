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
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Controller
@RequestMapping("/api/works")
public class WorkController {

    //get database repositories
    private final ChapterRepository chapterRepository;
    private final AccountRepository accountRepository;
    private final WorkRepository workRepository;
    private final NoteRepository noteRepository;
    public WorkController(AccountRepository accountRepository, WorkRepository workRepository, NoteRepository noteRepository, ChapterRepository chapterRepository) {
        this.accountRepository = accountRepository;
        this.workRepository = workRepository;
        this.noteRepository = noteRepository;
        this.chapterRepository = chapterRepository;
    }

    /**
     * Return a list of works that the user owns.
     * @param user The account calling this mapping.
     * @return A response entity containing a list of works.
     */
    @GetMapping("/list")
    public ResponseEntity<List<Work>> getWorkList(@AuthenticationPrincipal Jwt user) {
        Account account = Account.getOrCreateAccount(user.getSubject(), accountRepository);

        //get all works for that account
        ArrayList<Work> works = new ArrayList<>();
        works.addAll(workRepository.findAllByAccountId(account.getId()));

        return new ResponseEntity<>(works, HttpStatus.OK);
    }

    /**
     * Return a specific work that the user owns.
     * @param user The account calling this mapping.
     * @param body The request body, containing one element "workid" that must be an integer corresponding to the id of a work.
     * @return A ResponseEntity that may contain the target work.
     */
    @GetMapping("/get")
    public ResponseEntity<Work> getWork(
            @AuthenticationPrincipal Jwt user, @RequestBody String body) {

        Account account = Account.getOrCreateAccount(user.getSubject(), accountRepository);

        //get the target work
        Object[] workContainer = Work.getWorkIfAllowed(body, account, workRepository);
        if (workContainer[0] == null) {
            return new ResponseEntity<>((HttpStatusCode) workContainer[1]);
        }

        return new ResponseEntity<>((Work) workContainer[0], HttpStatus.OK);
    }

    /**
     * Create a work owned by the specified user.
     * @param user The user to own this work.
     * @param body Metadata about the work to create. Includes the name of the work.
     * @return A ResponseEntity that may contain the resulting Work object.
     */
    @PostMapping("/create")
    public ResponseEntity<Work> createWork(
            @AuthenticationPrincipal Jwt user, @RequestBody String body) {

        Account account = Account.getOrCreateAccount(user.getSubject(), accountRepository);

        //get request data about the work to create
        String workName;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(body);
            workName = node.get("work_name").asText();
        } catch (JsonProcessingException | NullPointerException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }

        //create and return the work
        Work work = new Work(workName, account, workRepository);
        return new ResponseEntity<>(work, HttpStatus.CREATED);
    }

    /**
     * Delete a work.
     * @param user The user who must own the target work.
     * @param body The request body, containing the work id.
     * @return An HttpStatusCode representing the result of this operation.
     */
    @PostMapping("/delete")
    public ResponseEntity<String> deleteWork(
            @AuthenticationPrincipal Jwt user, @RequestBody String body) {

        Account account = Account.getOrCreateAccount(user.getSubject(), accountRepository);

        //get the target work
        Object[] workContainer = Work.getWorkIfAllowed(body, account, workRepository);
        if (workContainer[0] == null) {
            return new ResponseEntity<>("Failed to retrieve work", (HttpStatusCode) workContainer[1]);
        }

        ((Work) workContainer[0]).deleteWork(workRepository, chapterRepository, noteRepository);
        return new ResponseEntity<>("Success", HttpStatus.OK);
    }

    /**
     * Update the work with a new title and subtitle.
     * @param user The user who must own the target work.
     * @param body The request body, containing the work title and subtitle.
     * @return A ResponseEntity that may contain the resulting work.
     */
    @PostMapping("/update")
    public ResponseEntity<Work> updateWork(
            @AuthenticationPrincipal Jwt user, @RequestBody String body) {

        Account account = Account.getOrCreateAccount(user.getSubject(), accountRepository);

        //get the target work
        Object[] workContainer = Work.getWorkIfAllowed(body, account, workRepository);
        if (workContainer[0] == null) {
            return new ResponseEntity<>((HttpStatusCode) workContainer[1]);
        }
        Work work = (Work) workContainer[0];

        //get request data about the new work values.
        String workTitle;
        String workSubtitle;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(body);
            workTitle = node.get("work_title").asText();
            workSubtitle = node.get("work_subtitle").asText();
        } catch (JsonProcessingException | NullPointerException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }

        System.out.println(workTitle);
        System.out.println(workSubtitle);

        //update the work with the new data.
        work.setTitle(workTitle);
        work.setSubtitle(workSubtitle);
        workRepository.save(work);
        return new ResponseEntity<>(work, HttpStatus.OK);
    }

}
