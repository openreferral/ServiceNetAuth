package org.benetech.servicenet.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import io.github.jhipster.config.JHipsterProperties;
import java.io.IOException;
import java.util.Locale;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.apache.commons.lang3.StringUtils;
import org.benetech.servicenet.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

@Service
public class SendGridMailServiceImpl {

    private static final String MAIL_ENDPOINT = "mail/send";

    private static final String MAIL_CONTENT_TYPE = "text/plain";

    private static final String MAIL_HTML_CONTENT_TYPE = "text/html";

    private static final String USER = "user";

    private static final String BASE_URL = "baseUrl";

    private final Logger log = LoggerFactory.getLogger(SendGridMailServiceImpl.class);

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Autowired
    private JHipsterProperties jHipsterProperties;

    @Autowired
    private SendGrid sendGrid;

    @Value("${registration.sender-address}")
    private String registrationSenderMail;


    private void sendMail(String from, String to, String subj, String content, boolean isHtml) {
        Email fromEmail = new Email(this.getValidEmailAddress(from));
        Email toEmail = new Email(to);
        String contentType = isHtml ? MAIL_HTML_CONTENT_TYPE : MAIL_CONTENT_TYPE;
        Content mailContent = new Content(contentType, content);
        Mail mail = new Mail(fromEmail, subj, toEmail, mailContent);

        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint(MAIL_ENDPOINT);
            request.setBody(mail.build());
            Response response = sendGrid.api(request);
            log.debug(response.getStatusCode() + " " + response.getBody() + " " + response.getHeaders());
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    private String getValidEmailAddress(String email) {
        if (StringUtils.isNotEmpty(email)) {
            try {
                InternetAddress emailAddr = new InternetAddress(email);
                emailAddr.validate();
            } catch (AddressException ex) {
                return registrationSenderMail;
            }
        } else {
            return registrationSenderMail;
        }

        return email;
    }

    @Async
    public void sendEmailFromTemplate(User user, String templateName, String titleKey, String baseUrl) {
        if (user.getEmail() == null) {
            log.debug("Email doesn't exist for user '{}'", user.getLogin());
            return;
        }
        Locale locale = Locale.forLanguageTag(user.getLangKey());
        Context context = new Context(locale);
        context.setVariable(USER, user);
        context.setVariable(BASE_URL, baseUrl);
        String content = templateEngine.process(templateName, context);
        String subject = messageSource.getMessage(titleKey, null, locale);
        sendMail(registrationSenderMail, user.getEmail(), subject, content, true);
    }

    @Async
    public void sendActivationEmail(User user, String baseUrl) {
        log.debug("Sending activation email to '{}'", user.getEmail());
        sendEmailFromTemplate(user, "mail/activationEmail",
            "email.activation.title", baseUrl);
    }

    @Async
    public void sendCreationEmail(User user, String baseUrl) {
        log.debug("Sending creation email to '{}'", user.getEmail());
        sendEmailFromTemplate(user, "mail/creationEmail",
            "email.activation.title", baseUrl);
    }

    @Async
    public void sendPasswordResetMail(User user, String baseUrl) {
        log.debug("Sending password reset email to '{}'", user.getEmail());
        sendEmailFromTemplate(user, "mail/passwordResetEmail",
            "email.reset.title", baseUrl);
    }
}
