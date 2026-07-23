package turuq.backend.controllers;

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
import turuq.backend.payloads.responses.PagedResponse;
import turuq.backend.payloads.responses.UserResponse;
import turuq.backend.services.UserService;

import java.net.URI;
import java.util.Optional;

/**
 * REST endpoints for managing User Profiles.
 * All routes here require a valid JWT (enforced by SecurityConfig);
 * only /api/auth/token is public.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final PaginationProperties paginationProperties;

    public UserController(UserService userService, PaginationProperties paginationProperties) {
        this.userService = userService;
        this.paginationProperties = paginationProperties;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        UserResponse created = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * GET /api/users?page=0&size=20&age=30
     * page/size support pagination; age is an optional exact-match filter.
     */
    @GetMapping
    public ResponseEntity<PagedResponse<UserResponse>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer age) {
        return ResponseEntity.ok(userService.getUsers(page, size, age));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable String id,
                                                   @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
