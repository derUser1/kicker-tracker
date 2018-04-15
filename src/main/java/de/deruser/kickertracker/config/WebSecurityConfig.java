package de.deruser.kickertracker.config;

import de.deruser.kickertracker.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private PlayerService playerService;
    private final String adminUsername;
    private final String adminPassword;

    @Autowired
    public WebSecurityConfig(
            @Value("admin.username") String adminUsername,
            @Value("admin.password") String adminPassword,
            final PlayerService playerService) {
        this.playerService = playerService;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/", "/matches", "/css/**",
                        "/js/**", "/images/**", "/**/favicon.ico").permitAll()
                .anyRequest().authenticated()
                .and()
//                .csrf().disable()
            .formLogin()
                .loginPage("/login").permitAll()
                .and()
            .logout()
                .permitAll();
    }

    @Bean
    @Override
    public UserDetailsService userDetailsService() {

        List<UserDetails> users = playerService.getAllPlayers().stream()
                .map(p -> User.builder()
                        .username(p.getName())
                        .password(p.getPassword())
                        .roles(p.getRoles().toArray(new String[0]))
                        .build())
                .collect(Collectors.toList());

        //TODO: store admin in DB
        users.add(User.withDefaultPasswordEncoder()
                .username(adminUsername)
                .password(adminPassword)
                .roles("ADMIN")
                .build());

        return new InMemoryUserDetailsManager(users);
    }
}
