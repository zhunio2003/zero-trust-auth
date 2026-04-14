package com.mazr.zerotrust.authservice.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RegisterRequest {

  @NotBlank
  @Email
  private String email;

  @NotBlank
  @Size(min = 12)
  @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
    message = "Password must contain at least one uppercase, one lowercase and one number"
  )
  private String password;

  @NotBlank
  private String role;

  @NotBlank
  private String department;

}

