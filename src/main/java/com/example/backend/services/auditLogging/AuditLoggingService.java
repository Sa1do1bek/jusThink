package com.example.backend.services.auditLogging;

import com.example.backend.enums.Action;
import com.example.backend.enums.ResourceType;
import com.example.backend.enums.Role;
import com.example.backend.exceptions.IllegalActionException;
import com.example.backend.models.AuditLog;
import com.example.backend.models.UserModel;
import com.example.backend.repositories.AuditLogRepository;
import com.example.backend.responses.AuditLogResponse;
import com.example.backend.security.IPAddressService;
import com.example.backend.services.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditLoggingService implements IAuditLoggingService{

    private final AuditLogRepository auditLogRepository;
    private final UserService userService;
    private final IPAddressService ipAddressService;

    public void createAuditLog(
            UserModel actor, Action action, ResourceType resourceType,
            UUID resourceId, HttpServletRequest request, boolean success,
            String metaData
    ) {
        AuditLog log = new AuditLog();

        log.setActor(actor);
        log.setActorRole(actor.getRole() == null ? Role.SYSTEM.name() : actor.getRole().getName());
        log.setAction(action);
        log.setResourceType(resourceType);
        log.setResourceId(resourceId);
        log.setIpAddress(ipAddressService.getClientIp(request));
        log.setUserAgent(request.getHeader("User-Agent"));
        log.setSuccess(success);
        log.setMetadata(metaData);

        auditLogRepository.save(log);
    }

    public List<AuditLogResponse> getLimitedAuditLogs(int limit) {
        if (limit < 1)
            throw new IllegalActionException("Wrong limit! It should be at least 1!");

        Pageable pageable = (Pageable) PageRequest.of(0, limit);
        List<AuditLog> auditLogs = (List<AuditLog>) auditLogRepository.findAll((PageRequest) pageable);
        
        return auditLogs
                .stream()
                .map(this::converterAuditLogResponse)
                .toList();
    }

    private AuditLogResponse converterAuditLogResponse(AuditLog auditLog) {
        return new AuditLogResponse(
                auditLog.getId(),
                auditLog.getResourceId(),
                auditLog.getUserAgent(),
                auditLog.isSuccess(),
                auditLog.getTimestamp(),
                auditLog.getResourceType(),
                auditLog.getAction(),
                userService.converterToUserResponse(auditLog.getActor()),
                auditLog.getActorRole(),
                auditLog.getMetadata()
        );
    }
}
