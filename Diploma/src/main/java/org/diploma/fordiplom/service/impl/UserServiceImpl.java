package org.diploma.fordiplom.service.impl;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.diploma.fordiplom.entity.DTO.UserDTO;
import org.diploma.fordiplom.entity.ProjectEntity;
import org.diploma.fordiplom.entity.TaskEntity;
import org.diploma.fordiplom.entity.UserEntity;
import org.diploma.fordiplom.repository.ProjectRepository;
import org.diploma.fordiplom.repository.TaskRepository;
import org.diploma.fordiplom.repository.UserRepository;
import org.diploma.fordiplom.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private ProjectRepository projectRepository;

    @Override
    public UserEntity createUser(UserEntity user) throws Exception {
        if (existsByEmail(user.getEmail())) {
            throw new Exception("Email already exists");
        }

        String token = UUID.randomUUID().toString(); // Уникальный токен

        UserEntity newUser = new UserEntity();
        newUser.setEmail(user.getEmail());
        newUser.setPassword(passwordEncoder.encode(user.getPassword()));
        newUser.setConfirmationToken(token);
        newUser.setEnabled(false); // Пока не активирован

        userRepository.save(newUser);

        // Отправка письма с токеном
        sendConfirmationEmail(newUser.getEmail(), token);

        return newUser;
    }

    public List<UserEntity> getUsersForProject(Long projectId) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Проект не найден"));

        return new ArrayList<>(project.getUsers());
    }

    @Transactional
    @Override
    public void sendConfirmationEmail(String toEmail, String token) throws MessagingException {
        String confirmUrl = "http://localhost:8080/confirm?token=" + token;
        String subject = "Подтверждение регистрации";
        String message = "<p>Перейдите по ссылке, чтобы подтвердить регистрацию:</p>" +
                "<a href=\"" + confirmUrl + "\">Подтвердить регистрацию</a>";

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

        helper.setFrom("martinov1804@mail.ru");
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(message, true); // true = письмо в формате HTML

        mailSender.send(mimeMessage);
        System.out.println("Email sent to " + toEmail);
    }
    @Transactional
    @Override
    public String confirmUser(String token) {
        Optional<UserEntity> userOptional = userRepository.findByConfirmationToken(token);
        if (userOptional.isEmpty()) {
            System.out.println("Некорректный токен: " + token);
            return "Некорректный токен подтверждения.";
        }

        UserEntity user = userOptional.get();

        user.setEnabled(true);
        userRepository.save(user);
        return "Email подтвержден. Теперь вы можете войти в систему.";
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
    public List<UserEntity> getUsersByProjectTasks(Long projectId) {
        return userRepository.findDistinctByTasksSprintProjectId(projectId);
    }

    @Override
    public String getGitHubToken(String email) {
        return userRepository.findByEmail(email)
                .map(UserEntity::getGitHubAccessToken) // получаем токен из сущности
                .orElse(null); // если пользователя нет, возвращаем null
    }


    @Override
    public List<UserDTO> getUsersWithTasksByProjectId(Long projectId) {
        List<UserEntity> userEntities = userRepository.findDistinctByTasksSprintProjectId(projectId);

        return userEntities.stream()
                .map(user -> {
                    List<TaskEntity> tasks = taskRepository.findAllByExecutorIdAndProjectId(user.getId_user(), projectId);
                    return new UserDTO(user, tasks);
                })
                .toList();
    }

    @Override
    public UserDTO getUserWithTasks(Long userId, Long projectId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        List<TaskEntity> tasks = taskRepository.findAllTasksByExecutorIdAndProjectId(userId, projectId);
        return new UserDTO(user, tasks);
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
    @Override
    public void saveUserImgPath(Long userId, String imgUrl) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setUserImgPath(imgUrl);
        userRepository.save(user);  // Сохраняем обновленного пользователя
    }


}
