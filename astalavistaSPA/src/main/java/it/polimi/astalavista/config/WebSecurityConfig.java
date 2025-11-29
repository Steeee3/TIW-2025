package it.polimi.astalavista.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import it.polimi.astalavista.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
      http
        .authorizeHttpRequests(auth -> auth
          .requestMatchers("/", "/index.html", "/login", "/register",
                          "/favicon.ico", "/css/**", "/js/**", "/images/**", "/assets/**", "/uploads/**").permitAll()
          .requestMatchers("/api/**").authenticated()
          .anyRequest().permitAll()
        )
        .csrf(csrf -> csrf
          .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
          .ignoringRequestMatchers("/login", "/register")
        )
        .formLogin(form -> form
          .loginPage("/login")
          .loginProcessingUrl("/login")
          .defaultSuccessUrl("/", true)
          .failureUrl("/login?error=true")
          .permitAll()
        )
        .rememberMe(r -> r.key("superSecretKey").tokenValiditySeconds(86400).userDetailsService(customUserDetailsService))
        .logout(l -> l.logoutUrl("/logout").logoutSuccessUrl("/login?logout=true").invalidateHttpSession(true).deleteCookies("JSESSIONID","remember-me").permitAll());

      return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
        throws Exception {
        return config.getAuthenticationManager();
    }
}