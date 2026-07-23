package turuq.backend.payloads.requests;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {

    @NotBlank(message = "name is required")
    private String name;

    @NotBlank(message = "password is required")
    @Size(min = 8, max = 100, message = "password must be between 8 and 100 characters")
    private String password;

    @NotBlank(message = "email is required")
    @Email(message = "email must be a valid email address")
    private String email;

    @Min(value = 0, message = "age must be a positive number")
    @Max(value = 100, message = "age must be realistic")
    private Integer age;

}
