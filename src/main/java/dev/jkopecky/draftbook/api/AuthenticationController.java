package dev.jkopecky.draftbook.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jkopecky.draftbook.Log;
import dev.jkopecky.draftbook.data.tables.Account;
import dev.jkopecky.draftbook.data.tables.AccountRepository;
import dev.jkopecky.draftbook.data.tables.AuthToken;
import dev.jkopecky.draftbook.data.tables.AuthTokenRepository;
import dev.jkopecky.draftbook.exceptions.AccountException;
import dev.jkopecky.draftbook.exceptions.AccountNonexistentException;
import dev.jkopecky.draftbook.exceptions.InvalidPasswordException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;

@Controller
@CrossOrigin
public class AuthenticationController {



    AccountRepository accountRepository;
    AuthTokenRepository authTokenRepository;
    public AuthenticationController(AccountRepository accountRepository, AuthTokenRepository authTokenRepository) {
        this.accountRepository = accountRepository;
        this.authTokenRepository = authTokenRepository;
    }


    //todo adapt to use custom exceptions with more detailed names
    public static Account getByToken(String token, AuthTokenRepository authTokenRepository) throws Exception {
        for (AuthToken authToken : authTokenRepository.findAll()) {
            if (authToken.getValue().equals(token)) {
                //token found.
                return authToken.getAccount();
            }
        }
        //no matching token exists.
        throw new Exception("No matching token exists.");
    }



    public static Account localAuth(String email, String password, AccountRepository repository) throws AccountException {
        Account account = null;

        //ensure account exists
        if (!Account.exists(email, repository)) {
            throw new AccountNonexistentException();
        }
        account = Account.authenticate(email, password, repository);
        if (account == null) {
            //incorrect password
            throw new InvalidPasswordException();
        }

        return account;
    }



    @PostMapping("/api/auth/authenticate")
    public ResponseEntity<HashMap<String, Object>> authenticate(@RequestBody String data, HttpServletResponse serverResponse) {
        HashMap<String, Object> response = new HashMap<>();

        String email;
        String password;

        ObjectMapper mapper = new ObjectMapper();
        try { //read data from request
            JsonNode node = mapper.readTree(data);
            email = node.get("email").asText();
            password = node.get("password").asText();
        } catch (Exception e) {
            Log.create(e.getMessage(), "AuthenticationController.authenticate()", "info", e);
            response.put("error", "authenticate_parse");
            return new ResponseEntity<>(response, HttpStatus.valueOf(500));
        }

        //todo secure sign on
        try {
            Account account = localAuth(email, password, accountRepository);
            //delete the old token associated with this account, if any.
            for (AuthToken token : authTokenRepository.findAll()) {
                if (token.getAccount().getEmail().equals(account.getEmail())) {
                    authTokenRepository.delete(token);
                }
            }
            //create a new token for the account.
            AuthToken token = new AuthToken(account, authTokenRepository);

            Cookie cookie = new Cookie("token", token.getValue());
            cookie.setMaxAge(60*60);
            cookie.setPath("/");
            serverResponse.addCookie(cookie);

            response.put("error", "none");
            response.put("authenticated", true);
            response.put("token", token.getValue());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (AccountNonexistentException _) {
            Log.create("Attempted to access account " + email + ", but it does not exist.",
                    "AuthenticationController.authenticate()", "info", null);
            response.put("error", "account_nonexistent");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);

        } catch (AccountException _) {
            Log.create("Failed to authenticate account " + email + " with password " + password + ".",
                    "AuthenticationController.authenticate()", "info", null);
            response.put("error", "invalid_password");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }



    @PostMapping("/api/auth/exists")
    public ResponseEntity<HashMap<String, Object>> exists(@RequestBody String data) {
        HashMap<String, Object> response = new HashMap<>();

        String email;

        ObjectMapper mapper = new ObjectMapper();
        try { //read data from request
            JsonNode node = mapper.readTree(data);
            email = node.get("email").asText();
        } catch (Exception e) {
            Log.create(e.getMessage(), "AuthenticationController.exists()", "info", e);
            response.put("error", "authenticate_parse");
            return new ResponseEntity<>(response, HttpStatus.valueOf(500));
        }

        response.put("exists", Account.exists(email, accountRepository));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }



    @PostMapping("/api/auth/create")
    public ResponseEntity<HashMap<String, Object>> create(@RequestBody String data, HttpServletResponse serverResponse) {
        HashMap<String, Object> response = new HashMap<>();

        String email;
        String password;

        ObjectMapper mapper = new ObjectMapper();
        try { //read data from request
            JsonNode node = mapper.readTree(data);
            email = node.get("email").asText();
            password = node.get("password").asText();
        } catch (Exception e) {
            Log.create(e.getMessage(), "AuthenticationController.exists()", "info", e);
            response.put("error", "authenticate_parse");
            return new ResponseEntity<>(response, HttpStatus.valueOf(500));
        }

        //make sure the account doesn't already exist
        if (Account.exists(email, accountRepository)) {
            Log.create("Attempted to create account, but the email " + email + " already exists.",
                    "AuthenticationController.create()", "info", null);
            response.put("error", "email_taken");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        //create account
        Account account = Account.create(email, password, accountRepository);
        AuthToken token = new AuthToken(account, authTokenRepository);

        //reply
        response.put("error", "none");
        response.put("authenticated", true);
        response.put("token", token.getValue());

        Cookie cookie = new Cookie("token", token.getValue());
        cookie.setMaxAge(60*60);
        cookie.setPath("/");
        serverResponse.addCookie(cookie);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
