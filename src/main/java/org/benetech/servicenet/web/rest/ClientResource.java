package org.benetech.servicenet.web.rest;

import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.benetech.servicenet.domain.Client;
import org.benetech.servicenet.security.AuthoritiesConstants;
import org.benetech.servicenet.service.ClientService;
import org.benetech.servicenet.service.dto.ClientDTO;
import org.benetech.servicenet.web.rest.errors.BadRequestAlertException;
import org.benetech.servicenet.web.rest.errors.EmailAlreadyUsedException;
import org.benetech.servicenet.web.rest.errors.IdAlreadyUsedException;
import org.benetech.servicenet.web.rest.errors.LoginAlreadyUsedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * REST controller for managing clients.
 */
@RestController
@RequestMapping("/api")
public class ClientResource {

    private final Logger log = LoggerFactory.getLogger(ClientResource.class);

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    @Autowired
    private ClientService clientService;

    /**
     * {@code POST  /clients}  : Creates a new client.
     *
     *
     * @param clientDTO the client to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new client, or with status {@code 400 (Bad Request)} if the id is already in use.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     * @throws BadRequestAlertException {@code 400 (Bad Request)} if the id is already in use.
     */
    @PostMapping("/clients")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<ClientDTO> createClient(@Valid @RequestBody ClientDTO clientDTO) throws URISyntaxException {
        log.debug("REST request to save Client : {}", clientDTO);

        if (clientService.findById(clientDTO.getClientId()).isPresent()) {
            throw new IdAlreadyUsedException();
        } else {
            ClientDTO newClient = clientService.createExternalClient(clientDTO);
            return ResponseEntity.created(new URI("/api/clients/" + newClient.getClientId()))
                .headers(HeaderUtil.createAlert(applicationName,  "clientManagement.created", newClient.getClientId()))
                .body(newClient);
        }
    }

    /**
     * {@code PUT /clients} : Updates an existing Client.
     *
     * @param clientDTO the client to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated client.
     * @throws EmailAlreadyUsedException {@code 400 (Bad Request)} if the email is already in use.
     * @throws LoginAlreadyUsedException {@code 400 (Bad Request)} if the login is already in use.
     */
    @PutMapping("/clients")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<ClientDTO> updateClient(@Valid @RequestBody ClientDTO clientDTO) {
        log.debug("REST request to update Client : {}", clientDTO);
        Optional<ClientDTO> existingClient = clientService.findById(clientDTO.getClientId());
        if (existingClient.isPresent() && (!existingClient.get().getClientId().equals(clientDTO.getClientId()))) {
            throw new LoginAlreadyUsedException();
        }
        Optional<ClientDTO> updatedClient = clientService.updateExternalClient(clientDTO);

        return ResponseUtil.wrapOrNotFound(updatedClient,
            HeaderUtil.createAlert(applicationName, "clientManagement.updated", clientDTO.getClientId()));
    }

    /**
     * {@code GET /clients} : get all clients.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body all clients.
     */
    @GetMapping("/clients")
    public ResponseEntity<List<ClientDTO>> getAllClients(Pageable pageable) {
        final Page<Client> page = clientService.getExternalClients(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(page.getContent().stream().map(clientService::toDto).collect(
            Collectors.toList()), headers, HttpStatus.OK);
    }

    /**
     * {@code GET /clients/:id} : get the "id" client.
     *
     * @param id the id of the client to find.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the "id" client, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/clients/{id}")
    public ResponseEntity<ClientDTO> getClient(@PathVariable String id) {
        log.debug("REST request to get Client : {}", id);
        return ResponseUtil.wrapOrNotFound(
            clientService.findById(id));
    }

    /**
     * {@code DELETE /clients/:id} : delete the "id" Client.
     *
     * @param id the id of the client to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/clients/{id}")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<Void> deleteClient(@PathVariable String id) {
        log.debug("REST request to delete Client: {}", id);
        clientService.deleteExternalClient(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createAlert(applicationName,  "clientManagement.deleted", id)).build();
    }
}
