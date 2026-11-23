package com.lucas.pacioli_ledger.identity.app;

import com.lucas.pacioli_ledger.identity.domain.User;
import com.lucas.pacioli_ledger.identity.domain.UserRepository;
import com.lucas.pacioli_ledger.shared.domain.id.UserID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository repository;

    public UserID getCurrentUserID() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof Jwt jwt)) {
            throw new AuthenticationCredentialsNotFoundException(
                    "Authenticated user not found"
            );
        }

        String sub = jwt.getSubject();
        String name = jwt.getClaimAsString("name");
        String email = jwt.getClaimAsString("email");

        if (!StringUtils.hasText(sub)) {
            throw new IllegalStateException("JWT subject is missing");
        }

        if (!StringUtils.hasText(name) || !StringUtils.hasText(email)) {
            throw  new IllegalStateException("JWT name or email is missing");
        }

        return repository.findIdByIdentitySubject(sub).orElseGet(
                () -> registerUser(sub, name, email)
        );
    }

    private UserID registerUser(String subject, String name, String email) {
        var user = new User(subject, name, email);

        repository.save(user);

        return user.getId();
    }
}
