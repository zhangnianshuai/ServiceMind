package com.zhangnianshuai.servicemind.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.zhangnianshuai.servicemind.domain.auth.AppRole;
import com.zhangnianshuai.servicemind.domain.auth.AppUser;
import com.zhangnianshuai.servicemind.domain.auth.AppUserRepository;

@Component
public class SecurityBootstrapInitializer
        implements ApplicationRunner {

    private static final Logger log =
        LoggerFactory.getLogger(
            SecurityBootstrapInitializer.class
        );

    private final AppUserRepository repository;
    private final PasswordEncoder passwordEncoder;

    private final String adminPassword;
    private final String agentPassword;

    public SecurityBootstrapInitializer(
            AppUserRepository repository,
            PasswordEncoder passwordEncoder,
            @Value(
                "${app.security.bootstrap.admin-password:}"
            )
            String adminPassword,
            @Value(
                "${app.security.bootstrap.agent-password:}"
            )
            String agentPassword) {

        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.adminPassword = adminPassword;
        this.agentPassword = agentPassword;
    }

    @Override
    @Transactional
    public void run(
            ApplicationArguments args) {

        synchronizeUser(
            "admin",
            "系统管理员",
            AppRole.ADMIN,
            adminPassword
        );

        synchronizeUser(
            "agent",
            "客服坐席",
            AppRole.AGENT,
            agentPassword
        );
    }

    private void synchronizeUser(
            String username,
            String displayName,
            AppRole role,
            String rawPassword) {

        if (!StringUtils.hasText(rawPassword)) {
            log.warn(
                "Bootstrap user '{}' was not created because "
                    + "its password environment variable "
                    + "is not configured.",
                username
            );

            return;
        }

        AppUser user = repository
            .findByUsernameIgnoreCase(username)
            .orElse(null);

        if (user == null) {
            repository.save(
                new AppUser(
                    username,
                    passwordEncoder.encode(
                        rawPassword
                    ),
                    displayName,
                    role
                )
            );

            log.info(
                "Created bootstrap user '{}' with role {}",
                username,
                role
            );

            return;
        }

        String passwordHash =
            user.getPasswordHash();

        if (!passwordEncoder.matches(
                rawPassword,
                passwordHash)) {

            passwordHash =
                passwordEncoder.encode(
                    rawPassword
                );
        }

        user.synchronize(
            passwordHash,
            displayName,
            role
        );

        log.info(
            "Synchronized bootstrap user '{}' with role {}",
            username,
            role
        );
    }
}