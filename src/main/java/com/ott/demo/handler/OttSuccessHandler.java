package com.ott.demo.handler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.web.authentication.ott.OneTimeTokenGenerationSuccessHandler;
import org.springframework.security.web.authentication.ott.RedirectOneTimeTokenGenerationSuccessHandler;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class OttSuccessHandler implements OneTimeTokenGenerationSuccessHandler
{
	private final JavaMailSender javaMailSender;
	private final String sender;
	private final OneTimeTokenGenerationSuccessHandler redirectHandler;

	public OttSuccessHandler(JavaMailSender javaMailSender, @Value("${spring.mail.username}") String sender)
	{
		this.javaMailSender = javaMailSender;
		this.sender = sender;
		this.redirectHandler = new RedirectOneTimeTokenGenerationSuccessHandler("/ott/sent");
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, OneTimeToken oneTimeToken) throws IOException, ServletException
	{
		// generate and send magic link (One time token)
		UriComponentsBuilder token = UriComponentsBuilder
				.fromUriString(UrlUtils.buildFullRequestUrl(request))
				.replacePath(request.getContextPath())
				.path("/login/ott")
				.queryParam("token", oneTimeToken.getTokenValue());

		final String magicLink = token.toUriString();
		log.info("One time token : {}", magicLink);

		sendOttNotification(oneTimeToken, magicLink);
		redirectHandler.handle(request, response, oneTimeToken);
	}

	private void sendOttNotification(OneTimeToken oneTimeToken, String magicLink)
	{
		try
		{
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom("Amlandeep <" + sender + ">");
			message.setTo(getEmail().get(oneTimeToken.getUsername()));
			message.setSubject("One Time Token - Magic Link ");

			String messageBody = """
					 Hello %s,
					        \s
					 Use the following link to sign in to the application:
					        \s
					 %s
					        \s
					 This link is valid for a limited time. If you did not request this, please ignore this email.
					        \s
					\s""".formatted(oneTimeToken.getUsername(), magicLink);

			message.setText(messageBody);
			javaMailSender.send(message);
			log.info("One time token sent in email to {} at {}", oneTimeToken.getUsername(), LocalDateTime.now());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private Map<String, String> getEmail()
	{
		Map<String, String> emailMap = new HashMap<>();
		emailMap.put("amlandeep", "XXX@gmail.com"); // replace XXX with your email
		return emailMap;
	}
}
