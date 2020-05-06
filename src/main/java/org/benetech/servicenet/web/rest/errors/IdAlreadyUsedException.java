package org.benetech.servicenet.web.rest.errors;

import org.zalando.problem.Status;

public class IdAlreadyUsedException extends BadRequestAlertException {

    private static final long serialVersionUID = 1L;

    public IdAlreadyUsedException() {
        super(ErrorConstants.ID_ALREADY_USED_TYPE, "Id already used!", "clientManagement", "clientexists",
            Status.CONFLICT);
    }
}
