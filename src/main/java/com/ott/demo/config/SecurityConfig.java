package com.ott.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig
{
	@Bean
	protected SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception
	{
		return http
				.authorizeHttpRequests(
						auth -> auth
								.requestMatchers("/login/ott").permitAll()
								.requestMatchers("/ott/sent").permitAll()
								.anyRequest().authenticated())
				.formLogin(Customizer.withDefaults())
				.oneTimeTokenLogin(Customizer.withDefaults())
				.build();
	}

	@Bean
	InMemoryUserDetailsManager inMemoryUserDetailsManager()
	{
		UserDetails user = User.withUsername("amlandeep")
				.password("{noop}password")
				.build();
		return new InMemoryUserDetailsManager(user);
	}
}
