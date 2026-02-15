package com.example.backend.services.authorities.permission;

import com.example.backend.models.Permission;
import com.example.backend.requests.CreatePermissionRequest;
import com.example.backend.requests.UpdatePermissionRequest;
import com.example.backend.responses.PermissionResponse;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface IPermissionService {
    Permission createPermission(CreatePermissionRequest permission);
    Permission updatePermission(UpdatePermissionRequest permissionRequest);
    void deletePermission(UUID id);
    Permission getPermissionById(UUID id);
    Set<Permission> getAllPermissionsByRole(UUID roleId);
    List<Permission> getAllPermissions();
    PermissionResponse converterToPermissionResponse(Permission permission);
}
