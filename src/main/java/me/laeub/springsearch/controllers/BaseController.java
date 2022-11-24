package me.laeub.springsearch.controllers;

import me.laeub.springsearch.models.SeachQuery;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class BaseController {

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("query", new SeachQuery());
        return "index";
    }

    @PostMapping("/")
    public String submitQuery(@ModelAttribute SeachQuery query, Model model) {
        model.addAttribute("query", query);
        return "result";
    }

}
