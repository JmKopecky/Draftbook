package dev.jkopecky.draftbook.web;

import dev.jkopecky.draftbook.api.AuthenticationController;
import dev.jkopecky.draftbook.api.WorkController;
import dev.jkopecky.draftbook.data.tables.Account;
import dev.jkopecky.draftbook.data.tables.AuthTokenRepository;
import dev.jkopecky.draftbook.data.tables.Work;
import dev.jkopecky.draftbook.data.tables.WorkRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;

@Controller
public class DashboardController {


    public AuthTokenRepository authTokenRepository;
    public WorkRepository workRepository;
    public DashboardController(AuthTokenRepository authTokenRepository, WorkRepository workRepository) {
        this.authTokenRepository = authTokenRepository;
        this.workRepository = workRepository;
    }

    @GetMapping("/dashboard")
    public String account(Model model,
                          @RequestParam(name = "section", required = false) String section,
                          @CookieValue(value = "token", defaultValue = "null") String token) {

        Account account;
        try {
            account = AuthenticationController.getByToken(token, authTokenRepository);
        } catch (Exception _) {
            return "redirect:/signon";
        }

        try {
            String tempsection = section.toLowerCase();
            if (tempsection.equals("statistics")
                    || tempsection.equals("security")
                    || tempsection.equals("customization")
                    || tempsection.equals("premium")
                    || tempsection.equals("billing")
                    || tempsection.equals("operations")
                    || tempsection.equals("works")) {
                model.addAttribute("section", tempsection);
            } else {
                model.addAttribute("section", "statistics");
            }
        } catch (Exception _) {
            model.addAttribute("section", "statistics");
        }

        ArrayList<Work> works = new ArrayList<>(workRepository.findByAccount_Id(account.getId()));
        model.addAttribute("works", works);

        return "dashboard";
    }
}
