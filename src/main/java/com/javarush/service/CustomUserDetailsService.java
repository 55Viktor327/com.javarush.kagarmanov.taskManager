package com.javarush.service;

import com.javarush.exception.UserNotFoundException;
import com.javarush.model.entity.User;
import com.javarush.model.repository.UserRepository;
import com.javarush.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findActiveByUsername(username)
                                .orElseThrow(
                                        () -> new UserNotFoundException("Пользователь не найден")
                                );

        return new UserPrincipal(user);
    }
}
