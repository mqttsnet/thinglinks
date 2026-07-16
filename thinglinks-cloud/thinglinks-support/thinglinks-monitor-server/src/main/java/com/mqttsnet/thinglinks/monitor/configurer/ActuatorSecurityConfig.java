package com.mqttsnet.thinglinks.monitor.configurer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.StringUtils;

/**
 * @author mqttsnet
 * @since 2026/01/07 17:34
 */
@Configuration
public class ActuatorSecurityConfig {

    @Value("${actuator.security.username:${ACTUATOR_USERNAME:}}")
    private String username;

    @Value("${actuator.security.password:${ACTUATOR_PASSWORD:}}")
    private String password;

    @Value("${actuator.security.roles:ACTUATOR_ADMIN}")
    private String roles;


    // 1. 配置密码加密器（Spring Security 6 强制要求加密）
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. 配置用户（生产环境建议用数据库存储，此处已从配置文件读取）
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        if (!StringUtils.hasText(username)) {
            throw new IllegalStateException("actuator.security.username or ACTUATOR_USERNAME must be configured");
        }
        if (!StringUtils.hasText(password)) {
            throw new IllegalStateException("actuator.security.password or ACTUATOR_PASSWORD must be configured");
        }
        // 仅授予 "ACTUATOR_ADMIN" 角色访问 Actuator
        UserDetails actuatorUser = User.withUsername(username)
                .password(passwordEncoder.encode(password))
                .roles(roles)
                .build();
        return new InMemoryUserDetailsManager(actuatorUser);
    }

    // 3. 配置 Actuator 端点权限规则
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 关闭 CSRF（若 Actuator 仅内部访问，可关闭；外部访问需评估）
                .csrf(AbstractHttpConfigurer::disable)
                // 配置请求权限
                .authorizeHttpRequests(auth -> auth
                        // Actuator 所有端点仅允许 "ACTUATOR_ADMIN" 角色访问
                        .requestMatchers("/actuator/**").hasRole(roles)
                        // 其他业务接口根据需求配置（如允许匿名访问或其他角色）
                        .anyRequest().permitAll()
                )
                // 启用 HTTP Basic 认证（简单场景）；生产环境建议用 OAuth2/JWT 更安全
                .httpBasic(httpBasic -> {
                });

        return http.build();
    }
}
