package engine.businesslayer.security;

import engine.businesslayer.exceptions.RegistrationUserException;
import engine.repositories.AppUserRepository;
import engine.presentation.request.RegistrationRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {
    private final AppUserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(AppUserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerUser(RegistrationRequest registrationRequest) throws RegistrationUserException {
        var user = repository.findAppUserByUsername(registrationRequest.getEmail());
        if (user.isPresent()) {
            throw new RegistrationUserException(String.format("User with name '%s' already exists", registrationRequest.getEmail()));
        }
        AppUser appUser = new AppUser();
        appUser.setUsername(registrationRequest.getEmail());
        appUser.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
        repository.save(appUser);
    }
}
