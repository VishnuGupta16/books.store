-- 1. Insert 5 Authors
INSERT INTO author (id, first_name, last_name, email, phone_number) VALUES (1, 'J.K.', 'Rowling', 'jk@example.com', '1234567890');
INSERT INTO author (id, first_name, last_name, email, phone_number) VALUES (2, 'George', 'Orwell', 'george@example.com', '2345678901');
INSERT INTO author (id, first_name, last_name, email, phone_number) VALUES (3, 'J.R.R.', 'Tolkien', 'jr@example.com', '3456789012');
INSERT INTO author (id, first_name, last_name, email, phone_number) VALUES (4, 'Stephen', 'King', 'stephen@example.com', '4567890123');
INSERT INTO author (id, first_name, last_name, email, phone_number) VALUES (5, 'Agatha', 'Christie', 'agatha@example.com', '5678901234');

-- 2. Insert 10 Books (no author_id column anymore -- links live in book_author)
INSERT INTO book (id, name, title) VALUES (1, 'HP1', 'Harry Potter and the Philosphers Stone');
INSERT INTO book (id, name, title) VALUES (2, 'HP2', 'Harry Potter and the Chamber of Secrets');
INSERT INTO book (id, name, title) VALUES (3, '1984', 'Nineteen Eighty-Four');
INSERT INTO book (id, name, title) VALUES (4, 'AnimalFarm', 'Animal Farm');
INSERT INTO book (id, name, title) VALUES (5, 'Hobbit', 'The Hobbit');
INSERT INTO book (id, name, title) VALUES (6, 'LOTR1', 'The Fellowship of the Ring');
INSERT INTO book (id, name, title) VALUES (7, 'Shining', 'The Shining');
INSERT INTO book (id, name, title) VALUES (8, 'It', 'It');
INSERT INTO book (id, name, title) VALUES (9, 'MurderOrient', 'Murder on the Orient Express');
INSERT INTO book (id, name, title) VALUES (10, 'Nile', 'Death on the Nile');

-- 3. Insert Book-Author links, each with a role.
--    One row per (book, author) pair -- this replaces the old book.author_id column.
INSERT INTO book_author (id, book_id, author_id, author_role) VALUES (1, 1, 1, 'MAIN_AUTHOR');
INSERT INTO book_author (id, book_id, author_id, author_role) VALUES (2, 2, 1, 'MAIN_AUTHOR');
INSERT INTO book_author (id, book_id, author_id, author_role) VALUES (3, 3, 2, 'MAIN_AUTHOR');
INSERT INTO book_author (id, book_id, author_id, author_role) VALUES (4, 4, 2, 'MAIN_AUTHOR');
INSERT INTO book_author (id, book_id, author_id, author_role) VALUES (5, 5, 3, 'MAIN_AUTHOR');
INSERT INTO book_author (id, book_id, author_id, author_role) VALUES (6, 6, 3, 'MAIN_AUTHOR');
INSERT INTO book_author (id, book_id, author_id, author_role) VALUES (7, 7, 4, 'MAIN_AUTHOR');
INSERT INTO book_author (id, book_id, author_id, author_role) VALUES (8, 8, 4, 'MAIN_AUTHOR');
INSERT INTO book_author (id, book_id, author_id, author_role) VALUES (9, 9, 5, 'MAIN_AUTHOR');
INSERT INTO book_author (id, book_id, author_id, author_role) VALUES (10, 10, 5, 'MAIN_AUTHOR');

-- Extra links to demonstrate MANY authors on one book (the whole point of the link entity)
INSERT INTO book_author (id, book_id, author_id, author_role) VALUES (11, 1, 3, 'CO_AUTHOR');
INSERT INTO book_author (id, book_id, author_id, author_role) VALUES (12, 6, 2, 'EDITOR');

-- 4. Insert Reviews (book_id is the @ManyToOne FK; created_at set explicitly because
--    @PrePersist does NOT run for raw import.sql inserts)
INSERT INTO review (id, rating, verified, reviewer_name, reviewer_email, comment, book_id, created_at) VALUES (1, 5, true,  'Alice',   'alice@example.com',   'Magical start to the series!',          1, '2026-01-05 09:15:00');
INSERT INTO review (id, rating, verified, reviewer_name, reviewer_email, comment, book_id, created_at) VALUES (2, 4, true,  'Bob',     'bob@example.com',     'Loved it, a bit slow at times.',        1, '2026-01-08 14:20:00');
INSERT INTO review (id, rating, verified, reviewer_name, reviewer_email, comment, book_id, created_at) VALUES (3, 5, false, 'Carol',   'carol@example.com',   'Couldn''t put it down.',                 2, '2026-01-10 18:45:00');
INSERT INTO review (id, rating, verified, reviewer_name, reviewer_email, comment, book_id, created_at) VALUES (4, 3, true,  'Dave',    'dave@example.com',    'Good, but not as strong as book one.',  2, '2026-01-12 11:00:00');
INSERT INTO review (id, rating, verified, reviewer_name, reviewer_email, comment, book_id, created_at) VALUES (5, 5, true,  'Erin',    'erin@example.com',    'A chilling, timeless classic.',         3, '2026-02-01 20:30:00');
INSERT INTO review (id, rating, verified, reviewer_name, reviewer_email, comment, book_id, created_at) VALUES (6, 4, false, 'Frank',   'frank@example.com',   'Thought-provoking and bleak.',          3, '2026-02-03 08:10:00');
INSERT INTO review (id, rating, verified, reviewer_name, reviewer_email, comment, book_id, created_at) VALUES (7, 4, true,  'Grace',   'grace@example.com',   'Short but powerful.',                   4, '2026-02-06 16:25:00');
INSERT INTO review (id, rating, verified, reviewer_name, reviewer_email, comment, book_id, created_at) VALUES (8, 5, true,  'Heidi',   'heidi@example.com',   'An adventure for all ages.',            5, '2026-02-09 13:40:00');
INSERT INTO review (id, rating, verified, reviewer_name, reviewer_email, comment, book_id, created_at) VALUES (9, 5, true,  'Ivan',    'ivan@example.com',    'Epic worldbuilding.',                   6, '2026-02-11 19:05:00');
INSERT INTO review (id, rating, verified, reviewer_name, reviewer_email, comment, book_id, created_at) VALUES (10, 2, false,'Judy',    'judy@example.com',    'Too long for my taste.',                6, '2026-02-14 10:50:00');
INSERT INTO review (id, rating, verified, reviewer_name, reviewer_email, comment, book_id, created_at) VALUES (11, 4, true, 'Mallory', 'mallory@example.com', 'Genuinely scary.',                      7, '2026-02-18 22:15:00');
INSERT INTO review (id, rating, verified, reviewer_name, reviewer_email, comment, book_id, created_at) VALUES (12, 5, true, 'Niaj',    'niaj@example.com',    'A masterful mystery.',                  9, '2026-02-20 12:00:00');

-- 5. Synchronize sequences so Hibernate's generated IDs don't collide with the ones above
ALTER SEQUENCE author_seq RESTART WITH 6;
ALTER SEQUENCE book_seq RESTART WITH 11;
ALTER SEQUENCE book_author_seq RESTART WITH 13;
ALTER SEQUENCE review_seq RESTART WITH 13;
