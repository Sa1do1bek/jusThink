package com.example.backend.services.authorities.permission;

import com.example.backend.exceptions.ResourceNotFoundException;
import com.example.backend.models.Permission;
import com.example.backend.models.Role;
import com.example.backend.repositories.PermissionRepository;
import com.example.backend.repositories.RoleRepository;
import com.example.backend.requests.CreatePermissionRequest;
import com.example.backend.requests.UpdatePermissionRequest;
import com.example.backend.responses.PermissionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PermissionService implements IPermissionService{

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public Permission createPermission(CreatePermissionRequest permission) {
        return permissionRepository.save(new Permission(permission.name()));
    }

    @Override
    public Permission updatePermission(UpdatePermissionRequest permissionRequest) {
        Permission oldPermission = permissionRepository.findById(permissionRequest.id())
                .orElseThrow(() -> new ResourceNotFoundException("Permission with + " + permissionRequest.id() + " id not found!"));

        if (!permissionRequest.name().isEmpty()) oldPermission.setName(permissionRequest.name());
        return permissionRepository.save(oldPermission);
    }

    @Override
    public void deletePermission(UUID id) {
        Permission oldPermission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission with + " + id + " id not found!"));
        permissionRepository.delete(oldPermission);
    }

    @Override
    public Permission getPermissionById(UUID id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission with + " + id + " id not found!"));
    }

    @Override
    public Set<Permission> getAllPermissionsByRole(UUID roleId) {
        Optional<Role> role = roleRepository.findById(roleId);
        if (role.isPresent()) return role.get().getPermissions();
        throw new ResourceNotFoundException("Role with + " + roleId + " id not found!");
    }

    @Override
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    @Override
    public PermissionResponse converterToPermissionResponse(Permission permission) {
        return new PermissionResponse(
                permission.getId(),
                permission.getName(),
                permission.getCreatedAt(),
                permission.getUpdatedAt()
        );
    }
}