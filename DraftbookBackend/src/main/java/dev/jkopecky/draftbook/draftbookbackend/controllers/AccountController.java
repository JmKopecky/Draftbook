package dev.jkopecky.draftbook.draftbook.controllers;

import dev.jkopecky.draftbook.draftbook.data.Account;
import dev.jkopecky.draftbook.draftbook.data.AccountRepository;
import dev.jkopecky.draftbook.draftbook.exceptions.NonexistentAccountException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("/api/account")
public class AccountController {


}
