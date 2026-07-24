package turuq.backend.services;



import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import turuq.backend.entities.User;
import turuq.backend.exception.BadRequestException;
import turuq.backend.exception.DuplicateEmailException;
import turuq.backend.exception.ResourceNotFoundException;
import turuq.backend.payloads.requests.UserCreateRequest;
import turuq.backend.payloads.requests.UserUpdateRequest;
import turuq.backend.payloads.responses.PagedResponse;
import turuq.backend.payloads.responses.UserResponse;
import turuq.backend.repositories.UserRepository;


@Service
public class UserService {

    private static final int MAX_PAGE_SIZE = 100;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,PasswordEncoder passwordEncoder){
        this.userRepository=userRepository;
        this.passwordEncoder=passwordEncoder;
    }

    public UserResponse createUser(UserCreateRequest request) {
        String email = normalizeEmail(request.getEmail());

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException("A user with email '" + email + "' already exists");
        }

        User user = User.builder()
                .name(request.getName().trim())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .age(request.getAge())
                .build();

        User saved = userRepository.save(user);
        return UserResponse.from(saved);
    }

    public PagedResponse<UserResponse> getUsers(int page, int size, Integer age) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<User> result = (age != null)
                ? userRepository.findByAge(age, pageable)
                : userRepository.findAll(pageable);

        return PagedResponse.from(result.map(UserResponse::from));
    }

    public UserResponse getUserById(String id) {
        User user = findUserOrThrow(id);
        return UserResponse.from(user);
    }

    public UserResponse updateUser(String id, UserUpdateRequest request) {
        User existing = findUserOrThrow(id);
        String email = normalizeEmail(request.getEmail());

        // Allow the email to stay the same on update, but reject it if it now collides
        // with a *different* user's email.
        if (userRepository.existsByEmailAndIdNot(email, id)) {
            throw new DuplicateEmailException("A user with email '" + email + "' already exists");
        }

        existing.setName(request.getName().trim());
        existing.setEmail(email);
        existing.setAge(request.getAge());

        User saved = userRepository.save(existing);
        return UserResponse.from(saved);
    }

    public void deleteUser(String id) {
        User existing = findUserOrThrow(id);
        userRepository.delete(existing);
    }

    // --- helpers ------------------------------------------------------------------

    private User findUserOrThrow(String id) {
        validateObjectId(id);
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    /** Fail fast with a clean 400 instead of letting a malformed id blow up as a 500 inside the driver. */
    private void validateObjectId(String id) {
        if (!StringUtils.hasText(id) || !ObjectId.isValid(id)) {
            throw new BadRequestException("Invalid user id: " + id);
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}

