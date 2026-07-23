package turuq.backend.entities;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Document(collection = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    private String id;

    @NotBlank(message = "Name is required")
    @Field("name")
    private String name;

    @NotBlank(message = "Password is required")
    @Field("password")
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Indexed(unique = true, name = "idx_email_unique")
    @Field("email")
    private String email;

    @Min(value = 0, message = "Age must be a positive number")
    @Indexed(name = "idx_age")
    @Field("age")
    private Integer age;

    @CreatedDate
    @Field("created_at")
    private Instant createdAt;
}
