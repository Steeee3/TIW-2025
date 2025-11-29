package it.polimi.astalavista.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;

import it.polimi.astalavista.exceptions.CountryNotAvailableException;
import it.polimi.astalavista.service.CountryService;
import it.polimi.astalavista.service.LoginService;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
public class LoginController {
    
    @Autowired
    private LoginService loginService;

    @Autowired
    private CountryService countryService;

    @GetMapping({"/login", "/register"})
    public String showLoginPage(HttpServletRequest request, Model model) {
        request.getSession(true);

        model.addAttribute("countries", countryService.getAllCountries());
        model.addAttribute("activeForm", "login");
        return "login";
    }
    
    @PostMapping("/register")
    public String processRegistration(
        @RequestParam String username,
        @RequestParam String password, 
        @RequestParam String name,
        @RequestParam String surname,
        @RequestParam int countryId,
        @RequestParam String city,
        @RequestParam String street,
        @RequestParam String postalCode,
        Model model
        ) {

            boolean success = false;

            String country = countryService.getCoutryNameById(countryId);
            try {
                success = loginService.register(username, password, name, surname, country, city, street, postalCode);
            } catch (CountryNotAvailableException e) {
                model.addAttribute("error", "Nazione non supportata");
                model.addAttribute("activeForm", "signup");
                model.addAttribute("countries", countryService.getAllCountries());
                return "login";
            }

            if (success) {
                return "redirect:/";
            } else {
                model.addAttribute("error", "Username già in uso");
                model.addAttribute("activeForm", "signup");
                model.addAttribute("countries", countryService.getAllCountries());
                return "login";
            }
    }
}
