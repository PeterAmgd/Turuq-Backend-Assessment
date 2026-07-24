package turuq.backend;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import turuq.backend.exception.DuplicateEmailException;
import turuq.backend.exception.ResourceNotFoundException;
import turuq.backend.payloads.requests.UserCreateRequest;
import turuq.backend.repositories.UserRepository;
import turuq.backend.services.UserService;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void createUser_throwsDuplicateEmailException_whenEmailAlreadyExists() {
        UserService service = new UserService(userRepository,passwordEncoder);
        UserCreateRequest request = new UserCreateRequest();
        request.setName("Ada Lovelace");
        request.setEmail("ada@example.com");
        request.setAge(30);

        when(userRepository.existsByEmail("ada@example.com")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> service.createUser(request));
    }

    @Test
    void getUserById_throwsResourceNotFoundException_whenIdDoesNotExist() {
        UserService service = new UserService(userRepository, passwordEncoder);
        // A syntactically valid ObjectId that simply isn't in the (mocked) database.
        String validButAbsentId = "64b64f1f2f8fb814b56fa181";

        when(userRepository.findById(validButAbsentId)).thenReturn(java.util.Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getUserById(validButAbsentId));
    }
}
