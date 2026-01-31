package com.dawn.config;

import com.dawn.filter.JwtAuthenticationTokenFilter;
import com.dawn.handler.CustomAuthorizationManager;
import com.dawn.service.impl.UserDetailServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置类
 * 
 * 已升级为 Spring Security 6.x 推荐写法：
 * - 使用 SecurityFilterChain 替代 WebSecurityConfigurerAdapter
 * - 使用 AuthorizationManager 替代 FilterSecurityInterceptor + AccessDecisionManager
 */
@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class WebSecurityConfig {
    
    private final AuthenticationEntryPoint authenticationEntryPoint;    // 处理被动认证检查失败
    private final AccessDeniedHandler accessDeniedHandler;              // 处理授权失败
    
    private final AuthenticationSuccessHandler authenticationSuccessHandler;
    private final AuthenticationFailureHandler authenticationFailureHandler;    // 处理主动登录失败
    
    private final JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;
    private final UserDetailServiceImpl userDetailService;
    
    private final CustomAuthorizationManager customAuthorizationManager;    // 新的授权管理器

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(authProvider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. 禁用 CSRF (前后端分离 JWT 模式不需要)
            .csrf(AbstractHttpConfigurer::disable)
            
            // 2. 登录配置
            .formLogin(form -> form
                .loginProcessingUrl("/users/login")
                .successHandler(authenticationSuccessHandler)
                .failureHandler(authenticationFailureHandler)
            )
            
            // 3. 授权配置 - 使用 AuthorizationManager 进行动态权限控制
            .authorizeHttpRequests(auth -> auth
                    // === 关键修改：添加静态资源放行 ===
                    .requestMatchers(
                            "/favicon.ico",
                            "/doc.html",
                            "/webjars/**",
                            "/v3/api-docs/**",
                            "/static/**",
                            "/public/**"
                    ).permitAll()
                    // 使用自定义 AuthorizationManager 处理所有请求的权限校验
                    .anyRequest().access(customAuthorizationManager)
            )
            
            // 4. 异常处理
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(authenticationEntryPoint) // 配置认证入口点，处理AuthenticationException异常
                .accessDeniedHandler(accessDeniedHandler)   // 配置访问拒绝处理器，处理AccessDeniedException异常
            )
            
            // 5. Session 管理 - 无状态
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // 6. JWT 过滤器
            .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
