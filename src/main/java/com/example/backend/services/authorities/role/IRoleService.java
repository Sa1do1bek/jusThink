package com.example.backend.services.authorities.role;

import com.example.backend.models.Role;
import com.example.backend.requests.CreateRoleRequest;
import com.example.backend.requests.UpdateRoleRequest;
import com.example.backend.responses.RoleResponse;

import java.util.List;
import java.util.UUID;

public interface IRoleService {
    Role createRole(CreateRoleRequest role);
    Role updateRole(UpdateRoleRequest roleRequest);
    void deleteRole(UUID id);
    Role getRoleById(UUID id);
    List<Role> getAllRoles();
    Role addPermission(UUID roleId, UUID permissionId);
    Role deletePermission(UUID roleId, UUID permissionId);
    RoleResponse converterToRoleResponse(Role role);
}
