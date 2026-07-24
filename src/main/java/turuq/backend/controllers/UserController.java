package turuq.backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import turuq.backend.config.PaginationProperties;
import turuq.backend.payloads.requests.UserCreateRequest;
import turuq.backend.payloads.requests.UserUpdateRequest;
import turuq.backend.payloads.responses.ErrorResponse;
import turuq.backend.payloads.responses.PagedResponse;
import turuq.backend.payloads.responses.UserResponse;
import turuq.backend.services.UserService;

import java.net.URI;
import java.util.Optional;


@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "CRUD operations for User Profiles (requires a Bearer JWT)")
@ApiResponse(responseCode = "401", description = "Missing, invalid, or expired JWT",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
public class UserController {

    private final UserService userService;
    private final PaginationProperties paginationProperties;

    public UserController(UserService userService, PaginationProperties paginationProperties) {
        this.userService = userService;
        this.paginationProperties = paginationProperties;
    }

    @Operation(summary = "Create a user profile")
    @ApiResponse(responseCode = "201", description = "Created")
    @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Email already in use",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        UserResponse created = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "List user profiles",
            description = "Paginated, optionally filtered by exact age. Example: /users?age=30&page=0&size=20")
    @GetMapping
    public ResponseEntity<PagedResponse<UserResponse>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer age) {
        return ResponseEntity.ok(userService.getUsers(page, size, age));
    }

    @Operation(summary = "Get a single user profile by id")
    @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Operation(summary = "Full update of an existing user profile")
    @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Email already in use by another user",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable String id,
                                                   @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @Operation(summary = "Delete a user profile")
    @ApiResponse(responseCode = "204", description = "Deleted")
    @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
