package it.polimi.astalavista.api;

import java.security.Principal;
import java.time.Instant;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UsersApi {

  @GetMapping("/me")
  public Map<String, Object> me(Principal principal) {
    return Map.of(
      "username", principal.getName(),
      "serverTime", Instant.now().toString()
    );
  } 
}
