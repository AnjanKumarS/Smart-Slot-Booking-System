package com.smartslot.config;

import com.smartslot.config.FirebaseAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import javax.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private FirebaseAuthenticationFilter firebaseAuthenticationFilter;
    
    @Value("${app.development.mode:false}")
    private boolean developmentMode;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .cors().and()
            .csrf().disable()
            .addFilterBefore(firebaseAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        if (developmentMode) {
            // Development mode: Allow public access to most endpoints
            http.authorizeRequests()
                .antMatchers("/admin/login").permitAll() // Allow admin login first
                .antMatchers(
                    "/", "/login", "/register", "/admin-login", "/api/venues", "/dashboard", "/test-auth", "/test-venues", "/health", "/booking", "/book", "/logout", "/verify-booking", "/user/bookings",
                    "/css/**", "/js/**", "/static/**", "/webjars/**", "/favicon.ico", "/images/**"
                ).permitAll()
                .antMatchers("/user/bookings", "/chatbot", "/user-dashboard", 
                           "/admin-dashboard", "/staff-dashboard", "/admin/panel", "/api/admin/**").authenticated()
                .anyRequest().permitAll()
                .and()
                .formLogin().disable()
                .httpBasic().disable()
                .logout().disable();
        } else {
            // Production mode: Strict authentication
            http.authorizeRequests()
                .antMatchers("/admin/login").permitAll() // Allow admin login first
                .antMatchers(
                    "/login", "/register", "/admin-login", "/api/auth/**", "/api/venues", "/logout", "/verify-booking", "/health",
                    "/css/**", "/js/**", "/static/**", "/webjars/**", "/favicon.ico", "/images/**"
                ).permitAll()
                .antMatchers("/", "/dashboard", "/booking", "/book", "/user/bookings", "/chatbot", "/user-dashboard", 
                           "/admin-dashboard", "/staff-dashboard", "/admin/panel", "/api/admin/**").authenticated()
                .anyRequest().authenticated()
                .and()
                .formLogin().disable()
                .httpBasic().disable()
                .logout().disable();
        }
        
        // Add error handling for debugging
        http.exceptionHandling()
            .accessDeniedPage("/error/403")
            .authenticationEntryPoint((request, response, authException) -> {
                System.out.println("Authentication failed for: " + request.getRequestURI());
                System.out.println("Auth exception: " + authException.getMessage());
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
            });
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
} 