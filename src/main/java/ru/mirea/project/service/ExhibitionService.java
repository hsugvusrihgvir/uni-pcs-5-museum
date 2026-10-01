package ru.mirea.project.service;

import ru.mirea.project.exception.BusinessException;
import ru.mirea.project.exception.EntityNotFoundException;
import ru.mirea.project.model.Exhibition;
import ru.mirea.project.repository.ExhibitionRepository;

import java.time.LocalDate;
import java.util.List;

public class ExhibitionService {

    private final ExhibitionRepository exhibitionRepository;

    public ExhibitionService(ExhibitionRepository exhibitionRepository) {
        this.exhibitionRepository = exhibitionRepository;
    }

    // Проверка существования 
    public boolean existsById(long id) {
        if (id <= 0) return false;
        return exhibitionRepository.existsById(id);
    }

    // Получение по ID 
    public Exhibition getExhibitionById(long id) {
        return exhibitionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Выставка с ID " + id + " не найдена!"));
    }

    public List<Exhibition> getAllExhibitions() {
        return exhibitionRepository.findAll();
    }

    public Exhibition createExhibition(String title, String description, String photoUrl,
                                       LocalDate startDate, LocalDate endDate, int hallNumber) {

        if (title == null || title.trim().isEmpty()) {
            throw new BusinessException("Название выставки не может быть пустым!");
        }
        if (startDate == null || endDate == null) {
            throw new BusinessException("Даты проведения должны быть заполнены!");
        }
        if (endDate.isBefore(startDate)) {
            throw new BusinessException("Дата окончания не может быть раньше даты начала!");
        }
        if (hallNumber <= 0) {
            throw new BusinessException("Номер зала должен быть положительным числом!");
        }

        Exhibition exhibition = new Exhibition();
        exhibition.setTitle(title);
        exhibition.setDescription(description);
        exhibition.setPhotoUrl(photoUrl);
        exhibition.setStartDate(startDate);
        exhibition.setEndDate(endDate);
        exhibition.setHallNumber(hallNumber);

        return exhibitionRepository.save(exhibition);
    }

    public void deleteExhibition(long id) {
        getExhibitionById(id); 
        exhibitionRepository.deleteById(id);
    }
}