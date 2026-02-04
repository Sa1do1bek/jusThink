package com.example.backend.services.user;

import com.example.backend.enums.Role;
import com.example.backend.exceptions.AlreadyExistsException;
import com.example.backend.exceptions.IllegalActionException;
import com.example.backend.exceptions.ResourceNotFoundException;
import com.example.backend.models.Image;
import com.example.backend.models.UserModel;
import com.example.backend.repositories.ImageRepository;
import com.example.backend.repositories.UserRepository;
import com.example.backend.requests.CreateUserRequest;
import com.example.backend.requests.UpdateUserRequest;
import com.example.backend.responses.UserResponse;
import com.example.backend.services.auth.email.EmailVerificationService;
import com.example.backend.services.image.ImageStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements IUserService{

    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final ImageStorageService storage;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;

    public boolean ownerChecker(String email,UUID ownerId) {
        return this.getUserByEmail(email)
                .getId()
                .equals(ownerId);
    }

    @Override
    public List<UserModel> getAllUsers(String email) throws IllegalAccessException {
        if (!this.getUserByEmail(email).getRole().equals(Role.ADMIN))
            throw new IllegalAccessException("Current user cannot access to this action!");

        return userRepository.findAll();
    }

    @Override
    public UserModel getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));
    }

    @Override
    public UserModel getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));
    }

    @Override
    public UserModel createUser(CreateUserRequest request) {

        String passwordError = validatePassword(request.password());
        if (passwordError != null)
            throw new IllegalActionException(passwordError);

        String emailError = validateEmail(request.email());
        if (emailError != null)
            throw new IllegalActionException(emailError);

        if (request.role() == Role.ADMIN)
            throw new IllegalActionException("Current user cannot access to this action!");

        return Optional.of(request)
                .filter(req -> !userRepository.existsByEmail(req.email()))
                .map(req -> {
                    UserModel user = new UserModel();
                    user.setNickname(req.nickName());
                    user.setEmail(req.email());
                    user.setPassword(passwordEncoder.encode(req.password()));
                    user.setRole(Role.valueOf(req.role().toString().toUpperCase(Locale.ROOT)));
                    user.setEmailVerified(false);

                    user = userRepository.save(user);
                    emailVerificationService.createVerificationToken(user);
                    return user;

                }).orElseThrow(() ->
                        new AlreadyExistsException(
                                "User with the email of " + request.email() + " exists!"
                        ));
    }


    @Override
    public UserModel updateUser(
            UUID userId,
            UpdateUserRequest request,
            MultipartFile imageFile,
            String email
    ) throws IllegalAccessException {

        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserModel currentUser = getUserByEmail(email);
        if (!user.getId().equals(currentUser.getId())
                && currentUser.getRole() != Role.ADMIN) {
            throw new IllegalAccessException("Current user cannot access to this action!");
        }

        if (!user.getEmail().equals(request.email())
                && userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already in use");
        }

        user.setEmail(request.email());

        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        if (request.nickName() != null && !request.nickName().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.nickName()));
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            Image image = user.getImage() != null ? user.getImage() : new Image();

            image.setFileName(imageFile.getOriginalFilename());
            image.setFileType(imageFile.getContentType());
            image.setPath(storage.save(userId, imageFile));

            user.setImage(image);
        }

        return userRepository.save(user);
    }

    @Override
    public void deleteUser(UUID userId, String email) throws IllegalAccessException {
        if (!this.getUserByEmail(email).getRole().equals(Role.ADMIN))
            throw new IllegalAccessException("Current user cannot access to this action!");

        userRepository.findById(userId).ifPresentOrElse(userRepository :: delete, () -> {
                    throw new ResourceNotFoundException("User not found!");
                });
    }

    @Override
    public void deleteImageByUserId(UUID userId, String email){
        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserModel currentUser = getUserByEmail(email);
        if ((!user.getId().equals(currentUser.getId())) && (currentUser.getRole() != Role.ADMIN))
            try {
                throw new IllegalAccessException("Current user cannot access to this action!");
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

        Image image = user.getImage();
        if (image == null) return;

        storage.delete(image.getPath());
        imageRepository.delete(image);
    }

    public void saveUser(UserModel user) {
        userRepository.save(user);
    }

    public UserResponse converterToUserResponse(UserModel user) {
        return new UserResponse(
                user.getId(),
                user.getNickname(),
                user.getEmail(),
                user.getRole(),
                user.getImage(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private String validatePassword(String password) {
        if (password == null || password.length() < 8)
            return "Password must be at least 8 characters long";
//        if (!password.matches(".*[A-Z].*"))
//            return "Password must contain at least one uppercase letter";
//        if (!password.matches(".*[a-z].*"))
//            return "Password must contain at least one lowercase letter";
//        if (!password.matches(".*[0-9].*"))
//            return "Password must contain at least one number";
//        if (!password.matches(".*[!@#$%^&*(),.?\":{}|<>].*"))
//            return "Password must contain at least one special character";
        return null;
    }

    private String validateEmail(String email) {
        if (email == null || email.trim().isEmpty())
            return "Email must not be empty";
        if (email.contains(" "))
            return "Email must not contain spaces";

        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

        if (!email.matches(emailRegex))
            return "Email format is invalid";
        return null;
    }

}
