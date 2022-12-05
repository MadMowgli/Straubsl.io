package SearchEngine.Controllers;

import PreProcessor.Models.WARCModel;
import SearchEngine.Models.SearchEngineDummy;
import SearchEngine.Models.SearchQuery;
import SearchEngine.Models.SearchEngine;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;


@Controller
public class MappingController {

    // -------------------------- Fields
    private final SearchEngine searchEngine = new SearchEngine();
    // private final SearchEngineDummy searchEngine = new SearchEngineDummy();


    // -------------------------- Mappings
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("query", new SearchQuery());
        return "index";
    }

    @PostMapping("/")
    public String submitQuery(@ModelAttribute SearchQuery query, Model model) {

        // Grab the search query & pass it to the search engine to transform into vector
        model.addAttribute("query", query);
        ArrayList<WARCModel> warcModels = searchEngine.search(query);
        model.addAttribute("warcModels", warcModels);

        return "result";
    }



}
