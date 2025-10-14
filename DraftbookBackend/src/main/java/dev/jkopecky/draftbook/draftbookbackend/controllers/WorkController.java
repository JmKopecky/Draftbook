package dev.jkopecky.draftbook.draftbook.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jkopecky.draftbook.draftbook.data.Account;
import dev.jkopecky.draftbook.draftbook.data.AccountRepository;
import dev.jkopecky.draftbook.draftbook.data.Work;
import dev.jkopecky.draftbook.draftbook.data.WorkRepository;
import dev.jkopecky.draftbook.draftbook.exceptions.NonexistentAccountException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Controller
@RequestMapping("/api/works")
public class WorkController {

    AccountRepository accountRepository;
    WorkRepository workRepository;
    public WorkController(AccountRepository accountRepository,  WorkRepository workRepository) {
        this.accountRepository = accountRepository;
        this.workRepository = workRepository;
    }

    /**
     * Return a list of works that the user owns.
     * @param user The account calling this mapping.
     * @return A response entity containing a list of works.
     */
    @GetMapping("/get/worklist")
    public ResponseEntity<List<Work>> getWorkList(@AuthenticationPrincipal OidcUser user) {
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
    @GetMapping("/get/work")
    public ResponseEntity<Work> getWork(
            @AuthenticationPrincipal OidcUser user, @RequestBody String body) {

        Account account = Account.getOrCreateAccount(user.getSubject(), accountRepository);

        //get the target work
        Work targetWork;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(body);
            int workId = node.get("workid").asInt();
            targetWork = workRepository.findById(workId).get();
        } catch (JsonProcessingException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }

        //check that the account has the right to access this work
        if (targetWork.getAccountId() == account.getId()) {
            return new ResponseEntity<>(targetWork, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
    }
}
