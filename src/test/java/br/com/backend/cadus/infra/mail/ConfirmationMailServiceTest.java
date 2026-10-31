package br.com.backend.cadus.infra.mail;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import br.com.backend.cadus.modules.people.entity.PersonEntity;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

class ConfirmationMailServiceTest {

  @Test
  void skipsWhenUsernameBlank() {
    JavaMailSender mail = mock(JavaMailSender.class);
    PersonEntity person = new PersonEntity();
    person.setName("Ana");
    person.setEmail("ana@example.com");
    person.setConfirmationToken("token");

    new ConfirmationMailService(mail, "http://localhost:8080", "", "").send(person);

    verify(mail, never()).send(any(SimpleMailMessage.class));
  }
}
