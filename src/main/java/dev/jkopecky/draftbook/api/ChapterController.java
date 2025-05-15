package dev.jkopecky.draftbook.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jkopecky.draftbook.Log;
import dev.jkopecky.draftbook.data.tables.*;
import dev.jkopecky.draftbook.exceptions.ChapterOwnershipException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;

@Controller
@CrossOrigin
public class ChapterController {


    //all the database repositories load here, add a new entry if another table is needed for this controller
    AccountRepository accountRepository;
    WorkRepository workRepository;
    ChapterRepository chapterRepository;
    NoteCategoryRepository noteCategoryRepository;
    AuthTokenRepository authTokenRepository;
    public ChapterController(AccountRepository accountRepository, WorkRepository workRepository, ChapterRepository chapterRepository, NoteCategoryRepository noteCategoryRepository, AuthTokenRepository authTokenRepository) {
        this.accountRepository = accountRepository;
        this.workRepository = workRepository;
        this.chapterRepository = chapterRepository;
        this.noteCategoryRepository = noteCategoryRepository;
        this.authTokenRepository = authTokenRepository;
    }




    //note: inputs {chaptername, chapternumber}
    @PostMapping("/api/work/{workid}/chapter/create")
    public ResponseEntity<HashMap<String, Object>> createChapter(
            @PathVariable int workid,
            @CookieValue(value = "token", defaultValue = "null") String token,
            @RequestBody String data) {

        HashMap<String, Object> response = new HashMap<>();

        Account account = null;
        Work work;

        //retrieve response data
        String chapterName;
        int chapterNumber;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(data);
            chapterName = node.get("chaptername").asText();
            chapterNumber = Integer.parseInt(node.get("chapternumber").asText());
            //token verification if included in request body
            if (node.has("token")) {
                token = node.get("token").asText();
            }
        } catch (IOException e) {
            Log.create(e.getMessage(), "ChapterController.createChapter()", "error", e);
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        //authentication and work retrieval
        try {
            account = AuthenticationController.getByToken(token, authTokenRepository);
            work = workRepository.findById(workid).get();
            if (work.getAccount().getId().intValue() != account.getId()) {
                Log.create("Work " + workid + " is not owned by account " + account.getId(),
                        "ChapterController.createChapter()", "info", null);
                response.put("error", "Work " + workid + " is not owned by account " + account.getId());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        } catch (NoSuchElementException e) {
            Log.create("Attempted to resolve work " + workid + ", but it does not exist", "ChapterController.createChapter()", "info", null);
            response.put("error", "unrecognized_work");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            Log.create("Authentication Exception: " + e.getMessage(), "ChapterController.createChapter()", "info", e);
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }


        //create chapter
        Chapter chapter;
        try {
            chapter = work.createChapter(chapterName, chapterNumber, chapterRepository);
        } catch (IOException e) {
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        response.put("error", "none");
        response.put("chapter_id", chapter.getId());
        response.put("chapter_name", chapter.getTitle());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    //note: inputs {chaptername}
    @PostMapping("/api/work/{workid}/chapter/{chapterid}/select")
    public ResponseEntity<HashMap<String, Object>> selectChapter(
            @PathVariable int workid, @PathVariable int chapterid,
            @CookieValue(value = "token", defaultValue = "null") String token,
            @RequestBody String data) {

        HashMap<String, Object> response = new HashMap<>();

        Account account;
        Work work;

        //retrieve token if applicable
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(data);
            //token verification if included in request body
            if (node.has("token")) {
                token = node.get("token").asText();
            }
        } catch (IOException e) {
            Log.create(e.getMessage(), "ChapterController.selectChapter()", "error", e);
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        //authentication and work retrieval
        try {
            account = AuthenticationController.getByToken(token, authTokenRepository);
            work = workRepository.findById(workid).get();
            if (work.getAccount().getId().intValue() != account.getId()) {
                Log.create("Work " + workid + " is not owned by account " + account.getId(),
                        "ChapterController.selectChapter()", "info", null);
                response.put("error", "Work " + workid + " is not owned by account " + account.getId());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        } catch (NoSuchElementException e) {
            Log.create("Attempted to resolve work " + workid + ", but it does not exist",
                    "ChapterController.selectChapter()", "info", null);
            response.put("error", "unrecognized_work");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            Log.create("Authentication Exception: " + e.getMessage(),
                    "ChapterController.selectChapter()", "info", e);
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        try {
            Chapter c = chapterRepository.findById(chapterid).get();
            if (c.getWork().getId().intValue() != work.getId()) {
                throw new ChapterOwnershipException();
            }
            response.put("content", c.retrieveAsHTML());
            response.put("notes", c.readNotes());
            response.put("title", c.getTitle());
            response.put("error", "none");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            Log.create("chapter " + chapterid + " does not exist", "ChapterController.selectChapter()", "error", e);
            response.put("error", "chapter " + chapterid + " does not exist");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            Log.create(e.getMessage(), "ChapterController.selectChapter()", "error", e);
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (ChapterOwnershipException e) {
            Log.create("chapter " + chapterid + " is not owned by work " + workid, "ChapterController.selectChapter()", "info", e);
            response.put("error", "chapter " + chapterid + " is not owned by work " + workid);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }


    //note: inputs {chaptername}
    @PostMapping("/api/work/{workid}/chapter/{chapterid}/rename")
    public ResponseEntity<HashMap<String, Object>> renameChapter(
            @PathVariable int workid, @PathVariable int chapterid,
            @CookieValue(value = "token", defaultValue = "null") String token,
            @RequestBody String data) {

        HashMap<String, Object> response = new HashMap<>();

        Account account;
        Work work;

        String name;
        //retrieve token if applicable
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(data);
            name = node.get("newname").asText();
            //token verification if included in request body
            if (node.has("token")) {
                token = node.get("token").asText();
            }
        } catch (IOException e) {
            Log.create(e.getMessage(), "ChapterController.renameChapter()", "error", e);
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        //authentication and work retrieval
        try {
            account = AuthenticationController.getByToken(token, authTokenRepository);
            work = workRepository.findById(workid).get();
            if (work.getAccount().getId().intValue() != account.getId()) {
                Log.create("Work " + workid + " is not owned by account " + account.getId(),
                        "ChapterController.renameChapter()", "info", null);
                response.put("error", "Work " + workid + " is not owned by account " + account.getId());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        } catch (NoSuchElementException e) {
            Log.create("Attempted to resolve work " + workid + ", but it does not exist",
                    "ChapterController.renameChapter()", "info", null);
            response.put("error", "unrecognized_work");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            Log.create("Authentication Exception: " + e.getMessage(),
                    "ChapterController.renameChapter()", "info", e);
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }


        //find and rename chapter
        try {
            Chapter chapter = chapterRepository.findById(chapterid).get();
            if (chapter.getWork().getId().intValue() != work.getId()) {
                throw new ChapterOwnershipException();
            }
            chapter.rename(name, chapterRepository);
            response.put("error", "none");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (NoSuchElementException e) { //chapter does not exist
            Log.create("Attempted to resolve work " + workid + ", but it does not exist",
                    "ChapterController.renameChapter()", "info", null);
            response.put("error", "unrecognized_work");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (ChapterOwnershipException e) {
            Log.create("chapter " + chapterid + " is not owned by work " + workid,
                    "ChapterController.renameChapter()", "info", e);
            response.put("error", "chapter " + chapterid + " is not owned by work " + workid);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }



    //note: inputs {chaptername, content, notes}
    @PostMapping("/api/work/{workid}/chapter/{chapterid}/save")
    public ResponseEntity<HashMap<String, Object>> saveChapter(
            @PathVariable int workid, @PathVariable int chapterid,
            @CookieValue(value = "token", defaultValue = "null") String token,
            @RequestBody String data) {

        HashMap<String, Object> response = new HashMap<>();

        Account account;
        Work work;

        //retrieve response data
        String content;
        String notes;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(data);
            content = node.get("content").asText();
            notes = node.get("notes").asText();
            //token verification if included in request body
            if (node.has("token")) {
                token = node.get("token").asText();
            }
        } catch (IOException e) {
            Log.create(e.getMessage(), "ChapterController.saveChapter()", "error", e);
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }


        //authentication and work retrieval
        try {
            account = AuthenticationController.getByToken(token, authTokenRepository);
            work = workRepository.findById(workid).get();
            if (work.getAccount().getId().intValue() != account.getId()) {
                Log.create("Work " + workid + " is not owned by account " + account.getId(),
                        "ChapterController.saveChapter()", "info", null);
                response.put("error", "Work " + workid + " is not owned by account " + account.getId());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        } catch (NoSuchElementException e) {
            Log.create("Attempted to resolve work " + workid + ", but it does not exist",
                    "ChapterController.saveChapter()", "info", null);
            response.put("error", "unrecognized_work");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            Log.create("Authentication Exception: " + e.getMessage(),
                    "ChapterController.saveChapter()", "info", e);
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }


        //save chapter
        try {
            Chapter chapter = chapterRepository.findById(chapterid).get();
            if (chapter.getWork().getId().intValue() != work.getId()) {
                throw new ChapterOwnershipException();
            }
            chapter.writeHTML(content);
            chapter.writeNotes(notes);
            response.put("error", "none");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IOException e) {
            Log.create(e.getMessage(), "ChapterController.saveChapter()", "error", e);
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (NoSuchElementException e) { //chapter does not exist
            Log.create("Attempted to resolve work " + workid + ", but it does not exist",
                    "ChapterController.saveChapter()", "info", null);
            response.put("error", "unrecognized_work");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (ChapterOwnershipException e) {
            Log.create("chapter " + chapterid + " is not owned by work " + workid,
                    "ChapterController.saveChapter()", "info", e);
            response.put("error", "chapter " + chapterid + " is not owned by work " + workid);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }


    //note: inputs {chaptername}
    @PostMapping("/api/work/{workid}/chapter/{chapterid}/delete")
    public ResponseEntity<HashMap<String, Object>> deleteChapter(
            @PathVariable int workid, @PathVariable int chapterid,
            @CookieValue(value = "token", defaultValue = "null") String token,
            @RequestBody String data) {

        HashMap<String, Object> response = new HashMap<>();

        Account account;
        Work work;

        //retrieve token if applicable
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(data);
            //token verification if included in request body
            if (node.has("token")) {
                token = node.get("token").asText();
            }
        } catch (IOException e) {
            Log.create(e.getMessage(), "ChapterController.deleteChapter()", "error", e);
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }


        //authentication and work retrieval
        try {
            account = AuthenticationController.getByToken(token, authTokenRepository);
            work = workRepository.findById(workid).get();
            if (work.getAccount().getId().intValue() != account.getId()) {
                Log.create("Work " + workid + " is not owned by account " + account.getId(),
                        "ChapterController.deleteChapter()", "info", null);
                response.put("error", "Work " + workid + " is not owned by account " + account.getId());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        } catch (NoSuchElementException e) {
            Log.create("Attempted to resolve work " + workid + ", but it does not exist",
                    "ChapterController.deleteChapter()", "info", null);
            response.put("error", "unrecognized_work");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            Log.create("Authentication Exception: " + e.getMessage(),
                    "ChapterController.deleteChapter()", "info", e);
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }


        //find and delete chapter
        try {
            Chapter chapter = chapterRepository.findById(chapterid).get();
            if (chapter.getWork().getId().intValue() != work.getId()) {
                throw new ChapterOwnershipException();
            }
            chapter.delete(chapterRepository);
            response.put("error", "none");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (NoSuchElementException e) { //chapter does not exist
            Log.create("Attempted to resolve work " + workid + ", but it does not exist",
                    "ChapterController.deleteChapter()", "info", null);
            response.put("error", "unrecognized_work");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (ChapterOwnershipException e) {
            Log.create("chapter " + chapterid + " is not owned by work " + workid,
                    "ChapterController.deleteChapter()", "info", e);
            response.put("error", "chapter " + chapterid + " is not owned by work " + workid);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
}
