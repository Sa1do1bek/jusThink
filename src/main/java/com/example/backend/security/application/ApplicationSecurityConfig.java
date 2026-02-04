package com.example.backend.security.application;

import com.example.backend.services.auth.ApplicationUserService;
import com.example.backend.jwt.JwtEmailAndPasswordAuthenticationFilter;
import com.example.backend.jwt.JwtVerifier;
import com.example.backend.services.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;

import static com.example.backend.enums.Role.*;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class ApplicationSecurityConfig extends WebSecurityConfigurerAdapter {

    private final PasswordEncoder passwordEncoder;
    private final JwtVerifier jwtVerifier;
    private final SecurityCorsConfig corsConfig;
    private final ApplicationUserService applicationUserService;
    private final JwtService jwtService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        JwtEmailAndPasswordAuthenticationFilter jwtFilter =
                new JwtEmailAndPasswordAuthenticationFilter(jwtService);

        jwtFilter.setAuthenticationManager(authenticationManagerBean());

        http
                .cors()
                    .configurationSource(corsConfig.corsConfigurationSource())
                .and()
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/ws/**", "/ws/info").permitAll()
                .antMatchers("/", "index", "/css/*", "/js/*").permitAll()
                .antMatchers("/api/jasThink/players/**", "/api/jasThink/auth/**").permitAll()
                .antMatchers("/management/api/**").hasRole(ADMIN.name())
                .antMatchers("/api/**").hasAnyRole(USER.name(), CREATOR.name(), ADMIN.name())
                .anyRequest().authenticated()
                .and()
                .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .headers().frameOptions().sameOrigin()
                .and()
                .addFilter(jwtFilter)
                .addFilterAfter(jwtVerifier, JwtEmailAndPasswordAuthenticationFilter.class);
//                .oauth2Client(Customizer.withDefaults());

    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(daoAuthenticationProvider());
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder);
        provider.setUserDetailsService(applicationUserService);
        return provider;
    }
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

}
