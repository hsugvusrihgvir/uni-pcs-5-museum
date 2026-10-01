package ru.mirea.project.model;

import java.time.LocalDate;

public class Visitor {
    private long id;
    private String name;
    private String last_name;
    private String patronymic;
    private LocalDate birth_date;
    private String email;
    private String password_hash;

    // пустой конструктор
    public Visitor() {
    }

    // для сохранения в бд
    public Visitor(String name,
                   String last_name,
                   String patronymic,
                   LocalDate birth_date,
                   String email,
                   String password_hash) {
        this.name = name;
        this.last_name = last_name;
        this.patronymic = patronymic;
        this.birth_date = birth_date;
        this.email = email;
        this.password_hash = password_hash;
    }

    // для доставания из бд
    public Visitor(long id,
                   String name,
                   String last_name,
                   String patronymic,
                   LocalDate birth_date,
                   String email,
                   String password_hash) {
        this.id = id;
        this.name = name;
        this.last_name = last_name;
        this.patronymic = patronymic;
        this.birth_date = birth_date;
        this.email = email;
        this.password_hash = password_hash;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return last_name;
    }

    public void setLastName(String last_name) {
        this.last_name = last_name;
    }

    public String getPatronymic() {
        return patronymic;
    }

    public void setPatronymic(String patronymic) {
        this.patronymic = patronymic;
    }

    public LocalDate getBirthDate() {
        return birth_date;
    }

    public void setBirthDate(LocalDate birth_date) {
        this.birth_date = birth_date;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return password_hash;
    }

    public void setPasswordHash(String password_hash) {
        this.password_hash = password_hash;
    }

    public String getFullName() {
        if (patronymic == null || patronymic.trim().isEmpty()) {
            return last_name + " " + name;
        }
        return last_name + " " + name + " " + patronymic;
    }

    @Override
    public String toString() {
        return "Visitor{" +
                "id=" + id +
                ", fullName='" + getFullName() + '\'' +
                ", birthDate=" + birth_date +
                ", email='" + email + '\'' +
                '}';
    }
}
