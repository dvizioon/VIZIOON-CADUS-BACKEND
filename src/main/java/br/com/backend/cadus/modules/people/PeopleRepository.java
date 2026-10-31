package br.com.backend.cadus.modules.people;

import br.com.backend.cadus.modules.people.entity.PersonEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PeopleRepository
    extends JpaRepository<PersonEntity, String>, JpaSpecificationExecutor<PersonEntity> {

  boolean existsByCpfDigits(String cpfDigits);

  boolean existsByCpfDigitsAndIdNot(String cpfDigits, String id);

  Optional<PersonEntity> findByConfirmationToken(String confirmationToken);
}
