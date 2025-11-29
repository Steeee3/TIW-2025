package it.polimi.astalavista.api;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CsrfApi {
  @GetMapping("/api/csrf")
  public Map<String,String> csrf(org.springframework.security.web.csrf.CsrfToken t){
    return Map.of("token", t.getToken(), "headerName", t.getHeaderName());
  }
}
