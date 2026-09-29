TRUNCATE TABLE
    employee_exhibitions,
    booking,
    artwork_exhibitions,
    photos,
    artwork_materials,
    artwork_authors,
    employee,
    visitor,
    exhibitions,
    materials,
    artworks,
    authors
RESTART IDENTITY CASCADE;


-- АВТОРЫ

INSERT INTO authors
    (name, last_name, patronymic, country, description, photo_url, birth_date, death_date)
VALUES
    ('Винсент', 'Ван Гог', NULL, 'Нидерланды',
     'Нидерландский художник-постимпрессионист.',
     'https://avatars.mds.yandex.net/i?id=8d05649b57cc2b738b872624b32a9807_l-12647631-images-thumbs&n=13',
     '1853-03-30', '1890-07-29'),

    ('Клод', 'Моне', NULL, 'Франция',
     'Французский художник-импрессионист.',
     'https://avatars.mds.yandex.net/i?id=3c32e847b011f4f0ec6ecc9fa48b63ff_l-9220607-images-thumbs&n=13',
     '1840-11-14', '1926-12-05'),

    ('Иван', 'Айвазовский', 'Константинович', 'Россия',
     'Русский художник-маринист.',
     'https://avatars.mds.yandex.net/i?id=3b627bb0f0391146d287c562b24798cb_l-5664890-images-thumbs&n=13',
     '1817-07-29', '1900-05-02'),

    ('Казимир', 'Малевич', 'Северинович', 'Россия',
     'Художник русского авангарда.',
     'https://avatars.mds.yandex.net/i?id=8a87939005a8a4cbf768472f8e8de9b6bb8b36c5-10972138-images-thumbs&n=13',
     '1879-02-23', '1935-05-15'),

    ('Василий', 'Кандинский', 'Васильевич', 'Россия',
     'Один из основоположников абстракционизма.',
     'https://rusmuseumvrm.ru/reference/classifier/author/kandinskiy_vv/2091_mainfoto_02.jpg',
     '1866-12-16', '1944-12-13'),

    ('Огюст', 'Роден', NULL, 'Франция',
     'Французский скульптор.',
     'https://levelvan.ru/upload/media/n-1001427648744-13379.jpg',
     '1840-11-12', '1917-11-17'),

    ('Анна', 'Лебедева', 'Игоревна', 'Россия',
     'Современная художница.',
     'images/authors/lebedeva.jpg',
     '1988-06-11', NULL),

    ('Михаил', 'Орлов', 'Петрович', 'Россия',
     'Современный фотограф.',
     'images/authors/orlov.jpg',
     '1991-09-04', NULL);


-- МАТЕРИАЛЫ

INSERT INTO materials (name)
VALUES
    ('Холст'),
    ('Масло'),
    ('Бронза'),
    ('Бумага'),
    ('Акварель'),
    ('Мрамор'),
    ('Фотобумага'),
    ('Дерево');


-- ПРОИЗВЕДЕНИЯ


INSERT INTO artworks
    (title, type, description, year, width_cm, height_cm, depth_cm, status)
VALUES
    ('Звёздная ночь', 'PAINTING',
     'Ночной пейзаж.',
     1889, 92.10, 73.70, NULL, 'IN_STORAGE'),

    ('Впечатление. Восходящее солнце', 'PAINTING',
     'Морской пейзаж.',
     1872, 63.00, 48.00, NULL, 'ON_EXHIBITION'),

    ('Девятый вал', 'PAINTING',
     'Морской пейзаж.',
     1850, 332.00, 221.00, NULL, 'ON_EXHIBITION'),

    ('Чёрный квадрат', 'PAINTING',
     'Произведение русского авангарда.',
     1915, 79.50, 79.50, NULL, 'RESTORATION'),

    ('Композиция VIII', 'PAINTING',
     'Абстрактная композиция.',
     1923, 201.00, 140.00, NULL, 'IN_STORAGE'),

    ('Мыслитель', 'SCULPTURE',
     'Скульптурное произведение.',
     1904, 57.00, 98.00, 72.00, 'ON_EXHIBITION'),

    ('Поцелуй', 'SCULPTURE',
     'Скульптурная композиция.',
     1882, 112.00, 181.50, 117.00, 'LOANED'),

    ('Осенний парк', 'GRAPHICS',
     'Современная графическая работа.',
     2021, 42.00, 59.40, NULL, 'IN_STORAGE'),

    ('Город после дождя', 'PHOTOGRAPH',
     'Авторская фотография.',
     2023, 60.00, 40.00, NULL, 'ON_EXHIBITION'),

    ('Деревянная форма №2', 'OTHER',
     'Современный арт-объект.',
     2024, 80.00, 120.00, 35.00, 'IN_STORAGE');



-- АВТОРЫ ПРОИЗВЕДЕНИЙ


INSERT INTO artwork_authors (id_author, id_artwork)
VALUES
    (1, 1),
    (2, 2),
    (3, 3),
    (4, 4),
    (5, 5),
    (6, 6),
    (6, 7),
    (7, 8),
    (8, 9),
    (7, 10);



-- МАТЕРИАЛЫ ПРОИЗВЕДЕНИЙ


INSERT INTO artwork_materials (id_artwork, id_material)
VALUES
    (1, 1), (1, 2),
    (2, 1), (2, 2),
    (3, 1), (3, 2),
    (4, 1), (4, 2),
    (5, 1), (5, 2),
    (6, 3),
    (7, 6),
    (8, 4), (8, 5),
    (9, 7),
    (10, 8);


-- ФОТОГРАФИИ


INSERT INTO photos (id_artwork, photo_url, is_main)
VALUES
    (1, 'images/artworks/1_main.jpg', TRUE),
    (1, 'images/artworks/1_detail.jpg', FALSE),
    (2, 'images/artworks/2_main.jpg', TRUE),
    (3, 'images/artworks/3_main.jpg', TRUE),
    (4, 'images/artworks/4_main.jpg', TRUE),
    (5, 'images/artworks/5_main.jpg', TRUE),
    (6, 'images/artworks/6_main.jpg', TRUE),
    (6, 'images/artworks/6_side.jpg', FALSE),
    (7, 'images/artworks/7_main.jpg', TRUE),
    (8, 'images/artworks/8_main.jpg', TRUE),
    (9, 'images/artworks/9_main.jpg', TRUE),
    (10, 'images/artworks/10_main.jpg', TRUE);



-- ВЫСТАВКИ


INSERT INTO exhibitions
    (title, description, photo_url, start_date, end_date, hall_number)
VALUES
    ('Искусство XIX века',
     'Живопись и скульптура XIX века.',
     'images/exhibitions/ex1.jpg',
     '2026-09-01', '2026-09-30', 2),

    ('Скульптура XIX–XX веков',
     'Выставка европейской скульптуры.',
     'images/exhibitions/ex2.jpg',
     '2026-09-05', '2026-10-20', 4),

    ('Русский авангард',
     'Работы художников русского авангарда.',
     'images/exhibitions/ex3.jpg',
     '2026-10-15', '2026-12-15', 1),

    ('Современное искусство',
     'Современная графика, фотография и арт-объекты.',
     'images/exhibitions/ex4.jpg',
     '2026-09-10', '2026-11-10', 3);


-- ПРОИЗВЕДЕНИЯ НА ВЫСТАВКАХ

INSERT INTO artwork_exhibitions (id_artwork, id_exhibition)
VALUES
    (2, 1),
    (3, 1),
    (6, 2),
    (7, 2),
    (4, 3),
    (5, 3),
    (8, 4),
    (9, 4),
    (10, 4);



-- ПОСЕТИТЕЛИ


INSERT INTO visitor
    (name, last_name, patronymic, birth_date, email, password_hash)
VALUES
    ('Алина', 'Соколова', 'Игоревна',
     '2002-05-14', 'alina@example.com', 'TEST_HASH_ALINA'),

    ('Максим', 'Орлов', 'Денисович',
     '1999-11-03', 'maxim@example.com', 'TEST_HASH_MAXIM'),

    ('Елена', 'Волкова', 'Сергеевна',
     '1987-01-22', 'elena@example.com', 'TEST_HASH_ELENA'),

    ('Илья', 'Морозов', 'Андреевич',
     '2004-08-17', 'ilya@example.com', 'TEST_HASH_ILYA'),

    ('Мария', 'Кузнецова', 'Олеговна',
     '1995-03-09', 'maria@example.com', 'TEST_HASH_MARIA');


-- СОТРУДНИКИ

INSERT INTO employee
    (name, last_name, patronymic, birth_date, position, email, password_hash, phone)
VALUES
    ('Ольга', 'Петрова', 'Викторовна',
     '1985-02-13', 'CURATOR',
     'petrova@museum.local', 'TEST_HASH_OLGA', '+79990000001'),

    ('Антон', 'Смирнов', 'Ильич',
     '1990-07-24', 'GUIDE',
     'smirnov@museum.local', 'TEST_HASH_ANTON', '+79990000002'),

    ('Николай', 'Егоров', 'Павлович',
     '1982-12-08', 'SECURITY',
     'egorov@museum.local', 'TEST_HASH_NIKOLAY', '+79990000003'),

    ('Ирина', 'Белова', 'Алексеевна',
     '1993-04-18', 'TECHNICIAN',
     'belova@museum.local', 'TEST_HASH_IRINA', '+79990000004'),

    ('Сергей', 'Попов', 'Михайлович',
     '1988-10-01', 'ADMINISTRATOR',
     'popov@museum.local', 'TEST_HASH_SERGEY', '+79990000005'),

    ('Дарья', 'Романова', 'Олеговна',
     '1989-06-20', 'RESTORER',
     'romanova@museum.local', 'TEST_HASH_DARIA', '+79990000006'),

    ('Павел', 'Соколов', 'Андреевич',
     '1984-03-12', 'KEEPER',
     'sokolov@museum.local', 'TEST_HASH_PAVEL', '+79990000007');


-- СОТРУДНИКИ НА ВЫСТАВКАХ


INSERT INTO employee_exhibitions (id_exhibition, id_employee)
VALUES
    (1, 1),
    (1, 2),
    (1, 3),

    (2, 1),
    (2, 2),
    (2, 3),

    (3, 1),
    (3, 4),
    (3, 7),

    (4, 2),
    (4, 4),
    (4, 7);



-- БРОНИРОВАНИЯ


INSERT INTO booking (id_visitor, id_exhibition, visit_date, status, price)
VALUES
    (1, 1, '2026-09-03', 'COMPLETED', 500.00),
    (2, 1, '2026-09-06', 'COMPLETED', 500.00),
    (3, 1, '2026-09-12', 'CANCELLED', NULL),

    (4, 2, '2026-09-10', 'COMPLETED', 700.00),
    (5, 2, '2026-09-14', 'COMPLETED', 700.00),
    (1, 2, '2026-09-15', 'CANCELLED', NULL),

    (2, 4, '2026-09-12', 'COMPLETED', 600.00),
    (3, 4, '2026-09-14', 'CONFIRMED', 600.00),
    (4, 4, '2026-09-15', 'CREATED', NULL),
    (5, 4, '2026-09-15', 'CONFIRMED', 600.00);



