package dev.jkopecky.draftbook.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
