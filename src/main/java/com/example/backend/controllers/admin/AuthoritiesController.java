package com.example.backend.controllers.admin;

import com.example.backend.exceptions.IllegalActionException;
import com.example.backend.exceptions.ResourceNotFoundException;
import com.example.backend.requests.CreatePermissionRequest;
import com.example.backend.requests.CreateRoleRequest;
import com.example.backend.requests.UpdatePermissionRequest;
import com.example.backend.requests.UpdateRoleRequest;
import com.example.backend.responses.ApiResponse;
import com.example.backend.services.authorities.permission.PermissionService;
import com.example.backend.services.authorities.role.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/authorities")
public class AuthoritiesController {

    private final RoleService roleService;
    private final PermissionService permissionService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/roles")
    public ResponseEntity<ApiResponse> createRole(@RequestBody CreateRoleRequest request) {
        try {
            return ResponseEntity.ok(new ApiResponse(
                    "Role created successfully!",
                    roleService.converterToRoleResponse(roleService.createRole(request)))
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/roles")
    public ResponseEntity<ApiResponse> updateRole(@RequestBody UpdateRoleRequest request) {
        try {
            return ResponseEntity.ok(new ApiResponse(
                    "Role updated successfully!",
                    roleService.converterToRoleResponse(roleService.updateRole(request)))
            );
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/roles/{roleId}")
    public ResponseEntity<ApiResponse> deleteRole(@PathVariable UUID roleId) {
        try {
            roleService.deleteRole(roleId);
            return ResponseEntity.ok(new ApiResponse(
                    "Role deleted successfully!", null
            ));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/roles/{roleId}")
    public ResponseEntity<ApiResponse> getRoleById(@PathVariable UUID roleId) {
        try {
            return ResponseEntity.ok(new ApiResponse(
                    "Role found!",
                    roleService.converterToRoleResponse(roleService.getRoleById(roleId))
            ));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/roles")
    public ResponseEntity<ApiResponse> getAllRoles() {
        try {
            return ResponseEntity.ok(new ApiResponse(
                    "All roles!",
                    roleService.getAllRoles().stream().map(roleService::converterToRoleResponse).toList()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/roles/{roleId}/permissions/{permissionId}")
    public ResponseEntity<ApiResponse> addPermission(@PathVariable UUID roleId, @PathVariable UUID permissionId) {
        try {
            return ResponseEntity.ok(new ApiResponse(
                    "Permission added to role successfully!",
                    roleService.converterToRoleResponse(roleService.addPermission(roleId, permissionId)))
            );
        } catch (ResourceNotFoundException | IllegalActionException e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/roles/delete-permission")
    public ResponseEntity<ApiResponse> deletePermission(@RequestParam UUID roleId, @RequestParam UUID permissionId) {
        try {
            return ResponseEntity.ok(new ApiResponse(
                    "Permission deleted from role successfully!",
                    roleService.converterToRoleResponse(roleService.deletePermission(roleId, permissionId)))
            );
        } catch (ResourceNotFoundException | IllegalActionException e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/permissions")
    public ResponseEntity<ApiResponse> createPermission(@RequestBody CreatePermissionRequest request) {
        try {
            return ResponseEntity.ok(new ApiResponse(
                    "Permission created successfully!",
                    permissionService.converterToPermissionResponse(permissionService.createPermission(request)))
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/permissions")
    public ResponseEntity<ApiResponse> updatePermission(@RequestBody UpdatePermissionRequest request) {
        try {
            return ResponseEntity.ok(new ApiResponse(
                    "Role updated successfully!",
                    permissionService.converterToPermissionResponse(permissionService.updatePermission(request))
                    )
            );
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/permissions/{permissionId}")
    public ResponseEntity<ApiResponse> deletePermission(@PathVariable UUID permissionId) {
        try {
            permissionService.deletePermission(permissionId);
            return ResponseEntity.ok(new ApiResponse(
                    "Permission deleted successfully!", null
            ));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/permissions/{permissionId}")
    public ResponseEntity<ApiResponse> getPermissionById(@PathVariable UUID permissionId) {
        try {
            return ResponseEntity.ok(new ApiResponse(
                    "Permission found!",
                    permissionService.converterToPermissionResponse(permissionService.getPermissionById(permissionId))
            ));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/permissions")
    public ResponseEntity<ApiResponse> getAllPermissions() {
        try {
            return ResponseEntity.ok(new ApiResponse(
                    "All permissions!",
                    permissionService.getAllPermissions().stream().map(permissionService::converterToPermissionResponse).toList()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }
}