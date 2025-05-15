package dev.jkopecky.draftbook.web;

import dev.jkopecky.draftbook.api.AuthenticationController;
import dev.jkopecky.draftbook.data.tables.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.NoSuchElementException;
import java.util.Objects;

@Controller
public class WorkDisplayController {

    WorkRepository workRepository;
    AuthTokenRepository authTokenRepository;
    ChapterRepository chapterRepository;
    public WorkDisplayController(WorkRepository workRepository, AuthTokenRepository authTokenRepository, ChapterRepository chapterRepository) {
        this.workRepository = workRepository;
        this.authTokenRepository = authTokenRepository;
        this.chapterRepository = chapterRepository;
    }

    @GetMapping("/work/{id}")
    public String work(Model model,
                       @CookieValue(value = "token", defaultValue = "null") String token,
                       @PathVariable String id,
                       @RequestParam(name = "section", required = false) String section) {

        //determine work
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

        //determine section
        try {
            String tempsection = section.toLowerCase();
            if (tempsection.equals("metadata") || tempsection.equals("chapters") || tempsection.equals("notes") || tempsection.equals("export")) {
                model.addAttribute("section", tempsection);
            } else {
                model.addAttribute("section", "metadata");
            }
        } catch (Exception _) {
            model.addAttribute("section", "metadata");
        }

        //auth passed, hand over the work
        model.addAttribute("work", work);
        model.addAttribute("chapters", work.getChapters(chapterRepository));
        return "workdisplay";
    }
}
