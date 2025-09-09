package com.empresa.proyecto.model;

public record StudentRequest(
        Long id,
        String firstName,
        String lastName,
        String email
) {
}
