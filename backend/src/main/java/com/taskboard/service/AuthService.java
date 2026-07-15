package com.taskboard.service;

import com.taskboard.entity.AppUser;
import com.taskboard.entity.Card;
import com.taskboard.entity.TaskList;
import com.taskboard.exception.DuplicateUsernameException;
import com.taskboard.repository.AppUserRepository;
import com.taskboard.repository.CardRepository;
import com.taskboard.repository.TaskListRepository;
import java.time.LocalDate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final TaskListRepository taskListRepository;
    private final CardRepository cardRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            AppUserRepository appUserRepository,
            TaskListRepository taskListRepository,
            CardRepository cardRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.appUserRepository = appUserRepository;
        this.taskListRepository = taskListRepository;
        this.cardRepository = cardRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AppUser register(String username, String password) {
        if (appUserRepository.findByUsername(username).isPresent()) {
            throw new DuplicateUsernameException(username);
        }

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user = appUserRepository.save(user);

        seedDefaultBoard(user);

        return user;
    }

    private void seedDefaultBoard(AppUser user) {
        TaskList toDo = createList(user, "To Do", 0);
        TaskList inProgress = createList(user, "進行中", 1);
        TaskList done = createList(user, "完了", 2);

        createCard(toDo, "開業届の準備をする", "高", LocalDate.of(2026, 7, 15), 0);
        createCard(toDo, "内装の見積もりを取る", "中", null, 1);
        createCard(inProgress, "メニューの試作をする", "中", LocalDate.of(2026, 7, 20), 0);
        createCard(done, "物件を契約する", "低", LocalDate.of(2026, 7, 1), 0);
    }

    private TaskList createList(AppUser user, String title, int position) {
        TaskList list = new TaskList();
        list.setUser(user);
        list.setTitle(title);
        list.setPosition(position);
        return taskListRepository.save(list);
    }

    private void createCard(TaskList list, String text, String priority, LocalDate dueDate, int position) {
        Card card = new Card();
        card.setList(list);
        card.setText(text);
        card.setPriority(priority);
        card.setDueDate(dueDate);
        card.setPosition(position);
        cardRepository.save(card);
    }
}
