package com.kryptoeuro.accountmapper.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
						.antMatchers("/auth/idCard").authenticated()
						.and()
						.logout()
						.permitAll();

		http.x509().subjectPrincipalRegex("serialNumber=(.*?),");
	}

	@Autowired
	public <T extends UserDetailsService> void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(idCardUserDetailsService());
	}

	@Bean
	private UserDetailsService idCardUserDetailsService() {
		return IdCardUser::new;
	}
}