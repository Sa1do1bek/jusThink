package com.example.backend.services.user;

import com.example.backend.models.UserModel;
import com.example.backend.requests.CreateUserRequest;
import com.example.backend.requests.UpdateUserRequest;
import com.example.backend.responses.UserResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface IUserService {
    UserModel getUserById(UUID userId);
    UserModel getUserByEmail(String email);
    UserModel createUser(CreateUserRequest request);
    UserModel updateUser(UUID userId, UpdateUserRequest request, MultipartFile imageFile, String email) throws IllegalAccessException;
    void deleteUser(UUID userId, String email) throws IllegalAccessException;
    void deleteImageByUserId(UUID userId, String email);
    List<UserModel> getAllUsers(String email) throws IllegalAccessException;
    UserResponse converterToUserResponse(UserModel user);
}
