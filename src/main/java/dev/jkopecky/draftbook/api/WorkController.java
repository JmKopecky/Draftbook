package dev.jkopecky.draftbook.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jkopecky.draftbook.Log;
import dev.jkopecky.draftbook.data.tables.*;
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
public class WorkController {

    //all the database repositories load here, add a new entry if another table is needed for this controller
    AccountRepository accountRepository;
    WorkRepository workRepository;
    ChapterRepository chapterRepository;
    NoteCategoryRepository noteCategoryRepository;
    AuthTokenRepository authTokenRepository;
    NoteRepository noteRepository;
    public WorkController(AccountRepository accountRepository, WorkRepository workRepository, ChapterRepository chapterRepository, NoteCategoryRepository noteCategoryRepository, AuthTokenRepository authTokenRepository, NoteRepository noteRepository) {
        this.accountRepository = accountRepository;
        this.workRepository = workRepository;
        this.chapterRepository = chapterRepository;
        this.noteCategoryRepository = noteCategoryRepository;
        this.authTokenRepository = authTokenRepository;
        this.noteCategoryRepository = noteCategoryRepository;
    }



    //note: work wide api calls

    @GetMapping("/api/work/{workid}/retrieve")
    public ResponseEntity<HashMap<String, Object>> getWork(
            @PathVariable int workid,
            @CookieValue(value = "token", defaultValue = "null") String token,
            @RequestBody String data) {

        HashMap<String, Object> response = new HashMap<>();

        //retrieve token from data if applicable
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(data);
            //token verification if included in request body
            if (node.has("token")) {
                token = node.get("token").asText();
            }
        } catch (IOException e) {
            Log.create(e.getMessage(), "WorkController.getWork()", "error", e);
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        Account account;
        Work work;
        //authentication and work retrieval
        try {
            account = AuthenticationController.getByToken(token, authTokenRepository);
            work = workRepository.findById(workid).get();
            if (work.getAccount().getId().intValue() != account.getId()) {
                Log.create("Work " + workid + " is not owned by account " + account.getId(),
                        "WorkController.getWork()", "info", null);
                response.put("error", "Work " + workid + " is not owned by account " + account.getId());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        } catch (NoSuchElementException e) {
            Log.create("Attempted to resolve work " + workid + ", but it does not exist",
                    "WorkController.getWork()", "info", null);
            response.put("error", "unrecognized_work");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            Log.create("Authentication Exception: " + e.getMessage(),
                    "WorkController.getWork()", "info", e);
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        response.put("error", "none");
        response.put("work", work);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }



    @GetMapping("/api/work/{workid}/retrievechapters")
    public ResponseEntity<HashMap<String, Object>> getChapters(
            @PathVariable int workid,
            @CookieValue(value = "token", defaultValue = "null") String token,
            @RequestBody String data) {

        HashMap<String, Object> response = new HashMap<>();

        //retrieve token from data if applicable
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(data);
            //token verification if included in request body
            if (node.has("token")) {
                token = node.get("token").asText();
            }
        } catch (IOException e) {
            Log.create(e.getMessage(), "WorkController.getChapters()", "error", e);
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        Account account;
        Work work;
        //authentication and work retrieval
        try {
            account = AuthenticationController.getByToken(token, authTokenRepository);
            work = workRepository.findById(workid).get();
            if (work.getAccount().getId().intValue() != account.getId()) {
                Log.create("Work " + workid + " is not owned by account " + account.getId(),
                        "WorkController.getChapters()", "info", null);
                response.put("error", "Work " + workid + " is not owned by account " + account.getId());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        } catch (NoSuchElementException e) {
            Log.create("Attempted to resolve work " + workid + ", but it does not exist",
                    "WorkController.getChapters()", "info", null);
            response.put("error", "unrecognized_work");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            Log.create("Authentication Exception: " + e.getMessage(),
                    "WorkController.getChapters()", "info", e);
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        response.put("error", "none");
        response.put("chapters", work.getChapters(chapterRepository));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }



    @GetMapping("/api/work/{workid}/retrievenotecategories")
    public ResponseEntity<HashMap<String, Object>> getNoteCategories(
            @PathVariable int workid,
            @CookieValue(value = "token", defaultValue = "null") String token,
            @RequestBody String data) {

        HashMap<String, Object> response = new HashMap<>();


        //retrieve token from data if applicable
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(data);
            //token verification if included in request body
            if (node.has("token")) {
                token = node.get("token").asText();
            }
        } catch (IOException e) {
            Log.create(e.getMessage(), "WorkController.renameWork()", "error", e);
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }


        Account account;
        Work work;
        //authentication and work retrieval
        try {
            account = AuthenticationController.getByToken(token, authTokenRepository);
            work = workRepository.findById(workid).get();
            if (work.getAccount().getId().intValue() != account.getId()) {
                Log.create("Work " + workid + " is not owned by account " + account.getId(),
                        "WorkController.getChapters()", "info", null);
                response.put("error", "Work " + workid + " is not owned by account " + account.getId());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        } catch (NoSuchElementException e) {
            Log.create("Attempted to resolve work " + workid + ", but it does not exist",
                    "WorkController.getChapters()", "info", null);
            response.put("error", "unrecognized_work");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            Log.create("Authentication Exception: " + e.getMessage(),
                    "WorkController.getChapters()", "info", e);
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }


        ArrayList<HashMap<String, Note>> noteCategories = new ArrayList<>();
        for (NoteCategory nc : noteCategoryRepository.findByWork_Id(work.getId())) {
            HashMap<String, Note> categoryAsMap = new HashMap<>();
            for (String id : nc.getNotes()) {
                categoryAsMap.put(id, noteRepository.findById(Integer.parseInt(id)).get());
            }
            noteCategories.add(categoryAsMap);
        }


        //retrieve notes and reply
        response.put("error", "none");
        response.put("notecategories", noteCategories);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }




    @PostMapping("/api/work/{workid}/rename")
    public ResponseEntity<HashMap<String, Object>> renameWork(
            @PathVariable int workid,
            @CookieValue(value = "token", defaultValue = "null") String token,
            @RequestBody String data) {

        HashMap<String, Object> response = new HashMap<>();


        //retrieve response data
        String newName;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(data);
            newName = node.get("newName").asText();
            //token verification if included in request body
            if (node.has("token")) {
                token = node.get("token").asText();
            }
        } catch (IOException e) {
            Log.create(e.getMessage(), "WorkController.renameWork()", "error", e);
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }


        Account account;
        Work work;
        //authentication and work retrieval
        try {
            account = AuthenticationController.getByToken(token, authTokenRepository);
            work = workRepository.findById(workid).get();
            if (work.getAccount().getId().intValue() != account.getId()) {
                Log.create("Work " + workid + " is not owned by account " + account.getId(),
                        "WorkController.renameWork()", "info", null);
                response.put("error", "Work " + workid + " is not owned by account " + account.getId());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        } catch (NoSuchElementException e) {
            Log.create("Attempted to resolve work " + workid + ", but it does not exist",
                    "WorkController.renameWork()", "info", null);
            response.put("error", "unrecognized_work");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            Log.create("Authentication Exception: " + e.getMessage(),
                    "WorkController.renameWork()", "info", e);
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }


        boolean result = work.changeName(newName, workRepository);
        if (result) {
            response.put("error", "none");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            response.put("error", "rename_failed");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }





    @PostMapping("/api/work/{workid}/delete")
    public ResponseEntity<HashMap<String, Object>> deleteWork(
            @PathVariable int workid,
            @CookieValue(value = "token", defaultValue = "null") String token,
            @RequestBody String data) {

        HashMap<String, Object> response = new HashMap<>();


        //retrieve token from data if applicable
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(data);
            //token verification if included in request body
            if (node.has("token")) {
                token = node.get("token").asText();
            }
        } catch (IOException e) {
            Log.create(e.getMessage(), "WorkController.deleteWork()", "error", e);
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }


        Account account;
        Work work;
        //authentication and work retrieval
        try {
            account = AuthenticationController.getByToken(token, authTokenRepository);
            work = workRepository.findById(workid).get();
            if (work.getAccount().getId().intValue() != account.getId()) {
                Log.create("Work " + workid + " is not owned by account " + account.getId(),
                        "WorkController.deleteWork()", "info", null);
                response.put("error", "Work " + workid + " is not owned by account " + account.getId());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        } catch (NoSuchElementException e) {
            Log.create("Attempted to resolve work " + workid + ", but it does not exist",
                    "WorkController.deleteWork()", "info", null);
            response.put("error", "unrecognized_work");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            Log.create("Authentication Exception: " + e.getMessage(),
                    "WorkController.deleteWork()", "info", e);
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }


        boolean result = work.delete(workRepository);
        if (result) {
            response.put("error", "none");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            response.put("error", "delete_failed");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }



    @PostMapping("/api/work/create")
    public ResponseEntity<HashMap<String, Object>> createWork(
            @CookieValue(value = "token", defaultValue = "null") String token,
            @RequestBody String data) {

        HashMap<String, Object> response = new HashMap<>();

        String title;
        //retrieve title
        //token verification from body if applicable
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(data);
            title = node.get("title").asText();
            //token verification if included in request body
            if (node.has("token")) {
                token = node.get("token").asText();
            }
        } catch (IOException e) {
            Log.create(e.getMessage(), "WorkController.createWork()", "error", e);
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        Account account;
        //authentication
        try {
            account = AuthenticationController.getByToken(token, authTokenRepository);
        } catch (Exception e) {
            Log.create("Authentication Exception: " + e.getMessage(),
                    "WorkController.createWork()", "info", e);
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }


        Work work;
        try {
            work = Work.createWork(account, title, workRepository);
        } catch (IOException e) {
            Log.create("Storage Exception: " + e.getMessage(),
                    "WorkController.createWork()", "error", e);
            response.put("error", "storage_exception");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        response.put("error", "none");
        response.put("workid", work.getId());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
