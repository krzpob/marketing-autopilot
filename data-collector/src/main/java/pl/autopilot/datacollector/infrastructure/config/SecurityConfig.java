package pl.autopilot.datacollector.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF wyłączone dla endpointów API — OAuth callback i Meta webhook
            // używają przekierowań i zewnętrznych callbacków które nie wysyłają CSRF token
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(
                    "/oauth/instagram/**",
                    "/meta/**",
                    "/actuator/**"
                )
            )
            .authorizeHttpRequests(auth -> auth
                // Endpointy OAuth — muszą być publiczne (użytkownik trafia tu z Instagrama)
                .requestMatchers("/oauth/instagram/**").permitAll()
                // Wymagany przez Meta — musi być dostępny bez autoryzacji
                .requestMatchers("/meta/**").permitAll()
                // Actuator — healthcheck dla Dockera i monitoringu
                .requestMatchers("/actuator/health").permitAll()
                // Wszystko inne — do zabezpieczenia w późniejszym etapie
                .anyRequest().permitAll()   // TODO: B-Security — zmienić na authenticated()
            );

        return http.build();
    }
}