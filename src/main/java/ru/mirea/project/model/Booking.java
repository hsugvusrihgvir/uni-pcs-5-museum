package ru.mirea.project.model;

import java.time.LocalDate;

public class Booking {

    private long id;
    private long visitorId;
    private long exhibitionId;
    private LocalDate visitDate;
    private BookingStatus status;
    private Double price; // Double, так как при создании тут будет NULL

    // пустой конструктор
    public Booking() {
    }

    // конструктор для создания новых объектов до сохранения в бд
    public Booking(
            long visitorId,
            long exhibitionId,
            LocalDate visitDate,
            BookingStatus status,
            Double price
    ) {
        this.visitorId = visitorId;
        this.exhibitionId = exhibitionId;
        this.visitDate = visitDate;
        this.status = status;
        this.price = price;
    }

    // конструктор с ид, когда достаем из бд
    public Booking(
            long id,
            long visitorId,
            long exhibitionId,
            LocalDate visitDate,
            BookingStatus status,
            Double price
    ) {
        this.id = id;
        this.visitorId = visitorId;
        this.exhibitionId = exhibitionId;
        this.visitDate = visitDate;
        this.status = status;
        this.price = price;
    }

    // геттеры и сеттеры
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getVisitorId() {
        return visitorId;
    }

    public void setVisitorId(long visitorId) {
        this.visitorId = visitorId;
    }

    public long getExhibitionId() {
        return exhibitionId;
    }

    public void setExhibitionId(long exhibitionId) {
        this.exhibitionId = exhibitionId;
    }

    public LocalDate getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(LocalDate visitDate) {
        this.visitDate = visitDate;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    // Метод для красивого вывода в консоль (пункты 2.2 и 2.3)
    @Override
    public String toString() {
        return "Booking{" +
                "id=" + id +
                ", visitorId=" + visitorId +
                ", exhibitionId=" + exhibitionId +
                ", visitDate=" + visitDate +
                ", status=" + status +
                ", price=" + (price != null ? price + " руб." : "NULL (Не оплачено)") +
                '}';
    }
}
