package com.example.backend.services.authorities.role;

import com.example.backend.exceptions.IllegalActionException;
import com.example.backend.exceptions.ResourceNotFoundException;
import com.example.backend.models.Permission;
import com.example.backend.models.Role;
import com.example.backend.repositories.PermissionRepository;
import com.example.backend.repositories.RoleRepository;
import com.example.backend.requests.CreateRoleRequest;
import com.example.backend.requests.UpdateRoleRequest;
import com.example.backend.responses.RoleResponse;
import com.example.backend.services.authorities.permission.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleService implements IRoleService{

    private final RoleRepository roleRepository;
    private final PermissionService permissionService;
    private final PermissionRepository permissionRepository;

    @Override
    public Role createRole(CreateRoleRequest roleRequest) {
        Role newRole = new Role(roleRequest.name());
        Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(roleRequest.permissionIds()));

        newRole.setPermissions(permissions);
        return roleRepository.save(newRole);
    }

    @Override
    public Role updateRole(UpdateRoleRequest roleRequest) {
        Role role = roleRepository.findById(roleRequest.id())
                .orElseThrow(() -> new ResourceNotFoundException("Role with " + roleRequest.id() + " id not found!"));

        if (!roleRequest.name().isEmpty()) role.setName(roleRequest.name());
        return roleRepository.save(role);
    }

    @Override
    public void deleteRole(UUID id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role with " + id + " id not found!"));
        roleRepository.delete(role);
    }

    @Override
    public Role getRoleById(UUID id) {
        return roleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Role with " + id + " id not found!"));
    }

    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Transactional
    @Override
    public Role addPermission(UUID roleId, UUID permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role with " + roleId + " id not found!"));
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission with + " + permissionId + " id not found!"));

        Set<Permission> permissions = role.getPermissions();

        if (permissions.contains(permission))
            throw new IllegalActionException("Role with " + roleId + " id already has a permission with " + permission + " id!");

        permissions.add(permission);
        role.setPermissions(permissions);

        return roleRepository.save(role);
    }

    @Override
    public Role deletePermission(UUID roleId, UUID permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role with " + roleId + " id not found!"));
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission with + " + permissionId + " id not found!"));

        Set<Permission> permissions = role.getPermissions();

        if (!permissions.contains(permission))
            throw new ResourceNotFoundException("Role with " + roleId + " id doesn't have a permission with " + permission + " id!");

        permissions.remove(permission);
        role.setPermissions(permissions);

        return roleRepository.save(role);
    }

    @Override
    public RoleResponse converterToRoleResponse(Role role) {
        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getCreatedAt(),
                role.getUpdatedAt(),
                role.getPermissions()
                        .stream()
                        .map(permissionService::converterToPermissionResponse)
                        .toList()
        );
    }
}