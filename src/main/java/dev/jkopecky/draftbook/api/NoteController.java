package dev.jkopecky.draftbook.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jkopecky.draftbook.Log;
import dev.jkopecky.draftbook.data.tables.*;
import dev.jkopecky.draftbook.exceptions.NoteCategoryOwnershipException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;

@Controller
@CrossOrigin
public class NoteController {



    //all the database repositories load here, add a new entry if another table is needed for this controller
    AccountRepository accountRepository;
    WorkRepository workRepository;
    ChapterRepository chapterRepository;
    NoteCategoryRepository noteCategoryRepository;
    AuthTokenRepository authTokenRepository;
    public NoteController(AccountRepository accountRepository, WorkRepository workRepository, ChapterRepository chapterRepository, NoteCategoryRepository noteCategoryRepository, AuthTokenRepository authTokenRepository) {
        this.accountRepository = accountRepository;
        this.workRepository = workRepository;
        this.chapterRepository = chapterRepository;
        this.noteCategoryRepository = noteCategoryRepository;
        this.authTokenRepository = authTokenRepository;
    }





    private Object getTargetWork(String target, Account account) {
        //retrieve work
        ArrayList<Work> works = account.getOwnedWorks(workRepository);
        for (Work work : works) {
            if (("" + work.getId()).equals(target)) {
                //found the work
                return work;
            }
        }

        //if this point is reached, the work was not found in the user's list of works
        Log.create("Failed to find work " + target + " in account work list.",
                "NoteController.getTargetWork()", "info", null);
        return "unrecognized_work";
    }



    public ArrayList<Object> getAccountAndWork(String token, String target) {
        ArrayList<Object> output = new ArrayList<>();
        String error = "none";

        //confirm user credentials
        Account account;
        try {
            account = AuthenticationController.getByToken(token, authTokenRepository);
        } catch (Exception e) {
            //failed to retrieve account;
            error = "Failed to match auth token to account";
            output.add(error);
            return output;
        }

        //retrieve work
        Work work;
        Object workResult = getTargetWork(target, account);
        if (workResult instanceof Work w) {
            work = w;
        } else {
            error = "" + workResult;
            output.add(error);
            return output;
        }
        output.add(account);
        output.add(work);
        output.add(error);
        return output;
    }




    //note: inputs {noteCategoryName}
    @PostMapping("/api/work/{workid}/notecategory/create")
    public ResponseEntity<HashMap<String, Object>> createCategory(
            @PathVariable int workid,
            @CookieValue(value = "token", defaultValue = "null") String token,
            @RequestBody String data) {

        HashMap<String, Object> response = new HashMap<>();

        Account account;
        Work work;

        //retrieve request data
        String noteCategoryName;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(data);
            noteCategoryName = node.get("noteCategoryName").asText();
            //token verification if included in request body
            if (node.has("token")) {
                token = node.get("token").asText();
            }
        } catch (IOException e) {
            Log.create(e.getMessage(), "NoteController.createCategory()", "error", e);
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }


        //authentication and work retrieval
        try {
            account = AuthenticationController.getByToken(token, authTokenRepository);
            work = workRepository.findById(workid).get();
            if (work.getAccount().getId().intValue() != account.getId()) {
                Log.create("Work " + workid + " is not owned by account " + account.getId(),
                        "NoteController.createCategory()", "info", null);
                response.put("error", "Work " + workid + " is not owned by account " + account.getId());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        } catch (NoSuchElementException e) {
            Log.create("Attempted to resolve work " + workid + ", but it does not exist",
                    "NoteController.createCategory()", "info", null);
            response.put("error", "unrecognized_work");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            Log.create("Authentication Exception: " + e.getMessage(),
                    "NoteController.createCategory()", "info", e);
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }


        try {
            NoteCategory.create(noteCategoryRepository, work, noteCategoryName);
        } catch (IOException e) {
            Log.create(e.getMessage(), "NoteController.createCategory()", "error", e);
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        response.put("error", "none");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }



    //note: inputs {noteCategoryName}
    @PostMapping("/api/work/{workid}/notecategory/{categoryid}/rename")
    public ResponseEntity<HashMap<String, Object>> renameCategory(
            @PathVariable int workid, @PathVariable int categoryid,
            @CookieValue(value = "token", defaultValue = "null") String token,
            @RequestBody String data) {

        HashMap<String, Object> response = new HashMap<>();

        Account account;
        Work work;

        //retrieve request data
        String noteCategoryName;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(data);
            noteCategoryName = node.get("noteCategoryName").asText();
            //token verification if included in request body
            if (node.has("token")) {
                token = node.get("token").asText();
            }
        } catch (IOException e) {
            Log.create(e.getMessage(), "NoteController.renameCategory()", "error", e);
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }


        //authentication and work retrieval
        try {
            account = AuthenticationController.getByToken(token, authTokenRepository);
            work = workRepository.findById(workid).get();
            if (work.getAccount().getId().intValue() != account.getId()) {
                Log.create("Work " + workid + " is not owned by account " + account.getId(),
                        "NoteController.renameCategory()", "info", null);
                response.put("error", "Work " + workid + " is not owned by account " + account.getId());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        } catch (NoSuchElementException e) {
            Log.create("Attempted to resolve work " + workid + ", but it does not exist",
                    "NoteController.renameCategory()", "info", null);
            response.put("error", "unrecognized_work");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            Log.create("Authentication Exception: " + e.getMessage(),
                    "NoteController.renameCategory()", "info", e);
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        //todo implement

        response.put("error", "none");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }



    //note: inputs {noteCategoryName}
    @PostMapping("/api/work/{workid}/notecategory{categoryid}/delete")
    public ResponseEntity<HashMap<String, Object>> deleteCategory(
            @PathVariable int workid, @PathVariable int categoryid,
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
            Log.create(e.getMessage(), "NoteController.deleteCategory()", "error", e);
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }


        //authentication and work retrieval
        try {
            account = AuthenticationController.getByToken(token, authTokenRepository);
            work = workRepository.findById(workid).get();
            if (work.getAccount().getId().intValue() != account.getId()) {
                Log.create("Work " + workid + " is not owned by account " + account.getId(),
                        "NoteController.deleteCategory()", "info", null);
                response.put("error", "Work " + workid + " is not owned by account " + account.getId());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        } catch (NoSuchElementException e) {
            Log.create("Attempted to resolve work " + workid + ", but it does not exist",
                    "NoteController.deleteCategory()", "info", null);
            response.put("error", "unrecognized_work");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            Log.create("Authentication Exception: " + e.getMessage(),
                    "NoteController.deleteCategory()", "info", e);
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }


        //find the noteCategory and delete it
        try {
            NoteCategory category = noteCategoryRepository.findById(categoryid).get();
            if (category.getWork().getId().intValue() != work.getId()) {
                throw new NoteCategoryOwnershipException();
            }
            //delete
            for (String s : category.getNotes()) {
                try {
                    category.deleteNote(s, noteCategoryRepository);
                } catch (IOException e) {
                    Log.create("Failed to delete note: " + s, "NoteController.deleteCategory()", "error", e);
                    response.put("error", e.getMessage());
                    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
            new File(category.findPath() + "/").delete();
            new File(category.findPath() + ".json").delete();
            noteCategoryRepository.delete(category);
        } catch (NoSuchElementException e) {
            Log.create("Attempted to resolve category " + categoryid + ", but it does not exist",
                    "NoteController.deleteCategory()", "info", null);
            response.put("error", "unrecognized_category");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (NoteCategoryOwnershipException e) {
            Log.create("category " + categoryid + " is not owned by work " + workid,
                    "NoteController.deleteCategory()", "info", null);
            response.put("error", "category_invalid_ownership");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }


        response.put("error", "none");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }





    // notes



    //note: inputs {noteCategoryName, noteName}
    @PostMapping("/api/work/{workid}/notecategory/{categoryid}/note/create")
    public ResponseEntity<HashMap<String, Object>> createNote(
            @PathVariable int workid, @PathVariable int categoryid,
            @CookieValue(value = "token", defaultValue = "null") String token,
            @RequestBody String data) {

        HashMap<String, Object> response = new HashMap<>();

        Account account;
        Work work;

        //retrieve request data
        String noteName;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(data);
            noteName = node.get("noteName").asText();
            //token verification if included in request body
            if (node.has("token")) {
                token = node.get("token").asText();
            }
        } catch (IOException e) {
            Log.create(e.getMessage(), "NoteController.createNote()", "error", e);
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }


        //authentication and work retrieval
        try {
            account = AuthenticationController.getByToken(token, authTokenRepository);
            work = workRepository.findById(workid).get();
            if (work.getAccount().getId().intValue() != account.getId()) {
                Log.create("Work " + workid + " is not owned by account " + account.getId(),
                        "NoteController.createNote()", "info", null);
                response.put("error", "Work " + workid + " is not owned by account " + account.getId());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        } catch (NoSuchElementException e) {
            Log.create("Attempted to resolve work " + workid + ", but it does not exist",
                    "NoteController.createNote()", "info", null);
            response.put("error", "unrecognized_work");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            Log.create("Authentication Exception: " + e.getMessage(),
                    "NoteController.createNote()", "info", e);
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        //find the notecategory and create the note
        try {
            NoteCategory category = noteCategoryRepository.findById(categoryid).get();
            if (category.getWork().getId().intValue() != work.getId()) {
                throw new NoteCategoryOwnershipException();
            }
            category.addNote(noteName, "", noteCategoryRepository);
            noteCategoryRepository.save(category);
        } catch (NoSuchElementException e) {
            Log.create("Attempted to resolve category " + categoryid + ", but it does not exist",
                    "NoteController.deleteCategory()", "info", null);
            response.put("error", "unrecognized_category");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (NoteCategoryOwnershipException e) {
            Log.create("category " + categoryid + " is not owned by work " + workid,
                    "NoteController.deleteCategory()", "info", null);
            response.put("error", "category_invalid_ownership");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            Log.create(e.getMessage(), "NoteController.createNote()", "error", e);
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        response.put("error", "none");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }



    //note: inputs {noteCategoryName, noteName}
    @PostMapping("/api/work/{workid}/notecategory/{categoryid}/note/select")
    public ResponseEntity<HashMap<String, Object>> selectNote(
            @PathVariable int workid, @PathVariable int categoryid,
            @CookieValue(value = "token", defaultValue = "null") String token,
            @RequestBody String data) {

        HashMap<String, Object> response = new HashMap<>();

        Account account;
        Work work;

        //retrieve response data
        String noteName;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(data);
            noteName = node.get("noteName").asText();
            //token verification if included in request body
            if (node.has("token")) {
                token = node.get("token").asText();
            }
        } catch (IOException e) {
            Log.create(e.getMessage(), "NoteController.selectNote()", "error", e);
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }


        //authentication and work retrieval
        try {
            account = AuthenticationController.getByToken(token, authTokenRepository);
            work = workRepository.findById(workid).get();
            if (work.getAccount().getId().intValue() != account.getId()) {
                Log.create("Work " + workid + " is not owned by account " + account.getId(),
                        "NoteController.selectNote()", "info", null);
                response.put("error", "Work " + workid + " is not owned by account " + account.getId());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        } catch (NoSuchElementException e) {
            Log.create("Attempted to resolve work " + workid + ", but it does not exist",
                    "NoteController.selectNote()", "info", null);
            response.put("error", "unrecognized_work");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            Log.create("Authentication Exception: " + e.getMessage(),
                    "NoteController.selectNote()", "info", e);
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }


        //todo implement

        response.put("error", "none");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }



    //note: inputs {noteCategoryName, noteName, newNoteName}
    @PostMapping("/api/work/{workid}/notecategory/{categoryid}/note/rename")
    public ResponseEntity<HashMap<String, Object>> renameNote(
            @PathVariable int workid, @PathVariable int categoryid,
            @CookieValue(value = "token", defaultValue = "null") String token,
            @RequestBody String data) {

        HashMap<String, Object> response = new HashMap<>();

        Account account;
        Work work;

        //retrieve response data
        String noteName;
        String newNoteName;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(data);
            noteName = node.get("noteName").asText();
            newNoteName = node.get("newNoteName").asText();
            //token verification if included in request body
            if (node.has("token")) {
                token = node.get("token").asText();
            }
        } catch (IOException e) {
            Log.create(e.getMessage(), "NoteController.renameNote()", "error", e);
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }


        //authentication and work retrieval
        try {
            account = AuthenticationController.getByToken(token, authTokenRepository);
            work = workRepository.findById(workid).get();
            if (work.getAccount().getId().intValue() != account.getId()) {
                Log.create("Work " + workid + " is not owned by account " + account.getId(),
                        "NoteController.renameNote()", "info", null);
                response.put("error", "Work " + workid + " is not owned by account " + account.getId());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        } catch (NoSuchElementException e) {
            Log.create("Attempted to resolve work " + workid + ", but it does not exist",
                    "NoteController.renameNote()", "info", null);
            response.put("error", "unrecognized_work");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            Log.create("Authentication Exception: " + e.getMessage(),
                    "NoteController.renameNote()", "info", e);
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }



        //find the notecategory and rename the note
        try {
            NoteCategory category = noteCategoryRepository.findById(categoryid).get();
            if (category.getWork().getId().intValue() != work.getId()) {
                throw new NoteCategoryOwnershipException();
            }

            if (category.getNotes().contains(noteName)) {
                try {
                    category.renameNote(noteName, newNoteName, noteCategoryRepository);
                } catch (FileAlreadyExistsException e) {
                    response.put("error", "file_already_exists");
                    Log.create("Attempted to edit note name, but the filename already is being used.", "NoteController.renameNote()", "info", null);
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                } catch (FileNotFoundException e) {
                    response.put("error", "unrecognized_note");
                    Log.create("Attempted to rename note that does not exist.", "NoteController.renameNote()", "info", null);
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
            }
        } catch (NoSuchElementException e) {
            Log.create("Attempted to resolve category " + categoryid + ", but it does not exist",
                    "NoteController.deleteCategory()", "info", null);
            response.put("error", "unrecognized_category");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (NoteCategoryOwnershipException e) {
            Log.create("category " + categoryid + " is not owned by work " + workid,
                    "NoteController.deleteCategory()", "info", null);
            response.put("error", "category_invalid_ownership");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        response.put("error", "none");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }



    //note: inputs {noteCategoryName, noteName}
    @PostMapping("/api/work/{workid}/notecategory/{categoryid}/note/save")
    public ResponseEntity<HashMap<String, Object>> saveNote(
            @PathVariable int workid, @PathVariable int categoryid,
            @CookieValue(value = "token", defaultValue = "null") String token,
            @RequestBody String data) {

        HashMap<String, Object> response = new HashMap<>();

        Account account;
        Work work;

        //retrieve response data
        String noteName;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(data);
            noteName = node.get("noteName").asText();
            //token verification if included in request body
            if (node.has("token")) {
                token = node.get("token").asText();
            }
        } catch (IOException e) {
            Log.create(e.getMessage(), "NoteController.saveNote()", "error", e);
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }


        //authentication and work retrieval
        try {
            account = AuthenticationController.getByToken(token, authTokenRepository);
            work = workRepository.findById(workid).get();
            if (work.getAccount().getId().intValue() != account.getId()) {
                Log.create("Work " + workid + " is not owned by account " + account.getId(),
                        "NoteController.saveNote()", "info", null);
                response.put("error", "Work " + workid + " is not owned by account " + account.getId());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        } catch (NoSuchElementException e) {
            Log.create("Attempted to resolve work " + workid + ", but it does not exist",
                    "NoteController.saveNote()", "info", null);
            response.put("error", "unrecognized_work");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            Log.create("Authentication Exception: " + e.getMessage(),
                    "NoteController.saveNote()", "info", e);
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }


        //todo implement

        response.put("error", "none");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }



    //note: inputs {noteCategoryName, noteName}
    @PostMapping("/api/work/{workid}/notecategory/{categoryid}/note/delete")
    public ResponseEntity<HashMap<String, Object>> deleteNote(
            @PathVariable int workid, @PathVariable int categoryid,
            @CookieValue(value = "token", defaultValue = "null") String token,
            @RequestBody String data) {

        HashMap<String, Object> response = new HashMap<>();

        Account account;
        Work work;

        //retrieve response data
        String noteName;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(data);
            noteName = node.get("noteName").asText();
            //token verification if included in request body
            if (node.has("token")) {
                token = node.get("token").asText();
            }
        } catch (IOException e) {
            Log.create(e.getMessage(), "NoteController.deleteNote()", "error", e);
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }


        //authentication and work retrieval
        try {
            account = AuthenticationController.getByToken(token, authTokenRepository);
            work = workRepository.findById(workid).get();
            if (work.getAccount().getId().intValue() != account.getId()) {
                Log.create("Work " + workid + " is not owned by account " + account.getId(),
                        "NoteController.deleteNote()", "info", null);
                response.put("error", "Work " + workid + " is not owned by account " + account.getId());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        } catch (NoSuchElementException e) {
            Log.create("Attempted to resolve work " + workid + ", but it does not exist",
                    "NoteController.deleteNote()", "info", null);
            response.put("error", "unrecognized_work");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            Log.create("Authentication Exception: " + e.getMessage(),
                    "NoteController.deleteNote()", "info", e);
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        //retrieve the category and delete the note
        try {
            NoteCategory category = noteCategoryRepository.findById(categoryid).get();
            if (category.getWork().getId().intValue() != work.getId()) {
                throw new NoteCategoryOwnershipException();
            }

            if (category.getNotes().contains(noteName)) {
                try {
                    category.deleteNote(noteName, noteCategoryRepository);
                } catch (FileNotFoundException e) {
                    Log.create("Attempted to delete note " + noteName + ", but it does not exist", "NoteController.deleteNote()", "info", null);
                    response.put("error", "note_does_not_exist");
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                } catch (Exception e) {
                    Log.create(e.getMessage(), "NoteController.deleteNote()", "error", e);
                    response.put("error", e.getMessage());
                    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } catch (NoSuchElementException e) {
            Log.create("Attempted to resolve category " + categoryid + ", but it does not exist",
                    "NoteController.deleteCategory()", "info", null);
            response.put("error", "unrecognized_category");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (NoteCategoryOwnershipException e) {
            Log.create("category " + categoryid + " is not owned by work " + workid,
                    "NoteController.deleteCategory()", "info", null);
            response.put("error", "category_invalid_ownership");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        response.put("error", "none");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
