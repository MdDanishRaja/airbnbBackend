package com.codingshuttle.projects.airBnbApp.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class SignUpRequestDto {
    private String email;
    private String password;
    private String name;
    private String gender;
    private LocalDate dateOfBirth;
}
