package com.eCommerce.auth.handler.impl;

import com.eCommerce.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads a user by their username.
     *
     * @param username The username of the user to load.
     * @return A UserDetailsImpl object containing user details.
     * @throws UsernameNotFoundException If the user is not found in the database.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }
        return user.get();
    }

    /**
     * Checks if a user has permission to access a specific resource with a given method.
     *
     * @param username The username of the user.
     * @param path The path to check access for.
     * @param method   The HTTP method (e.g., GET, POST) to check access for.
     * @return True if the user has permission, false otherwise.
     */
    @Transactional(readOnly = true)
    public boolean hasPermission(String username, String path, String method) {
        return userRepository.hasPermission(username, path, method);
    }


}