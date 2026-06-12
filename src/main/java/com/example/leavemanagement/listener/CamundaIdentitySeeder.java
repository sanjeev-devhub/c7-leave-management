package com.example.leavemanagement.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds Camunda's identity service (engine's own user/group tables in Postgres)
 * so that:
 * <ul>
 *   <li>candidate-group task queries (employee / line-manager) resolve</li>
 *   <li>the embedded Camunda Tasklist/Cockpit can be used with these users</li>
 * </ul>
 *
 * <p>Demo users are seeded with simple passwords and mapped to the BPMN
 * candidate groups: employees to "employee" and managers to "line-manager".</p>
 *
 * <p>Idempotent: checks existence before creating, so it's safe across restarts.</p>
 *
 * <p>NOTE: the {@code line-manager} group id contains a hyphen. Since Camunda
 * 7.10, identity ids are validated against a whitelist pattern whose default
 * ({@code [a-zA-Z0-9]+|camunda-admin}) rejects hyphens. The widened pattern is
 * configured in {@code application.yml} under
 * {@code camunda.bpm.generic-properties.properties.generalResourceWhitelistPattern};
 * without it, {@code saveGroup("line-manager")} throws
 * "is not a valid resource identifier".</p>
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class CamundaIdentitySeeder {

    private static final String GROUP_EMPLOYEE = "employee";
    private static final String GROUP_LINE_MANAGER = "line-manager";
    private static final String DEMO_PASSWORD = "demo-password";

    private final IdentityService identityService;

    @EventListener(ApplicationReadyEvent.class)
    public void seed() {
        if (identityService.isReadOnly()) {
            log.info("IdentityService is read-only; skipping Camunda identity seeding.");
            return;
        }

        ensureGroup(GROUP_EMPLOYEE, "Employees");
        ensureGroup(GROUP_LINE_MANAGER, "Line Managers");

        // Employees
        ensureUser("emp01", GROUP_EMPLOYEE);
        ensureUser("emp02", GROUP_EMPLOYEE);
        // Line managers
        ensureUser("manager01", GROUP_LINE_MANAGER);
        ensureUser("manager02", GROUP_LINE_MANAGER);
        // HR / Admin (no candidate-group routing needed for the process)
        ensureUser("hr01", null);
        ensureUser("admin", null);

        log.info("Camunda identity seeding complete.");
    }

    private void ensureGroup(String groupId, String name) {
        if (identityService.createGroupQuery().groupId(groupId).count() == 0) {
            Group group = identityService.newGroup(groupId);
            group.setName(name);
            group.setType("WORKFLOW");
            identityService.saveGroup(group);
            log.info("Created Camunda group '{}'", groupId);
        }
    }

    private void ensureUser(String userId, String groupId) {
        if (identityService.createUserQuery().userId(userId).count() == 0) {
            User user = identityService.newUser(userId);
            user.setPassword(DEMO_PASSWORD);
            user.setFirstName(userId);
            user.setLastName("(demo)");
            user.setEmail(userId + "@example.com");
            identityService.saveUser(user);
            log.info("Created Camunda user '{}'", userId);
        }

        if (groupId != null) {
            addMembershipQuietly(userId, groupId);
        }
    }

    private void addMembershipQuietly(String userId, String groupId) {
        try {
            List<Group> existing = identityService.createGroupQuery()
                    .groupMember(userId).groupId(groupId).list();
            if (existing.isEmpty()) {
                identityService.createMembership(userId, groupId);
                log.info("Added user '{}' to group '{}'", userId, groupId);
            }
        } catch (Exception e) {
            log.debug("Membership {} -> {} already exists or failed: {}", userId, groupId, e.getMessage());
        }
    }
}