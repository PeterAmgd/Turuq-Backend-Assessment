package turuq.backend.payloads.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    private String name;

    @Email(message = "email must be a valid email address")
    private String email;

    @Min(value = 0, message = "age must be a positive number")
    @Max(value = 150, message = "age must be realistic")
    private Integer age;
}
