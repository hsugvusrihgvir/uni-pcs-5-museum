package ru.mirea.project.model;

import java.time.LocalDate;

public class Exhibition {
    private long id;
    private String title;
    private String description;
    private String photoUrl;
    private LocalDate startDate;
    private LocalDate endDate;
    private int hallNumber;

    public Exhibition() {
    }

    public Exhibition(long id, String title, String description, String photoUrl, 
                      LocalDate startDate, LocalDate endDate, int hallNumber) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.photoUrl = photoUrl;
        this.startDate = startDate;
        this.endDate = endDate;
        this.hallNumber = hallNumber;
    }

    // Геттеры и сеттеры
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public int getHallNumber() { return hallNumber; }
    public void setHallNumber(int hallNumber) { this.hallNumber = hallNumber; }

    @Override
    public String toString() {
        return String.format("Exhibition{id=%d, title='%s', start=%s, end=%s, hall=%d}",
                id, title, startDate, endDate, hallNumber);
    }
}