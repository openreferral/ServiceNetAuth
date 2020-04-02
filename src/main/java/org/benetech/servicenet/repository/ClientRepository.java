package org.benetech.servicenet.repository;

import java.util.Collection;
import java.util.List;
import org.benetech.servicenet.domain.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for the {@link Client} entity.
 */
public interface ClientRepository extends JpaRepository<Client, String> {
    Page<Client> findAllByClientIdNotIn(Collection<String> clientId, Pageable pageable);
}
