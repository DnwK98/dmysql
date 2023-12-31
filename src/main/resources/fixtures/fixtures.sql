DELETE FROM invoice_items;
DELETE FROM invoices;
DELETE FROM cars_workshops;
DELETE FROM cars;
DELETE FROM users;
DELETE FROM countries;
DELETE FROM workshops;
INSERT INTO workshops(id, name) VALUES (1, 'Auto Mistrz'), (2, 'Ekspert Auto'), (3, 'Express fix')
INSERT INTO countries (code, name) VALUES ('PL', 'Poland'), ('US', 'United States');
INSERT INTO countries (code, name) VALUES ('RU', 'Russia'), ('SE', 'Sweden'), ('MX', 'Mexico');
INSERT INTO countries (code, name) VALUES ('DE', 'Germany'), ('CZ', 'Czechia'), ('JP', 'Japan');
INSERT INTO countries (code, name) VALUES ('IL', 'Israel'), ('IN', 'India'), ('HU', 'Hungary');
INSERT INTO users (id, name) VALUES (1, 'Mateusz'), (2, 'Karol'), (3, 'Eryk'), (4, 'Patrycja');
INSERT INTO users (id, name) VALUES (5, 'Jan'),(6, 'Leon'), (7, 'Renata'), (8, 'Marta');
INSERT INTO users (id, name) VALUES (9, 'Robert'), (10, 'Szymon');
INSERT INTO cars (registration, owner_id, model, production_country, mileage) VALUES ('GD 1234', 1, 'Honda', 'JP', 55000);
INSERT INTO cars (registration, owner_id, model, production_country, mileage) VALUES ('WI 53D2', 4, 'Ford', 'US', 83000);
INSERT INTO cars (registration, owner_id, model, production_country, mileage) VALUES ('DW 12H1', 2, 'Audi', 'DE', 127000);
INSERT INTO cars (registration, owner_id, model, production_country, mileage) VALUES ('WB 721L', 2, 'Mercedes', 'DE', 348000);
INSERT INTO cars (registration, owner_id, model, production_country, mileage) VALUES ('BI 22E1', 3, 'BMW', 'DE', 73000);
INSERT INTO cars (registration, owner_id, model, production_country, mileage) VALUES ('BI K731', 3, 'BMW', 'DE', 235000);
INSERT INTO cars (registration, owner_id, model, production_country, mileage) VALUES ('WB 7622', 3, 'Porsche', 'DE', null);
INSERT INTO cars (registration, owner_id, model, production_country, mileage) VALUES ('BI 9K36', 3, 'Honda', 'JP', 75000);
INSERT INTO cars_workshops (registration, owner_id, workshop_id) VALUES ('GD 1234', 1, 1);
INSERT INTO cars_workshops (registration, owner_id, workshop_id) VALUES ('WI 53D2', 4, 1);
INSERT INTO cars_workshops (registration, owner_id, workshop_id) VALUES ('WI 53D2', 4, 2);
INSERT INTO cars_workshops (registration, owner_id, workshop_id) VALUES ('WB 721L', 2, 3);
INSERT INTO invoices (id, user_id, workshop_id, amount) VALUES (1, 1, 1, 500);
INSERT INTO invoices (id, user_id, workshop_id, amount) VALUES (2, 1, 1, 2700);
INSERT INTO invoices (id, user_id, workshop_id, amount) VALUES (3, 1, 1, 490);
INSERT INTO invoices (id, user_id, workshop_id, amount) VALUES (4, 4, 1, 2800);
INSERT INTO invoices (id, user_id, workshop_id, amount) VALUES (5, 4, 1, 3000);
INSERT INTO invoices (id, user_id, workshop_id, amount) VALUES (6, 4, 1, 120);
INSERT INTO invoices (id, user_id, workshop_id, amount) VALUES (7, 4, 1, 290);
INSERT INTO invoices (id, user_id, workshop_id, amount) VALUES (8, 4, 2, 5000);
INSERT INTO invoices (id, user_id, workshop_id, amount) VALUES (9, 2, 3, 1200);
INSERT INTO invoice_items (id, invoice_id, user_id, name, amount) VALUES (1, 1, 1, 'Brake pads', 500);
INSERT INTO invoice_items (id, invoice_id, user_id, name, amount) VALUES (2, 2, 1, 'Engine', 2500);
INSERT INTO invoice_items (id, invoice_id, user_id, name, amount) VALUES (3, 2, 1, 'Alternator', 200);
INSERT INTO invoice_items (id, invoice_id, user_id, name, amount) VALUES (4, 3, 1, 'Brake pads', 490);
INSERT INTO invoice_items (id, invoice_id, user_id, name, amount) VALUES (5, 4, 4, 'Engine', 490);
INSERT INTO invoice_items (id, invoice_id, user_id, name, amount) VALUES (6, 5, 4, 'Engine', 2300);
INSERT INTO invoice_items (id, invoice_id, user_id, name, amount) VALUES (7, 5, 4, 'Suspension', 700);
INSERT INTO invoice_items (id, invoice_id, user_id, name, amount) VALUES (8, 6, 4, 'Radiator', 120);
INSERT INTO invoice_items (id, invoice_id, user_id, name, amount) VALUES (9, 7, 4, 'Radiator', 290);
INSERT INTO invoice_items (id, invoice_id, user_id, name, amount) VALUES (10, 8, 4, 'Engine', 3000);
INSERT INTO invoice_items (id, invoice_id, user_id, name, amount) VALUES (11, 8, 4, 'Suspension', 1000);
INSERT INTO invoice_items (id, invoice_id, user_id, name, amount) VALUES (12, 8, 4, 'Transmission', 1000);
INSERT INTO invoice_items (id, invoice_id, user_id, name, amount) VALUES (13, 9, 2, 'Battery', 400);
INSERT INTO invoice_items (id, invoice_id, user_id, name, amount) VALUES (14, 9, 2, 'Exhaust system', 800);