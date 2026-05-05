package com.coldchain.guardian.app.security;

import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer; // 🌟 导入此类
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // 🌟 核心终极杀招：直接在 WebSecurity 层面忽略跨域预检和 SSE 流式接口的二次拦截
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                // 完全忽略 OPTIONS 请求的拦截
                .requestMatchers(HttpMethod.OPTIONS, "/**")
                // 完全忽略 AI 流式接口的底层拦截 (因为你已经在 JwtFilter 里验证过 Token 了)
                .requestMatchers("/api/ai-assistant/chat/stream");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        .dispatcherTypeMatchers(DispatcherType.ASYNC).permitAll()
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll() // 🌟 放行 Error 调度

                        .requestMatchers("/api/auth/**", "/api/wx/auth/login", "/api/wx/auth/login-manual", "/api/wx/auth/login-manual-standard", "/error").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/ws/**").permitAll()

                        .requestMatchers("/api/ai-assistant/**").hasAnyRole("ADMIN", "MANAGER", "USER", "EMPLOYEE", "STOCK_MANAGER", "TECHNICIAN")
                        .requestMatchers("/api/monitor/**").hasAnyRole("ADMIN", "MANAGER", "USER", "EMPLOYEE", "STOCK_MANAGER", "TECHNICIAN")
                        .requestMatchers("/api/areas/**").hasAnyRole("ADMIN", "MANAGER", "USER", "EMPLOYEE", "STOCK_MANAGER", "TECHNICIAN")
                        .requestMatchers("/api/devices/**").hasAnyRole("ADMIN", "MANAGER", "USER", "EMPLOYEE", "STOCK_MANAGER", "TECHNICIAN")
                        .requestMatchers("/api/work-orders/**").hasAnyRole("ADMIN", "MANAGER", "USER", "EMPLOYEE", "STOCK_MANAGER", "TECHNICIAN")
                        .requestMatchers("/api/alerts/**").hasAnyRole("ADMIN", "MANAGER", "USER", "EMPLOYEE", "STOCK_MANAGER", "TECHNICIAN")
                        .requestMatchers("/api/dashboard/**").hasAnyRole("ADMIN", "MANAGER", "USER", "EMPLOYEE", "STOCK_MANAGER", "TECHNICIAN")
                        .requestMatchers("/api/user/me").hasAnyRole("ADMIN", "MANAGER", "USER", "EMPLOYEE", "STOCK_MANAGER", "TECHNICIAN")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Collections.singletonList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
