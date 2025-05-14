package dev.jkopecky.draftbook.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jkopecky.draftbook.Log;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;

@Controller
public class SignonController {

    @GetMapping("/signon")
    public String signon(Model model, @RequestParam(name="create", required = false) String create) {
        boolean newAccount = false;
        try {
            newAccount = Boolean.parseBoolean(create);
        } catch (Exception e) {}

        model.addAttribute("newaccount", newAccount);

        return "signon";
    }
}
