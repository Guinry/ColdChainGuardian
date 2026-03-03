package com.coldchain.guardian.app.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/auth/**", "/error").permitAll()  // 允许认证接口和错误页面无需认证
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()  // 允许Swagger接口
                        .requestMatchers("/ws/**").permitAll()  // 允许WebSocket接口
                        .requestMatchers("/api/monitor/**").hasAnyRole("ADMIN", "MANAGER", "USER")  // 允许监控接口
                        .requestMatchers("/api/areas/**").hasAnyRole("ADMIN", "MANAGER", "USER")  // 允许库区管理接口
                        .requestMatchers("/api/work-orders/**").hasAnyRole("ADMIN", "MANAGER", "USER")  // 允许工单管理接口
                        .requestMatchers("/api/alerts/**").hasAnyRole("ADMIN", "MANAGER", "USER")  // 允许告警管理接口
                        .anyRequest().authenticated()  // 其他请求都需要认证
                )
                .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}