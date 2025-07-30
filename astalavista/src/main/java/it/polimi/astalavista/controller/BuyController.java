package it.polimi.astalavista.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BuyController {
    
    @GetMapping("/buy")
    public String getMethodName() {
        return "buy";
    }
}
