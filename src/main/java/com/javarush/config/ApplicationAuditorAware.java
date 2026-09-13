package com.javarush.config;

import com.javarush.model.entity.User;
import com.javarush.model.repository.UserRepository;
import com.javarush.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("applicationAuditorAware")
@RequiredArgsConstructor
public class ApplicationAuditorAware implements AuditorAware<User> {
    private final UserRepository userRepository;

    @Override
    public Optional<User> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken){
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if(!(principal instanceof UserPrincipal userPrincipal)){
            return Optional.empty();
        }

        return Optional.of(userRepository.getReferenceById(userPrincipal.getId()));
    }
}
