package com.example.backend.controllers.user;

import com.example.backend.requests.UpdateUserRequest;
import com.example.backend.responses.ApiResponse;
import com.example.backend.responses.UserResponse;
import com.example.backend.services.analytics.excel.ExcelService;
import com.example.backend.services.image.ImageStorageService;
import com.example.backend.services.user.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/users")
public class UserController {

    private final UserService userService;
    private final ExcelService excelService;
    private final ImageStorageService storage;

    @PreAuthorize("hasAuthority('user:update')")
    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse> updateUserById(
            @PathVariable UUID userId,
            @RequestPart("request") String requestJson,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile
            ) {
        try {
            UpdateUserRequest request = new ObjectMapper().readValue(requestJson, UpdateUserRequest.class);

            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            UserResponse response = userService.converterToUserResponse(
                    userService.updateUser(
                            userId, request, imageFile, email
                    )
            );
            return ResponseEntity.ok(new ApiResponse("User updated successfully!", response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @PreAuthorize("hasAuthority('user-all:get')")
    @GetMapping
    public ResponseEntity<ApiResponse> getAllUsers() {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            List<UserResponse> users = userService.getAllUsers(email)
                    .stream()
                    .map(userService::converterToUserResponse)
                    .toList();
            return ResponseEntity.ok(new ApiResponse("All users!", users));
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasAuthority('user:delete')")
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse> deleteUserById(@PathVariable UUID userId) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            userService.deleteUser(userId, email);
            return ResponseEntity.ok(new ApiResponse("User deleted successfully!", null));
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasAuthority('user-image-by-id:delete')")
    @DeleteMapping("/{userId}/image")
    public ResponseEntity<ApiResponse> deleteImageByUserId(@PathVariable UUID userId) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            userService.deleteImageByUserId(userId, email);
            return ResponseEntity.ok(new ApiResponse("Image deleted successfully!", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }


    @PreAuthorize("hasAuthority('user-image:get')")
    @GetMapping("/media/images/image/{path}")
    public ResponseEntity<FileSystemResource> getImage(@PathVariable String path) {
        try {
            return storage.loadImage(path)
                    .map(resource ->
                            ResponseEntity.ok()
                                    .contentType(MediaType.IMAGE_PNG)
                                    .body(resource)
                    )
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    @PreAuthorize("hasAuthority('user-all:export')")
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportAllUsers() {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            byte[] excelFile = excelService.exportUsersToExcel(email);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users.xlsx")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(excelFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}