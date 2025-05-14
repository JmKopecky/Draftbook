package dev.jkopecky.draftbook.web;

import dev.jkopecky.draftbook.api.AuthenticationController;
import dev.jkopecky.draftbook.data.tables.Account;
import dev.jkopecky.draftbook.data.tables.AuthTokenRepository;
import dev.jkopecky.draftbook.data.tables.Work;
import dev.jkopecky.draftbook.data.tables.WorkRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.NoSuchElementException;
import java.util.Objects;

@Controller
public class WorkDisplayController {

    WorkRepository workRepository;
    AuthTokenRepository authTokenRepository;
    public WorkDisplayController(WorkRepository workRepository, AuthTokenRepository authTokenRepository) {
        this.workRepository = workRepository;
        this.authTokenRepository = authTokenRepository;
    }

    @GetMapping("/work/{id}")
    public String work(Model model,
                       @CookieValue(value = "token", defaultValue = "null") String token,
                       @PathVariable String id) {

        Work work = null;
        try {
            work = workRepository.findById(Integer.parseInt(id)).get();
        } catch (Exception e) {
            return "error/404";
        }
        Account account;
        try {
            account = AuthenticationController.getByToken(token, authTokenRepository);
        } catch (Exception _) {
            return "redirect:/signon";
        }
        if (!Objects.equals(work.getAccount().getId(), account.getId())) {
            return "redirect:/signon";
        }

        //auth passed, hand over the work
        model.addAttribute("work", work);
        return "workdisplay";
    }
}
