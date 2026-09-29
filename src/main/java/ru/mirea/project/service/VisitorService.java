package ru.mirea.project.service;

import java.time.LocalDate;
import java.util.List;

import ru.mirea.project.model.Visitor;
import ru.mirea.project.repository.VisitorRepository;

import ru.mirea.project.exception.BusinessException;
import ru.mirea.project.exception.EntityNotFoundException;

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
                                 String password_hash)
    {

    }

    // просмотр всех пользователей 1.2
    public List<Visitor> getAllVisitors(){

    }

    // проверить существование пользователя по id
    public boolean existsById(long visitorId) {

    }
}
