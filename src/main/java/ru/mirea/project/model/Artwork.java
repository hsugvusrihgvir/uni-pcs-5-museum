package ru.mirea.project.model;

public class Artwork {

    private long id;
    private String title;
    private ArtworkType type;
    private String description;
    private Integer year;

    private Double widthCm;
    private Double heightCm;
    private Double depthCm;

    private ArtworkStatus status;


    public Artwork() {
    }


    public Artwork(
            String title,
            ArtworkType type,
            String description,
            Integer year,
            Double widthCm,
            Double heightCm,
            Double depthCm,
            ArtworkStatus status
    ) {
        this.title = title;
        this.type = type;
        this.description = description;
        this.year = year;
        this.widthCm = widthCm;
        this.heightCm = heightCm;
        this.depthCm = depthCm;
        this.status = status;
    }


    public Artwork(
            long id,
            String title,
            ArtworkType type,
            String description,
            Integer year,
            Double widthCm,
            Double heightCm,
            Double depthCm,
            ArtworkStatus status
    ) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.description = description;
        this.year = year;
        this.widthCm = widthCm;
        this.heightCm = heightCm;
        this.depthCm = depthCm;
        this.status = status;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public ArtworkType getType() {
        return type;
    }

    public void setType(ArtworkType type) {
        this.type = type;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }


    public Double getWidthCm() {
        return widthCm;
    }

    public void setWidthCm(Double widthCm) {
        this.widthCm = widthCm;
    }


    public Double getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(Double heightCm) {
        this.heightCm = heightCm;
    }


    public Double getDepthCm() {
        return depthCm;
    }

    public void setDepthCm(Double depthCm) {
        this.depthCm = depthCm;
    }


    public ArtworkStatus getStatus() {
        return status;
    }

    public void setStatus(ArtworkStatus status) {
        this.status = status;
    }

    // метод для вывода обычного
    @Override
    public String toString() {
        return "Artwork{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", type=" + type +
                ", description='" + description + '\'' +
                ", year=" + year +
                ", widthCm=" + widthCm +
                ", heightCm=" + heightCm +
                ", depthCm=" + depthCm +
                ", status=" + status +
                '}';
    }
}