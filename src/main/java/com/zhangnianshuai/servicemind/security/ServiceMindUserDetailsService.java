package com.zhangnianshuai.servicemind.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.zhangnianshuai.servicemind.domain.auth.AppUser;
import com.zhangnianshuai.servicemind.domain.auth.AppUserRepository;

@Service
public class ServiceMindUserDetailsService
        implements UserDetailsService {

    private final AppUserRepository repository;

    public ServiceMindUserDetailsService(
            AppUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(
            String username)
            throws UsernameNotFoundException {

        AppUser user = repository
            .findByUsernameIgnoreCase(username)
            .orElseThrow(
                () -> new UsernameNotFoundException(
                    "User not found"
                )
            );

        return User
            .withUsername(user.getUsername())
            .password(user.getPasswordHash())
            .roles(user.getRole().name())
            .disabled(!user.isEnabled())
            .build();
    }
}