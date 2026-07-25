package com.uce.hours_service.cqrs.commands;

public final class ValidateHoursCommand {
    private final Long id;
    private final String tutorId;
    private final boolean aprobado;

    public ValidateHoursCommand(Long id, String tutorId, boolean aprobado) {
        this.id = id;
        this.tutorId = tutorId;
        this.aprobado = aprobado;
    }

    public Long getId() { return id; }
    public String getTutorId() { return tutorId; }
    public boolean isAprobado() { return aprobado; }
}
