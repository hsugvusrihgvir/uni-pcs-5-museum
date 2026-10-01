package ru.mirea.project.service;

import java.time.LocalDate;
import java.util.List;

import ru.mirea.project.model.Visitor;
import ru.mirea.project.repository.VisitorRepository;

import ru.mirea.project.exception.BusinessException;

public class VisitorService {
    private final VisitorRepository visitorRepository;

    public VisitorService(VisitorRepository visitorRepository) {
        this.visitorRepository = visitorRepository;
    }

    // создание пользователя 1.1
    public Visitor createVisitor(String name,
                                 String last_name,
                                 String patronymic,
                                 LocalDate birth_date,
                                 String email,
                                 String password_hash) {

        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException("Имя посетителя не может быть пустым!");
        }
        if (last_name == null || last_name.trim().isEmpty()) {
            throw new BusinessException("Фамилия посетителя не может быть пустой!");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new BusinessException("Email посетителя не может быть пустым!");
        }
        if (password_hash == null || password_hash.trim().isEmpty()) {
            throw new BusinessException("Хеш пароля не может быть пустым!");
        }

        Visitor visitor = new Visitor();
        visitor.setName(name);
        visitor.setLastName(last_name);
        visitor.setPatronymic(patronymic);
        visitor.setBirthDate(birth_date);
        visitor.setEmail(email);
        visitor.setPasswordHash(password_hash);

        return visitorRepository.save(visitor);
    }

    // просмотр всех пользователей 1.2
    public List<Visitor> getAllVisitors() {
        return visitorRepository.findAll();
    }

    // проверить существование пользователя по id
    public boolean existsById(long visitorId) {
        if (visitorId <= 0) return false;
        return visitorRepository.existsById(visitorId);
    }
}
