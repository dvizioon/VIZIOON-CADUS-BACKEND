package br.com.backend.cadus.infra.mail;

import br.com.backend.cadus.modules.people.entity.PersonEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class ConfirmationMailService {

  private final JavaMailSender mail;
  private final String publicUrl;
  private final String from;
  private final String username;

  public ConfirmationMailService(
      JavaMailSender mail,
      @Value("${app.public-url:http://localhost:8080}") String publicUrl,
      @Value("${app.mail.from:}") String from,
      @Value("${spring.mail.username:}") String username) {
    this.mail = mail;
    this.publicUrl = publicUrl.endsWith("/") ? publicUrl.substring(0, publicUrl.length() - 1) : publicUrl;
    this.from = from;
    this.username = username;
  }

  public boolean configured() {
    return username != null && !username.isBlank();
  }

  public void send(PersonEntity person) {
    if (!configured() || person.getConfirmationToken() == null || person.getConfirmationToken().isBlank()) {
      return;
    }
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(from == null || from.isBlank() ? username : from);
    message.setTo(person.getEmail());
    message.setSubject("Confirme seu cadastro");
    message.setText(
        "Olá "
            + person.getName()
            + ",\n\nConfirme seu e-mail para ativar o cadastro:\n"
            + publicUrl
            + "/api/people/confirm/"
            + person.getConfirmationToken()
            + "\n");
    mail.send(message);
  }
}
