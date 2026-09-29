DROP TABLE IF EXISTS employee_exhibitions CASCADE;
DROP TABLE IF EXISTS booking CASCADE;
DROP TABLE IF EXISTS visits CASCADE;
DROP TABLE IF EXISTS artwork_exhibitions CASCADE;
DROP TABLE IF EXISTS photos CASCADE;
DROP TABLE IF EXISTS artwork_materials CASCADE;
DROP TABLE IF EXISTS artwork_authors CASCADE;
DROP TABLE IF EXISTS employee CASCADE;
DROP TABLE IF EXISTS visitor CASCADE;
DROP TABLE IF EXISTS exhibitions CASCADE;
DROP TABLE IF EXISTS materials CASCADE;
DROP TABLE IF EXISTS artworks CASCADE;
DROP TABLE IF EXISTS authors CASCADE;

DROP TYPE IF EXISTS employee_position CASCADE;
DROP TYPE IF EXISTS booking_status CASCADE;
DROP TYPE IF EXISTS artwork_status CASCADE;
DROP TYPE IF EXISTS artwork_type CASCADE;


CREATE TYPE artwork_type AS ENUM (
    'PAINTING',
    'SCULPTURE',
    'GRAPHICS',
    'PHOTOGRAPH',
    'OTHER'
);

CREATE TYPE artwork_status AS ENUM (
    'IN_STORAGE',
    'ON_EXHIBITION',
    'RESTORATION',
    'LOANED'
);

CREATE TYPE employee_position AS ENUM (
    'ADMINISTRATOR',
    'CURATOR',
    'GUIDE',
    'RESTORER',
    'KEEPER',
    'SECURITY',
    'TECHNICIAN'
);

CREATE TYPE booking_status AS ENUM (
    'CREATED',
    'CONFIRMED',
    'COMPLETED',
    'CANCELLED'
);



CREATE TABLE authors (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    patronymic VARCHAR(255),

    country VARCHAR(100),
    description TEXT,
    photo_url TEXT,

    birth_date DATE,
    death_date DATE,

    CONSTRAINT chk_author_dates
        CHECK (
            death_date IS NULL
            OR birth_date IS NULL
            OR death_date >= birth_date
        )
);


CREATE TABLE artworks (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    title VARCHAR(255) NOT NULL,
    type artwork_type NOT NULL,
    description TEXT,
    year INTEGER,

    width_cm NUMERIC(10, 2),
    height_cm NUMERIC(10, 2),
    depth_cm NUMERIC(10, 2),

    status artwork_status NOT NULL,

    CONSTRAINT chk_artwork_year
        CHECK (
            year IS NULL
            OR year BETWEEN 1 AND 2100
        ),

    CONSTRAINT chk_artwork_width
        CHECK (
            width_cm IS NULL
            OR width_cm > 0
        ),

    CONSTRAINT chk_artwork_height
        CHECK (
            height_cm IS NULL
            OR height_cm > 0
        ),

    CONSTRAINT chk_artwork_depth
        CHECK (
            depth_cm IS NULL
            OR depth_cm > 0
        )
);


CREATE TABLE materials (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);


CREATE TABLE exhibitions (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    title VARCHAR(255) NOT NULL,
    description TEXT,
    photo_url TEXT,

    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    hall_number INTEGER NOT NULL,

    CONSTRAINT chk_exhibition_dates
        CHECK (end_date >= start_date),

    CONSTRAINT chk_hall_number
        CHECK (hall_number > 0)
);


CREATE TABLE visitor (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    patronymic VARCHAR(255),

    birth_date DATE,

    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL
);


CREATE TABLE employee (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    patronymic VARCHAR(255),

    birth_date DATE,

    position employee_position NOT NULL,

    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(30) UNIQUE
);





CREATE TABLE artwork_authors (
    id_author BIGINT NOT NULL,
    id_artwork BIGINT NOT NULL,

    PRIMARY KEY (id_author, id_artwork),

    FOREIGN KEY (id_author)
        REFERENCES authors(id)
        ON DELETE RESTRICT,

    FOREIGN KEY (id_artwork)
        REFERENCES artworks(id)
        ON DELETE CASCADE
);


CREATE TABLE artwork_materials (
    id_artwork BIGINT NOT NULL,
    id_material BIGINT NOT NULL,

    PRIMARY KEY (id_artwork, id_material),

    FOREIGN KEY (id_artwork)
        REFERENCES artworks(id)
        ON DELETE CASCADE,

    FOREIGN KEY (id_material)
        REFERENCES materials(id)
        ON DELETE RESTRICT
);


CREATE TABLE artwork_exhibitions (
    id_artwork BIGINT NOT NULL,
    id_exhibition BIGINT NOT NULL,

    PRIMARY KEY (id_artwork, id_exhibition),

    FOREIGN KEY (id_artwork)
        REFERENCES artworks(id)
        ON DELETE CASCADE,

    FOREIGN KEY (id_exhibition)
        REFERENCES exhibitions(id)
        ON DELETE CASCADE
);


CREATE TABLE employee_exhibitions (
    id_exhibition BIGINT NOT NULL,
    id_employee BIGINT NOT NULL,

    PRIMARY KEY (id_exhibition, id_employee),

    FOREIGN KEY (id_exhibition)
        REFERENCES exhibitions(id)
        ON DELETE CASCADE,

    FOREIGN KEY (id_employee)
        REFERENCES employee(id)
        ON DELETE CASCADE
);




CREATE TABLE photos (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    id_artwork BIGINT NOT NULL,
    photo_url TEXT NOT NULL,
    is_main BOOLEAN NOT NULL DEFAULT FALSE,

    FOREIGN KEY (id_artwork)
        REFERENCES artworks(id)
        ON DELETE CASCADE
);


CREATE UNIQUE INDEX uq_photos_one_main_per_artwork
    ON photos (id_artwork)
    WHERE is_main = TRUE;



CREATE TABLE booking (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    id_visitor BIGINT NOT NULL,
    id_exhibition BIGINT NOT NULL,

    visit_date DATE NOT NULL,
    status booking_status NOT NULL,
    price DOUBLE PRECISION,

    FOREIGN KEY (id_visitor)
        REFERENCES visitor(id)
        ON DELETE CASCADE,

    FOREIGN KEY (id_exhibition)
        REFERENCES exhibitions(id)
        ON DELETE CASCADE
);


-- поиск произведений
CREATE INDEX idx_artworks_title
    ON artworks(title);

CREATE INDEX idx_artworks_year
    ON artworks(year);

-- поиск авторов
CREATE INDEX idx_authors_last_name
    ON authors(last_name);

-- поиск выставок
CREATE INDEX idx_exhibitions_title
    ON exhibitions(title);

-- фото конкретного произведения
CREATE INDEX idx_photos_artwork
    ON photos(id_artwork);

-- бронирования
CREATE INDEX idx_booking_visitor
    ON booking(id_visitor);

CREATE INDEX idx_booking_exhibition
    ON booking(id_exhibition);

-- все произведения конкретного автора
CREATE INDEX idx_artwork_authors_artwork
    ON artwork_authors(id_artwork);

-- все произведения из конкретного материала
CREATE INDEX idx_artwork_materials_material
    ON artwork_materials(id_material);

-- все произведения конкретной выставки
CREATE INDEX idx_artwork_exhibitions_exhibition
    ON artwork_exhibitions(id_exhibition);

-- все выставки конкретного сотрудника
CREATE INDEX idx_employee_exhibitions_employee
    ON employee_exhibitions(id_employee);
