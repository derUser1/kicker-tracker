package de.deruser.kickertracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class HomeController {

    /* Home */
    @GetMapping("/")
    public String home(){
        return "index";
    }

    /** login on master **/
    @GetMapping("/login")
    public String login(){
        return "login";
    }

}
