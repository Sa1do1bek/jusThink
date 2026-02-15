package com.example.backend.security;

import com.example.backend.models.Permission;
import com.example.backend.models.Role;
import com.example.backend.repositories.PermissionRepository;
import com.example.backend.repositories.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class SecurityDataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public void run(String... args) {
        Permission quizCreate = createPermissionIfNotFound("quiz:create");
        Permission quizUpdate = createPermissionIfNotFound("quiz:update");
        Permission quizDelete = createPermissionIfNotFound("quiz:delete");
        Permission quizGet = createPermissionIfNotFound("quiz-all:get");
        Permission quizByIdGet = createPermissionIfNotFound("quiz-by-id:get");
        Permission quizAllPublishedGet = createPermissionIfNotFound("quiz-all-published:get");
        Permission quizByIdAndModeGet = createPermissionIfNotFound("quiz-by-id-and-mode:get");

        Permission categoryCreate = createPermissionIfNotFound("category:create");
        Permission categoryUpdate = createPermissionIfNotFound("category:update");
        Permission categoryDelete = createPermissionIfNotFound("category:delete");
        Permission categoryGet = createPermissionIfNotFound("category-all:get");
        Permission categoryByIdGet = createPermissionIfNotFound("category-by-id:get");

        Permission sessionStart = createPermissionIfNotFound("session:start");
        Permission sessionSkip = createPermissionIfNotFound("session:skip");
        Permission sessionEnd = createPermissionIfNotFound("session:end");
        Permission sessionCreate = createPermissionIfNotFound("session:create");
        Permission sessionByIdGet = createPermissionIfNotFound("session-by-id:get");
        Permission sessionAllGet = createPermissionIfNotFound("session-all:get");

        Permission imageQuizGet = createPermissionIfNotFound("image-quiz:get");
        Permission imageUserGet = createPermissionIfNotFound("image:get");
        Permission imageByUserIdDelete = createPermissionIfNotFound("user-image-by-id:delete");
        Permission imageByQuizIdDelete = createPermissionIfNotFound("image-by-quiz-id:delete");

        Permission userUpdate = createPermissionIfNotFound("user:update");
        Permission userDelete = createPermissionIfNotFound("user:delete");
        Permission userAllGet = createPermissionIfNotFound("user-all:get");
        Permission userByIdGet = createPermissionIfNotFound("user-by-id:get");
        Permission userAllExport = createPermissionIfNotFound("user-all:export");

        Permission sessionStatsGet = createPermissionIfNotFound("session-stats:get");
        Permission sessionQuestionCorrectnessGet = createPermissionIfNotFound("session-question-correctness:get");
        Permission sessionAnswerPercentageGet = createPermissionIfNotFound("session-answer-percentage:get");
        Permission sessionPlayerNumberGet = createPermissionIfNotFound("session-player-number:get");
        Permission sessionAllAnswerPercentageGet = createPermissionIfNotFound("session-all-answer-percentage:get");
        Permission sessionAllNumberGet = createPermissionIfNotFound("session-all-number:get");
        Permission sessionAnswerAverageTimeGet = createPermissionIfNotFound("session-answer-average-time:get");
        Permission sessionQuestionAnswerAverageTimeGet = createPermissionIfNotFound("session-question-answer-average-time:get");

//        Role userRole = createRoleIfNotFound("USER");
        Role creatorRole = createRoleIfNotFound("CREATOR");
        Role adminRole = createRoleIfNotFound("ADMIN");

//        userRole.setPermissions(Set.of(quizGet, sessionGet));
        creatorRole.setPermissions(Set.of(quizGet, quizCreate, quizDelete, quizUpdate,quizByIdGet, quizAllPublishedGet,
                quizByIdAndModeGet, categoryGet, categoryByIdGet, sessionSkip, sessionEnd, sessionCreate, sessionByIdGet,
                userUpdate, userByIdGet, imageQuizGet, imageUserGet, imageByQuizIdDelete, imageByUserIdDelete,
                sessionStatsGet, sessionQuestionCorrectnessGet, sessionAnswerPercentageGet, sessionPlayerNumberGet,
                sessionAllAnswerPercentageGet, sessionAllNumberGet, sessionAnswerAverageTimeGet,
                sessionQuestionAnswerAverageTimeGet));

        adminRole.setPermissions(Set.of(quizGet, quizCreate, quizDelete, quizUpdate,quizByIdGet, quizAllPublishedGet,
                quizByIdAndModeGet, categoryCreate, categoryDelete, categoryUpdate, categoryGet, categoryByIdGet,
                sessionStart, sessionSkip, sessionEnd, sessionCreate, sessionAllGet, sessionByIdGet, userUpdate, userByIdGet,
                userDelete, userAllGet, userAllExport, imageQuizGet, imageUserGet, imageByQuizIdDelete, imageByUserIdDelete,
                sessionStatsGet, sessionQuestionCorrectnessGet, sessionAnswerPercentageGet, sessionPlayerNumberGet,
                sessionAllAnswerPercentageGet, sessionAllNumberGet, sessionAnswerAverageTimeGet,
                sessionQuestionAnswerAverageTimeGet));

//        roleRepository.save(userRole);
        roleRepository.save(creatorRole);
        roleRepository.save(adminRole);
    }

    private Permission createPermissionIfNotFound(String name) {
        return permissionRepository.findByName(name)
                .orElseGet(() -> permissionRepository.save(new Permission(name)));
    }

    private Role createRoleIfNotFound(String name) {
        return roleRepository.findByName(name)
                .orElseGet(() -> roleRepository.save(new Role(name)));
    }
}