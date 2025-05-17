package dev.jkopecky.draftbook.web;

import dev.jkopecky.draftbook.Log;
import dev.jkopecky.draftbook.api.AuthenticationController;
import dev.jkopecky.draftbook.data.tables.Account;
import dev.jkopecky.draftbook.data.tables.AuthTokenRepository;
import dev.jkopecky.draftbook.data.tables.Chapter;
import dev.jkopecky.draftbook.data.tables.ChapterRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.util.NoSuchElementException;

@Controller
public class EditorController {

    ChapterRepository chapterRepository;
    AuthTokenRepository authTokenRepository;
    public EditorController(ChapterRepository chapterRepository, AuthTokenRepository authTokenRepository) {
        this.chapterRepository = chapterRepository;
        this.authTokenRepository = authTokenRepository;
    }

    @GetMapping("/chapter/{chapterid}")
    public String getEditor(Model model,
                            @PathVariable int chapterid,
                            @CookieValue(value = "token", defaultValue = "null") String token) {

        Account account;
        Chapter chapter;
        try {
            account = AuthenticationController.getByToken(token, authTokenRepository);
        } catch (Exception _) {
            return "redirect:/signon";
        }
        try {
            chapter = chapterRepository.findById(chapterid).get();
        } catch (NoSuchElementException e) {
            return "error/404";
        }
        if (chapter.getWork().getAccount().getId() != account.getId().intValue()) {
            return "error/404";
        }

        model.addAttribute("current_chapter", chapter);
        try {
            model.addAttribute("content_chapter", chapter.retrieveAsHTML());
        } catch (IOException e) {
            return "error";
        }

        model.addAttribute("editor", true);

        //sidebar fields
        model.addAttribute("sidebar_chapters", chapterRepository.findByWork_Id(chapter.getWork().getId()));

        return "editor";
    }
}
