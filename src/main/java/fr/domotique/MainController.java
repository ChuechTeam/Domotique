package fr.domotique;

import fr.domotique.data.User;
import fr.domotique.data.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

/**
 * A little demo controller for the home page
 *
 * @author Dynamic
 */
@Controller
public class MainController {
    // The logger, so we can output messages nicely to the console.
    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    // The UserRepository to query the MySQL database without writing SQL.
    private final UserRepository userRepository;

    // This controller will be automatically called by Spring,
    // who will give us the UserRepository automatically!
    public MainController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String index(Model model) {
        // Find all the users from the SQL database.
        // Try to run this request in MySQL Workbench to see that this works:
        //      INSERT INTO user (email, gender, pass_hash) VALUES ('abc@mail.fr', 1, 'mdp')
        List<User> users = userRepository.findAll();

        // Print this to the console as an INFO
        log.info("Number of users found: {}", users.size());

        // Give the entire list to the HTML template!
        model.addAttribute("users", users);
        return "home";
    }
}
