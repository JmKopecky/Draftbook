package dev.jkopecky.draftbook.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BasicPagesController {

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/features")
    public String features() {return "features";}

    @GetMapping("/pricing")
    public String pricing() {return "pricing";}

    @GetMapping("/about")
    public String about() {return "about";}

    @GetMapping("/support")
    public String support() {return "support";}
}
