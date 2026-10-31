package br.com.backend.cadus.modules.people;

import br.com.backend.cadus.modules.people.entity.PersonEntity;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class PeopleSeeder implements ApplicationRunner {

  private final PeopleRepository people;

  public PeopleSeeder(PeopleRepository people) {
    this.people = people;
  }

  @Override
  public void run(ApplicationArguments args) {
    if (people.count() > 0) {
      return;
    }
    people.save(
        person(
            "marina-costa",
            "52998224725",
            "529.982.247-25",
            "Marina Costa",
            "Operadora de terminal",
            "3500.00",
            "2026-11-01",
            "2027-10-31",
            "https://randomuser.me/api/portraits/women/44.jpg",
            "marina.costa@example.com",
            "(85) 90000-0000",
            "Rua do Porto, Bairro Centro",
            "--",
            true,
            "Cadastro demonstrativo para operação portuária.",
            true,
            "Documento de identidade",
            "Comprovante de CPF",
            "Conta de energia"));
    people.save(
        person(
            "joao-almeida",
            "39053344705",
            "390.533.447-05",
            "João Almeida",
            "Auxiliar de cais",
            "2800.00",
            "2025-03-15",
            "2026-03-14",
            "https://randomuser.me/api/portraits/men/32.jpg",
            "joao.almeida@example.com",
            "(85) 98888-0000",
            "Av. Beira Mar, 100",
            "100",
            false,
            "",
            false,
            "",
            "",
            ""));
    people.save(
        person(
            "ana-ribeiro",
            "24843803480",
            "248.438.034-80",
            "Ana Ribeiro",
            "Analista de contratos",
            "4200.00",
            "2026-01-10",
            "2027-01-09",
            "https://randomuser.me/api/portraits/women/68.jpg",
            "ana.ribeiro@example.com",
            "(85) 97777-0000",
            "Rua das Dunas",
            "50",
            false,
            "",
            true,
            "",
            "",
            ""));
  }

  private static PersonEntity person(
      String id,
      String cpfDigits,
      String cpf,
      String name,
      String cargo,
      String salary,
      String contractStart,
      String contractEnd,
      String photoUrl,
      String email,
      String phone,
      String address,
      String number,
      boolean noNumber,
      String notes,
      boolean active,
      String rgDescription,
      String cpfDocDescription,
      String residenceDescription) {
    PersonEntity entity = new PersonEntity();
    entity.setId(id);
    entity.setCpfDigits(cpfDigits);
    entity.setCpf(cpf);
    entity.setName(name);
    entity.setCargo(cargo);
    entity.setSalary(new BigDecimal(salary));
    entity.setContractStart(LocalDate.parse(contractStart));
    entity.setContractEnd(LocalDate.parse(contractEnd));
    entity.setPhotoUrl(photoUrl);
    entity.setEmail(email);
    entity.setPhone(phone);
    entity.setAddress(address);
    entity.setNumber(number);
    entity.setNoNumber(noNumber);
    entity.setUf("CE");
    entity.setCity("Fortaleza");
    entity.setCep(
        switch (id) {
          case "marina-costa" -> "60000-000";
          case "ana-ribeiro" -> "60180-000";
          default -> "60165-121";
        });
    entity.setNotes(notes);
    entity.setActive(active);
    entity.setEmailConfirmed(active);
    entity.setAttachmentDescriptions(rgDescription, cpfDocDescription, residenceDescription);
    return entity;
  }
}
