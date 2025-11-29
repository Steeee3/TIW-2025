package it.polimi.astalavista.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class SpaForwardController {
  @GetMapping("/")
  public String root() { return "forward:/index.html"; }

  @GetMapping("/buy")
  public String buy() { return "forward:/index.html"; }

  @GetMapping("/sell")
  public String sell() { return "forward:/index.html"; }
  
  @GetMapping("/won")
  public String won() { return "forward:/index.html"; }

  @GetMapping("/home")
  public String home() { return "forward:/index.html"; }

  @GetMapping("/details/{id}")
  public String details(@PathVariable String id) { return "forward:/index.html"; }

  @GetMapping("/offer/{id}")
  public String offer(@PathVariable String id) { return "forward:/index.html"; }
}
