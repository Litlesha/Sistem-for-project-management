package org.diploma.fordiplom.service.impl;
import jakarta.transaction.Transactional;
import org.diploma.fordiplom.entity.UserEntity;
import org.diploma.fordiplom.repository.UserRepository;
import org.diploma.fordiplom.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserEntity createUser(UserEntity user) throws Exception {
        UserEntity newUser = new UserEntity();
        if (existsByEmail(user.getEmail())) {
            throw new Exception("Email already exists");
        }
        newUser.setEmail(user.getEmail());
        newUser.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(newUser);
    }

    @Override
    public UserEntity updateUser(String email, UserEntity user) {
        UserEntity updateUser = getUserByEmail(email);
        updateUser.setEmail(user.getEmail());
        updateUser.setPassword(user.getPassword());
        updateUser.setPosition(user.getPosition());
        updateUser.setOrganization(user.getOrganization());
        return userRepository.save(updateUser);
    }

    @Override
    public UserEntity getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public List<UserEntity> getAllUsersByEmails(List<String> emails) {
        return userRepository.findAllByEmailIn(emails);
    }


    @Override
    public UserEntity findUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public UserEntity getCurrentUser(Principal principal) {
        String user = principal.getName();
        return userRepository.findByEmail(user).orElseThrow(() -> new RuntimeException("User not found"));

    }
    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    public UserEntity updateUserProfile(String email, String position,String username, String organization) {  // Найти пользователя по email
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        if (username != null && !username.isEmpty()) {
            user.setUsername(username);
        }
        // Обновить должность и организацию, если они были переданы
        if (position != null && !position.isEmpty()) {
            user.setPosition(position);
        }
        if (organization != null && !organization.isEmpty()) {
            user.setOrganization(organization);
        }
        // Сохранить обновленного пользователя в базу данных
         return userRepository.save(user);
    }


}
