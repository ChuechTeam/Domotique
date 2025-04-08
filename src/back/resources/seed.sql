-- Sample data for the EHPAD

    -- First remove all data
DELETE FROM Device WHERE id >= 0;
DELETE FROM DeviceType WHERE id >= 0;
DELETE FROM Room WHERE id >= 0;
DELETE FROM User WHERE id >= 0;
DELETE FROM ActionLog WHERE id >= 0;
DELETE FROM PowerLog WHERE id >= 0;
DELETE FROM InviteCode WHERE id <> '';
    
-- Users (Residents, Caregivers, Admins)
-- Gender: 0=MALE, 1=FEMALE, 2=UNDISCLOSED
-- Role: 0=RESIDENT, 1=CAREGIVER, 2=ADMIN
-- Level: 0=BEGINNER, 1=INTERMEDIATE, 2=ADVANCED, 3=EXPERT
INSERT INTO User (`id`, `email`, `emailConfirmationToken`, `emailConfirmed`, `passHash`, `firstName`, `lastName`, `gender`, `role`, `level`, `points`) VALUES
                                                                                                                                                             (1, 'jean.dupont@ehpad.fr', 1234567890123456, 1, '$pbkdf2$$zP3DwtYzvu0Kf+X2FQHptQ$WdxhcSVAbCVtEBvy+Q0uBNds31VXzoi70fjXZwB3sLPYGX4y8i18435bJm8YFdETC8Os2DtGovIKJKCK0Rb57A', 'Jean', 'Dupont', 0, 0, 1, 150),
                                                                                                                                                             (2, 'marie.martin@ehpad.fr', 2345678901234567, 1, '$pbkdf2$$zP3DwtYzvu0Kf+X2FQHptQ$WdxhcSVAbCVtEBvy+Q0uBNds31VXzoi70fjXZwB3sLPYGX4y8i18435bJm8YFdETC8Os2DtGovIKJKCK0Rb57A', 'Marie', 'Martin', 1, 0, 0, 50),
                                                                                                                                                             (3, 'pierre.bernard@ehpad.fr', 3456789012345678, 1, '$pbkdf2$$zP3DwtYzvu0Kf+X2FQHptQ$WdxhcSVAbCVtEBvy+Q0uBNds31VXzoi70fjXZwB3sLPYGX4y8i18435bJm8YFdETC8Os2DtGovIKJKCK0Rb57A', 'Pierre', 'Bernard', 0, 0, 1, 210),
                                                                                                                                                             (4, 'sophie.dubois@ehpad.fr', 4567890123456789, 1, '$pbkdf2$$zP3DwtYzvu0Kf+X2FQHptQ$WdxhcSVAbCVtEBvy+Q0uBNds31VXzoi70fjXZwB3sLPYGX4y8i18435bJm8YFdETC8Os2DtGovIKJKCK0Rb57A', 'Sophie', 'Dubois', 1, 0, 2, 520),
                                                                                                                                                             (5, 'jacques.robert@ehpad.fr', 5678901234567890, 1, '$pbkdf2$$zP3DwtYzvu0Kf+X2FQHptQ$WdxhcSVAbCVtEBvy+Q0uBNds31VXzoi70fjXZwB3sLPYGX4y8i18435bJm8YFdETC8Os2DtGovIKJKCK0Rb57A', 'Jacques', 'Robert', 0, 0, 0, 80),
                                                                                                                                                             (6, 'isabelle.richard@ehpad.fr', 6789012345678901, 1, '$pbkdf2$$zP3DwtYzvu0Kf+X2FQHptQ$WdxhcSVAbCVtEBvy+Q0uBNds31VXzoi70fjXZwB3sLPYGX4y8i18435bJm8YFdETC8Os2DtGovIKJKCK0Rb57A', 'Isabelle', 'Richard', 1, 0, 1, 190),
                                                                                                                                                             (7, 'alain.petit@ehpad.fr', 7890123456789012, 0, '$pbkdf2$$zP3DwtYzvu0Kf+X2FQHptQ$WdxhcSVAbCVtEBvy+Q0uBNds31VXzoi70fjXZwB3sLPYGX4y8i18435bJm8YFdETC8Os2DtGovIKJKCK0Rb57A', 'Alain', 'Petit', 0, 0, 0, 25), -- Email not confirmed
                                                                                                                                                             (8, 'monique.durand@ehpad.fr', 8901234567890123, 1, '$pbkdf2$$zP3DwtYzvu0Kf+X2FQHptQ$WdxhcSVAbCVtEBvy+Q0uBNds31VXzoi70fjXZwB3sLPYGX4y8i18435bJm8YFdETC8Os2DtGovIKJKCK0Rb57A', 'Monique', 'Durand', 1, 0, 2, 480),
                                                                                                                                                             (9, 'gerard.leroy@ehpad.fr', 9012345678901234, 1, '$pbkdf2$$zP3DwtYzvu0Kf+X2FQHptQ$WdxhcSVAbCVtEBvy+Q0uBNds31VXzoi70fjXZwB3sLPYGX4y8i18435bJm8YFdETC8Os2DtGovIKJKCK0Rb57A', 'Gérard', 'Leroy', 0, 0, 1, 250),
                                                                                                                                                             (10, 'nicole.moreau@ehpad.fr', 1122334455667788, 1, '$pbkdf2$$zP3DwtYzvu0Kf+X2FQHptQ$WdxhcSVAbCVtEBvy+Q0uBNds31VXzoi70fjXZwB3sLPYGX4y8i18435bJm8YFdETC8Os2DtGovIKJKCK0Rb57A', 'Nicole', 'Moreau', 1, 0, 0, 95),
                                                                                                                                                             (11, 'andre.simon@ehpad.fr', 2233445566778899, 1, '$pbkdf2$$zP3DwtYzvu0Kf+X2FQHptQ$WdxhcSVAbCVtEBvy+Q0uBNds31VXzoi70fjXZwB3sLPYGX4y8i18435bJm8YFdETC8Os2DtGovIKJKCK0Rb57A', 'André', 'Simon', 0, 0, 1, 310),
                                                                                                                                                             (12, 'chantal.laurent@ehpad.fr', 3344556677889900, 1, '$pbkdf2$$zP3DwtYzvu0Kf+X2FQHptQ$WdxhcSVAbCVtEBvy+Q0uBNds31VXzoi70fjXZwB3sLPYGX4y8i18435bJm8YFdETC8Os2DtGovIKJKCK0Rb57A', 'Chantal', 'Laurent', 1, 0, 0, 115),
                                                                                                                                                             (13, 'michel.lefevre@ehpad.fr', 4455667788990011, 1, '$pbkdf2$$zP3DwtYzvu0Kf+X2FQHptQ$WdxhcSVAbCVtEBvy+Q0uBNds31VXzoi70fjXZwB3sLPYGX4y8i18435bJm8YFdETC8Os2DtGovIKJKCK0Rb57A', 'Michel', 'Lefevre', 0, 0, 2, 600),
                                                                                                                                                             (14, 'brigitte.roux@ehpad.fr', 5566778899001122, 1, '$pbkdf2$$zP3DwtYzvu0Kf+X2FQHptQ$WdxhcSVAbCVtEBvy+Q0uBNds31VXzoi70fjXZwB3sLPYGX4y8i18435bJm8YFdETC8Os2DtGovIKJKCK0Rb57A', 'Brigitte', 'Roux', 1, 0, 1, 280),
                                                                                                                                                             (15, 'bernard.david@ehpad.fr', 6677889900112233, 1, '$pbkdf2$$zP3DwtYzvu0Kf+X2FQHptQ$WdxhcSVAbCVtEBvy+Q0uBNds31VXzoi70fjXZwB3sLPYGX4y8i18435bJm8YFdETC8Os2DtGovIKJKCK0Rb57A', 'Bernard', 'David', 0, 0, 0, 65),
-- Caregivers
                                                                                                                                                             (16, 'sylvie.caron@soignant.ehpad.fr', 7788990011223344, 1, '$pbkdf2$$zP3DwtYzvu0Kf+X2FQHptQ$WdxhcSVAbCVtEBvy+Q0uBNds31VXzoi70fjXZwB3sLPYGX4y8i18435bJm8YFdETC8Os2DtGovIKJKCK0Rb57A', 'Sylvie', 'Caron', 1, 1, 2, 800),
                                                                                                                                                             (17, 'luc.mercier@soignant.ehpad.fr', 8899001122334455, 1, '$pbkdf2$$zP3DwtYzvu0Kf+X2FQHptQ$WdxhcSVAbCVtEBvy+Q0uBNds31VXzoi70fjXZwB3sLPYGX4y8i18435bJm8YFdETC8Os2DtGovIKJKCK0Rb57A', 'Luc', 'Mercier', 0, 1, 1, 450),
                                                                                                                                                             (18, 'nathalie.guerin@soignant.ehpad.fr', 9900112233445566, 1, '$pbkdf2$$zP3DwtYzvu0Kf+X2FQHptQ$WdxhcSVAbCVtEBvy+Q0uBNds31VXzoi70fjXZwB3sLPYGX4y8i18435bJm8YFdETC8Os2DtGovIKJKCK0Rb57A', 'Nathalie', 'Guerin', 1, 1, 2, 750),
                                                                                                                                                             (19, 'patrick.moulin@soignant.ehpad.fr', 1001122334455667, 1, '$pbkdf2$$zP3DwtYzvu0Kf+X2FQHptQ$WdxhcSVAbCVtEBvy+Q0uBNds31VXzoi70fjXZwB3sLPYGX4y8i18435bJm8YFdETC8Os2DtGovIKJKCK0Rb57A', 'Patrick', 'Moulin', 0, 1, 1, 400),
-- Admins
                                                                                                                                                             (20, 'admin.boss@ehpad.fr', 1112223334445556, 1, '$pbkdf2$$zP3DwtYzvu0Kf+X2FQHptQ$WdxhcSVAbCVtEBvy+Q0uBNds31VXzoi70fjXZwB3sLPYGX4y8i18435bJm8YFdETC8Os2DtGovIKJKCK0Rb57A', 'Admin', 'Boss', 2, 2, 3, 10000),
                                                                                                                                                             (21, 'technicien.support@ehpad.fr', 2223334445556667, 1, '$pbkdf2$$zP3DwtYzvu0Kf+X2FQHptQ$WdxhcSVAbCVtEBvy+Q0uBNds31VXzoi70fjXZwB3sLPYGX4y8i18435bJm8YFdETC8Os2DtGovIKJKCK0Rb57A', 'Technicien', 'Support', 0, 2, 2, 1500);

-- Rooms (Personal and Common)
-- Colors are in 0xRRGGBB format (integer representation)
INSERT INTO Room (`id`, `name`, `color`, `ownerId`) VALUES
                                                          (1, 'Chambre 101', 16777215, 1), -- White, Jean Dupont
                                                          (2, 'Chambre 102', 16761035, 2), -- Light Pink, Marie Martin
                                                          (3, 'Chambre 103', 15132390, 3), -- Light Blue, Pierre Bernard
                                                          (4, 'Chambre 104', 16777164, 4), -- Light Yellow, Sophie Dubois
                                                          (5, 'Chambre 201', 15128749, 5), -- Light Green, Jacques Robert
                                                          (6, 'Chambre 202', 16764108, 6), -- Peach, Isabelle Richard
                                                          (7, 'Chambre 203', 16119285, 7), -- Lavender, Alain Petit
                                                          (8, 'Chambre 204', 14474460, 8), -- Beige, Monique Durand
                                                          (9, 'Chambre 301', 16753920, 9), -- Orange, Gérard Leroy
                                                          (10, 'Chambre 302', 14745599, 10), -- Cyan, Nicole Moreau
                                                          (11, 'Chambre 303', 16737996, 11), -- Pink, André Simon
                                                          (12, 'Chambre 304', 13434879, 12), -- Sky Blue, Chantal Laurent
                                                          (13, 'Chambre 401', 16776960, 13), -- Yellow, Michel Lefevre
                                                          (14, 'Chambre 402', 10092543, 14), -- Forest Green, Brigitte Roux
                                                          (15, 'Chambre 403', 16750950, 15), -- Gold, Bernard David
-- Common Rooms
                                                          (16, 'Salon Commun RDC', 11184810, NULL), -- Brown
                                                          (17, 'Salle à Manger', 9419919, NULL), -- Gray
                                                          (18, 'Salle d''Activité', 6591981, NULL), -- Dark Blue
                                                          (19, 'Jardin', 3407667, NULL), -- Dark Green
                                                          (20, 'Infirmerie', 16711680, NULL), -- Red
                                                          (21, 'Couloir Etage 1', 14540253, NULL); -- Light Grey

-- Device Types
-- Category: 0=OTHER, 1=LIGHTING, 2=KITCHEN, 3=SECURITY, 4=TEMPERATURE_REGULATION, 5=SPORT, 6=HEALTH, 7=GARDENING
-- Attributes: JSON array of AttributeType ordinals
-- Ordinals: CALORIES_BURNED=0, ACTIVITY_DURATION=1, TEMPERATURE=2, HUMIDITY=3, HEART_RATE=4, BLOOD_PRESSURE=5, BLOOD_OXYGEN=6, BLOOD_GLUCOSE=7, FAT_PERCENTAGE=8, STEPS=9, LAST_SLEEP_DURATION=10, MAX_VO2=11, RESPIRATORY_RATE=12, BODY_TEMPERATURE=13, BODY_WEIGHT=14, BATTERY_LEVEL=15, BODY_HEIGHT=16, LIGHT_INTENSITY=17
INSERT INTO DeviceType (`id`, `name`, `category`, `attributes`) VALUES
                                                                      (1, 'Lampe Connectée', 1, CAST('[15, 17]' AS JSON)), -- Battery, Light Intensity
                                                                      (2, 'Thermostat Intelligent', 4, CAST('[2, 3]' AS JSON)), -- Temperature, Humidity
                                                                      (3, 'Bracelet d''Activité', 6, CAST('[0, 1, 4, 6, 9, 10, 12, 13, 15]' AS JSON)), -- Calories, Activity, HR, SpO2, Steps, Sleep, Resp Rate, Body Temp, Battery
                                                                      (4, 'Tensiomètre Connecté', 6, CAST('[5, 15]' AS JSON)), -- Blood Pressure, Battery
                                                                      (5, 'Balance Intelligente', 6, CAST('[8, 14, 15, 16]' AS JSON)), -- Fat %, Weight, Battery, Height (user enters height)
                                                                      (6, 'Capteur de Porte/Fenêtre', 3, CAST('[15]' AS JSON)), -- Battery
                                                                      (7, 'Caméra de Surveillance', 3, CAST('[]' AS JSON)), -- No specific attributes tracked here, maybe status implied
                                                                      (8, 'Prise Intelligente', 0, CAST('[]' AS JSON)), -- Simple on/off
                                                                      (9, 'Pilulier Intelligent', 6, CAST('[15]' AS JSON)), -- Battery (tracks doses taken via interaction)
                                                                      (10, 'Détecteur de Chute', 6, CAST('[15]' AS JSON)), -- Battery (sends alert on fall)
                                                                      (11, 'Glucomètre Connecté', 6, CAST('[7, 15]' AS JSON)), -- Blood Glucose, Battery
                                                                      (12, 'Oxymètre de Pouls', 6, CAST('[6, 4, 15]' AS JSON)), -- SpO2, HR, Battery
                                                                      (13, 'Tapis de Marche Connecté', 5, CAST('[0, 1, 9, 11]' AS JSON)), -- Calories, Duration, Steps, Max VO2 (estimated)
                                                                      (14, 'Vélo d''Appartement Connecté', 5, CAST('[0, 1, 9, 4]' AS JSON)), -- Calories, Duration, Steps (equiv), HR
                                                                      (15, 'Capteur Qualité de l''Air', 4, CAST('[2, 3, 17]' AS JSON)), -- Temp, Humidity, Light (often includes basic light)
                                                                      (16, 'Humidificateur d''Air', 4, CAST('[3]' AS JSON)), -- Humidity control
                                                                      (17, 'Déambulateur Connecté', 5, CAST('[9, 1]' AS JSON)), -- Steps, Activity Duration
                                                                      (18, 'Capteur de Sommeil (sous matelas)', 6, CAST('[10, 4, 12]' AS JSON)); -- Sleep Duration, HR, Resp Rate

-- Devices
-- Attributes JSON format: [[key_ordinals], [values]]
INSERT INTO Device (`id`, `name`, `description`, `typeId`, `roomId`, `userId`, `attributes`, `powered`, `energyConsumption`, `deletionRequestedById`) VALUES
-- Jean Dupont (User 1, Room 1)
(1, 'Lampe Chevet Jean', 'Lampe sur la table de nuit', 1, 1, 1, CAST('[[15, 17], [85.0, 350.0]]' AS JSON), 1, 5.0, NULL),
(2, 'Bracelet Jean', 'Bracelet d''activité Fitbit', 3, 1, 1, CAST('[[0, 1, 4, 6, 9, 10, 12, 13, 15], [15234.0, 2500.0, 72.0, 98.0, 123456.0, 420.0, 16.0, 36.8, 92.0]]' AS JSON), 1, 0.1, NULL),
(3, 'Tensiomètre Jean', NULL, 4, 1, 1, CAST('[[5, 15], [135.0, 78.0]]' AS JSON), 0, 0.5, NULL), -- BP slightly high (systolic)
(4, 'Pilulier Jean', 'Pilulier pour les médicaments du matin et soir', 9, 1, 1, CAST('[[15], [99.0]]' AS JSON), 1, 0.2, NULL),

-- Marie Martin (User 2, Room 2)
(5, 'Lampe Bureau Marie', NULL, 1, 2, 2, CAST('[[15, 17], [95.0, 500.0]]' AS JSON), 1, 7.0, NULL),
(6, 'Balance Marie', 'Balance dans la salle de bain', 5, 2, 2, CAST('[[8, 14, 15, 16], [28.5, 62.0, 88.0, 160.0]]' AS JSON), 0, 0.3, NULL), -- Healthy weight for 160cm
(7, 'Détecteur Chute Marie', 'Pendentif d''alerte', 10, 2, 2, CAST('[[15], [100.0]]' AS JSON), 1, 0.05, NULL),

-- Pierre Bernard (User 3, Room 3)
(8, 'Thermostat Chambre Pierre', NULL, 2, 3, 3, CAST('[[2, 3], [21.5, 55.0]]' AS JSON), 1, 1.0, NULL),
(9, 'Oxymètre Pierre', 'Oxymètre au doigt', 12, 3, 3, CAST('[[6, 4, 15], [94.0, 85.0, 65.0]]' AS JSON), 0, 0.2, NULL), -- SpO2 slightly low

-- Sophie Dubois (User 4, Room 4)
(10, 'Bracelet Sophie', 'Bracelet d''activité Garmin', 3, 4, 4, CAST('[[0, 1, 4, 6, 9, 10, 12, 13, 15], [35800.0, 5800.0, 68.0, 99.0, 315000.0, 480.0, 15.0, 36.6, 76.0]]' AS JSON), 1, 0.1, NULL),
(11, 'Vélo Appart Sophie', 'Pour exercice quotidien', 14, 4, 4, CAST('[[0, 1, 9, 4], [15000.0, 1200.0, 0.0, 95.0]]' AS JSON), 0, 3.0, NULL), -- Steps irrelevant for bike

-- Jacques Robert (User 5, Room 5)
(12, 'Glucomètre Jacques', 'Pour suivi diabète type 2', 11, 5, 5, CAST('[[7, 15], [155.0, 91.0]]' AS JSON), 0, 0.4, NULL), -- Glucose high post-meal?

-- Isabelle Richard (User 6, Room 6)
(13, 'Capteur Porte Isabelle', 'Sur la porte d''entrée de la chambre', 6, 6, 6, CAST('[[15], [98.0]]' AS JSON), 1, 0.02, NULL),
(14, 'Lampe Plafond Isabelle', NULL, 1, 6, 6, CAST('[[15, 17], [100.0, 600.0]]' AS JSON), 1, 12.0, NULL), -- Assuming no battery for ceiling lamp

-- Alain Petit (User 7, Room 7)
(15, 'Tensiomètre Alain', NULL, 4, 7, 7, CAST('[[5, 15], [145.0, 92.0]]' AS JSON), 0, 0.5, NULL), -- BP high

-- Monique Durand (User 8, Room 8)
(16, 'Balance Monique', NULL, 5, 8, 8, CAST('[[8, 14, 15, 16], [35.0, 78.0, 95.0, 165.0]]' AS JSON), 0, 0.3, NULL), -- Overweight for 165cm, high fat%
(17, 'Capteur Sommeil Monique', 'Placé sous le matelas', 18, 8, 8, CAST('[[10, 4, 12], [380.0, 58.0, 13.0]]' AS JSON), 1, 0.5, NULL), -- Low HR during sleep

-- Gérard Leroy (User 9, Room 9)
(18, 'Déambulateur Gérard', 'Déambulateur connecté pour suivi marche', 17, 9, 9, CAST('[[9, 1], [55000.0, 3200.0]]' AS JSON), 1, 0.3, NULL),

-- Nicole Moreau (User 10, Room 10)
(19, 'Pilulier Nicole', NULL, 9, 10, 10, CAST('[[15], [70.0]]' AS JSON), 1, 0.2, NULL),

-- André Simon (User 11, Room 11)
(20, 'Bracelet André', 'Xiaomi Mi Band', 3, 11, 11, CAST('[[0, 1, 4, 6, 9, 10, 12, 13, 15], [9800.0, 1500.0, 75.0, 97.0, 89000.0, 450.0, 17.0, 37.0, 81.0]]' AS JSON), 1, 0.1, NULL),

-- Chantal Laurent (User 12, Room 12)
(21, 'Lampe Lecture Chantal', NULL, 1, 12, 12, CAST('[[15, 17], [60.0, 450.0]]' AS JSON), 0, 6.0, NULL),

-- Michel Lefevre (User 13, Room 13)
(22, 'Tapis Marche Michel', 'Tapis pour rééducation', 13, 13, 13, CAST('[[0, 1, 9, 11], [8000.0, 950.0, 45000.0, 22.0]]' AS JSON), 0, 200.0, NULL),

-- Brigitte Roux (User 14, Room 14)
(23, 'Tensiomètre Brigitte', NULL, 4, 14, 14, CAST('[[5, 15], [118.0, 75.0]]' AS JSON), 0, 0.5, NULL), -- Healthy BP

-- Bernard David (User 15, Room 15)
(24, 'Oxymètre Bernard', NULL, 12, 15, 15, CAST('[[6, 4, 15], [99.0, 65.0, 80.0]]' AS JSON), 1, 0.2, NULL),

-- Common Area Devices
(25, 'Thermostat Salon RDC', NULL, 2, 16, NULL, CAST('[[2, 3], [22.0, 50.0]]' AS JSON), 1, 1.5, NULL),
(26, 'Lampe Couloir Etage 1 NORD', NULL, 1, 21, NULL, CAST('[[15, 17], [100.0, 200.0]]' AS JSON), 1, 10.0, NULL), -- No battery
(27, 'Lampe Couloir Etage 1 SUD', NULL, 1, 21, NULL, CAST('[[15, 17], [100.0, 200.0]]' AS JSON), 1, 10.0, NULL), -- No battery
(28, 'Caméra Entrée Principale', 'Surveillance hall d''entrée', 7, 16, NULL, CAST('[[], []]' AS JSON), 1, 4.0, NULL),
(29, 'Capteur Qualité Air Salon', NULL, 15, 16, NULL, CAST('[[2, 3, 17], [22.1, 52.0, 400.0]]' AS JSON), 1, 0.8, NULL),
(30, 'Prise Intelligente TV Salon', 'Contrôle la TV commune', 8, 16, NULL, CAST('[[], []]' AS JSON), 1, 0.5, NULL), -- Standby power of the plug itself
(31, 'Tapis Marche Salle Activité', 'Tapis commun', 13, 18, NULL, CAST('[[0, 1, 9, 11], [500.0, 60.0, 2500.0, 20.0]]' AS JSON), 0, 200.0, NULL),
(32, 'Vélo Appart Salle Activité', 'Vélo commun', 14, 18, NULL, CAST('[[0, 1, 9, 4], [300.0, 45.0, 0.0, 90.0]]' AS JSON), 0, 3.0, NULL),
(33, 'Humidificateur Infirmerie', NULL, 16, 20, NULL, CAST('[[3], [60.0]]' AS JSON), 1, 25.0, NULL),
(34, 'Lampe Salle à Manger 1', NULL, 1, 17, NULL, CAST('[[15, 17], [100.0, 700.0]]' AS JSON), 1, 15.0, NULL),
(35, 'Lampe Salle à Manger 2', NULL, 1, 17, NULL, CAST('[[15, 17], [100.0, 700.0]]' AS JSON), 1, 15.0, NULL),
(36, 'Capteur Porte Jardin', 'Accès au jardin', 6, 19, NULL, CAST('[[15], [95.0]]' AS JSON), 1, 0.02, NULL),
(37, 'Tensiomètre Infirmerie', 'Usage par les soignants', 4, 20, NULL, CAST('[[5, 15], [120.0, 80.0]]' AS JSON), 0, 0.6, NULL), -- Represents last measurement or default
(38, 'Balance Infirmerie', 'Usage par les soignants', 5, 20, NULL, CAST('[[8, 14, 15, 16], [0.0, 0.0, 100.0, 0.0]]' AS JSON), 0, 0.4, NULL), -- Represents last measurement or default
(39, 'Oxymètre Infirmerie', 'Usage par les soignants', 12, 20, NULL, CAST('[[6, 4, 15], [98.0, 70.0, 99.0]]' AS JSON), 0, 0.3, NULL), -- Represents last measurement or default
(40, 'Glucomètre Infirmerie', 'Usage par les soignants', 11, 20, NULL, CAST('[[7, 15], [100.0, 96.0]]' AS JSON), 0, 0.4, NULL), -- Represents last measurement or default
(41, 'Thermostat Salle Manger', NULL, 2, 17, NULL, CAST('[[2, 3], [21.0, 48.0]]' AS JSON), 1, 1.5, NULL),
(42, 'Prise Machine à Café', 'Salle à manger', 8, 17, NULL, CAST('[[], []]' AS JSON), 1, 0.5, NULL),
(43, 'Capteur Qualité Air Infirmerie', NULL, 15, 20, NULL, CAST('[[2, 3, 17], [22.5, 55.0, 300.0]]' AS JSON), 1, 0.8, NULL),

-- More devices to reach ~60
(44, 'Lampe Chevet Droite Ch 101', NULL, 1, 1, 1, CAST('[[15, 17], [90.0, 300.0]]' AS JSON), 0, 5.0, NULL),
(45, 'Capteur Fenêtre Ch 102', NULL, 6, 2, 2, CAST('[[15], [99.0]]' AS JSON), 1, 0.02, NULL),
(46, 'Prise Radio Ch 103', NULL, 8, 3, 3, CAST('[[], []]' AS JSON), 1, 0.5, NULL),
(47, 'Lampe Plafond Ch 104', NULL, 1, 4, 4, CAST('[[15, 17], [100.0, 550.0]]' AS JSON), 1, 12.0, NULL),
(48, 'Capteur Mouvement Couloir E1', NULL, 3, 21, NULL, CAST('[[15], [96.0]]' AS JSON), 1, 0.1, NULL), -- Using Bracelet type just for battery attribute
(49, 'Lampe Jardin Allée', NULL, 1, 19, NULL, CAST('[[15, 17], [100.0, 150.0]]' AS JSON), 1, 20.0, NULL),
(50, 'Caméra Jardin', NULL, 7, 19, NULL, CAST('[[], []]' AS JSON), 1, 4.5, NULL),
(51, 'Thermostat Bureau Soignants', NULL, 2, 20, NULL, CAST('[[2, 3], [21.0, 45.0]]' AS JSON), 1, 1.0, NULL), -- Using Infirmerie room for staff office area
(52, 'Prise Ordinateur Bureau Soignants', NULL, 8, 20, NULL, CAST('[[], []]' AS JSON), 1, 0.5, NULL),
(53, 'Lampe Bureau Soignants', NULL, 1, 20, NULL, CAST('[[15, 17], [100.0, 800.0]]' AS JSON), 1, 9.0, NULL),
(54, 'Capteur Porte Infirmerie', NULL, 6, 20, NULL, CAST('[[15], [100.0]]' AS JSON), 1, 0.02, NULL),
(55, 'Lampe Lecture Ch 201', NULL, 1, 5, 5, CAST('[[15, 17], [75.0, 400.0]]' AS JSON), 1, 6.0, NULL),
(56, 'Oxymètre Ch 202', NULL, 12, 6, 6, CAST('[[6, 4, 15], [97.0, 78.0, 82.0]]' AS JSON), 0, 0.2, NULL),
(57, 'Pilulier Ch 301', NULL, 9, 9, 9, CAST('[[15], [92.0]]' AS JSON), 1, 0.2, NULL),
(58, 'Capteur Sommeil Ch 303', NULL, 18, 11, 11, CAST('[[10, 4, 12], [410.0, 62.0, 14.0]]' AS JSON), 1, 0.5, NULL),
(59, 'Lampe Salle Activité', NULL, 1, 18, NULL, CAST('[[15, 17], [100.0, 900.0]]' AS JSON), 1, 18.0, NULL),
(60, 'Capteur Fenêtre Salle Manger', NULL, 6, 17, NULL, CAST('[[15], [97.0]]' AS JSON), 1, 0.02, NULL),
(61, 'Détecteur Chute Ch 401', NULL, 10, 13, 13, CAST('[[15], [99.0]]' AS JSON), 1, 0.05, NULL);

-- Power Logs (Sample entries)
INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (1, 'POWER_ON', 5.0, NOW() - INTERVAL 2 HOUR),
                                                                               (5, 'POWER_ON', 7.0, NOW() - INTERVAL 1 HOUR),
                                                                               (25, 'POWER_ON', 1.5, NOW() - INTERVAL 5 HOUR),
                                                                               (28, 'POWER_ON', 4.0, NOW() - INTERVAL 24 HOUR),
                                                                               (30, 'POWER_ON', 0.5, NOW() - INTERVAL 10 HOUR),
                                                                               (1, 'POWER_OFF', 5.0, NOW() - INTERVAL 1 HOUR), -- Turned off lamp 1
                                                                               (21, 'POWER_ON', 6.0, NOW() - INTERVAL 30 MINUTE), -- Chantal started reading
                                                                               (31, 'POWER_ON', 200.0, NOW() - INTERVAL 45 MINUTE), -- Someone started walking
                                                                               (31, 'POWER_OFF', 200.0, NOW() - INTERVAL 15 MINUTE); -- Someone finished walking

-- Action Logs (Sample entries)
-- TargetType: 0=DEVICE, 1=USER, 2=ROOM, 3=DEVICE_TYPE
-- Operation: 0=CREATE, 1=UPDATE, 2=DELETE
-- Flags: POWER_ON=1, POWER_OFF=2, DELETE_REQUESTED=4, DELETE_REQUEST_DELETED=8, PASSWORD_CHANGED=16, EMAIL_CONFIRMED=32
INSERT INTO ActionLog (`userId`, `targetId`, `targetType`, `operation`, `flags`, `time`) VALUES
                                                                                               (1, 1, 0, 1, 1, NOW() - INTERVAL 2 HOUR), -- Jean turned on device 1 (flag POWER_ON)
                                                                                               (16, 2, 0, 1, 0, NOW() - INTERVAL 2 DAY), -- Sylvie (caregiver) updated device 2 (Bracelet Jean - maybe settings)
                                                                                               (20, 5, 3, 0, 0, NOW() - INTERVAL 1 MONTH), -- Admin created DeviceType 5 (Balance)
                                                                                               (NULL, 7, 1, 1, 32, NOW() - INTERVAL 5 DAY), -- System confirmed email for user 7 (flag EMAIL_CONFIRMED)
                                                                                               (4, 4, 1, 1, 16, NOW() - INTERVAL 3 DAY), -- Sophie changed her password (flag PASSWORD_CHANGED)
                                                                                               (17, 25, 0, 1, 0, NOW() - INTERVAL 6 HOUR), -- Luc (caregiver) adjusted Thermostat 25
                                                                                               (1, 1, 0, 1, 2, NOW() - INTERVAL 1 HOUR), -- Jean turned off device 1 (flag POWER_OFF)
                                                                                               (18, 56, 0, 0, 0, NOW() - INTERVAL 1 DAY), -- Nathalie (caregiver) added device 56 (Oxymètre Ch 202)
                                                                                               (20, 15, 2, 1, 0, NOW() - INTERVAL 1 WEEK), -- Admin updated Room 15 (Chambre 403) - maybe changed color
                                                                                               (13, 22, 0, 1, 4, NOW() - INTERVAL 10 MINUTE); -- Michel requested deletion of device 22 (Tapis Marche)

-- Login Logs (Sample entries)
INSERT INTO LoginLog (`userId`, `time`) VALUES
                                              (1, NOW() - INTERVAL 5 MINUTE),
                                              (4, NOW() - INTERVAL 1 HOUR),
                                              (16, NOW() - INTERVAL 15 MINUTE),
                                              (20, NOW() - INTERVAL 2 HOUR),
                                              (1, NOW() - INTERVAL 1 DAY),
                                              (18, NOW() - INTERVAL 3 HOUR),
                                              (17, NOW() - INTERVAL 8 HOUR),
                                              (4, NOW() - INTERVAL 2 DAY),
                                              (20, NOW() - INTERVAL 1 DAY);

-- Invite Codes (Sample entries)
-- Role: 0=RESIDENT, 1=CAREGIVER, 2=ADMIN
INSERT INTO InviteCode (`id`, `usagesLeft`, `role`, `creatorId`, `createdAt`) VALUES
                                                                                    ('NEWCAREGIVER2024', 1, 1, 20, NOW() - INTERVAL 3 DAY), -- Invite code for one caregiver, created by Admin Boss
                                                                                    ('RESIDENTTEST01', 5, 0, 21, NOW() - INTERVAL 1 WEEK); -- Invite code for 5 residents, created by Tech Support

-- Generated PowerLog Data --
-- Timestamp: 2025-04-07 23:54:15 --

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (1, 'POWER_OFF', 5.0, '2025-04-05 11:54:15'),
                                                                               (1, 'POWER_ON', 5.0, '2025-04-05 18:59:15'),
                                                                               (1, 'POWER_OFF', 5.0, '2025-04-06 02:08:15'),
                                                                               (1, 'POWER_ON', 5.0, '2025-04-06 06:47:15'),
                                                                               (1, 'POWER_OFF', 5.0, '2025-04-06 09:02:15'),
                                                                               (1, 'POWER_ON', 5.0, '2025-04-06 14:47:15'),
                                                                               (1, 'POWER_OFF', 5.0, '2025-04-06 21:20:15'),
                                                                               (1, 'POWER_ON', 5.0, '2025-04-07 06:39:15'),
                                                                               (1, 'POWER_OFF', 5.0, '2025-04-07 11:10:15'),
                                                                               (1, 'POWER_ON', 5.0, '2025-04-07 12:24:15'),
                                                                               (1, 'POWER_OFF', 5.0, '2025-04-07 17:50:15'),
                                                                               (1, 'POWER_ON', 5.0, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (2, 'POWER_OFF', 0.1, '2025-04-06 03:11:15'),
                                                                               (2, 'POWER_ON', 0.1, '2025-04-06 11:08:15'),
                                                                               (2, 'POWER_OFF', 0.1, '2025-04-06 16:33:15'),
                                                                               (2, 'POWER_ON', 0.1, '2025-04-06 23:27:15'),
                                                                               (2, 'POWER_OFF', 0.1, '2025-04-07 08:47:15'),
                                                                               (2, 'POWER_ON', 0.1, '2025-04-07 15:58:15'),
                                                                               (2, 'POWER_OFF', 0.1, '2025-04-07 21:02:15'),
                                                                               (2, 'POWER_ON', 0.1, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (3, 'POWER_ON', 0.5, '2025-04-05 00:41:15'),
                                                                               (3, 'POWER_OFF', 0.5, '2025-04-05 06:27:15'),
                                                                               (3, 'POWER_ON', 0.5, '2025-04-05 15:28:15'),
                                                                               (3, 'POWER_OFF', 0.5, '2025-04-05 20:46:15'),
                                                                               (3, 'POWER_ON', 0.5, '2025-04-06 04:07:15'),
                                                                               (3, 'POWER_OFF', 0.5, '2025-04-06 05:34:15'),
                                                                               (3, 'POWER_ON', 0.5, '2025-04-06 11:13:15'),
                                                                               (3, 'POWER_OFF', 0.5, '2025-04-06 15:22:15'),
                                                                               (3, 'POWER_ON', 0.5, '2025-04-06 21:59:15'),
                                                                               (3, 'POWER_OFF', 0.5, '2025-04-07 03:27:15'),
                                                                               (3, 'POWER_ON', 0.5, '2025-04-07 07:41:15'),
                                                                               (3, 'POWER_OFF', 0.5, '2025-04-07 12:27:15'),
                                                                               (3, 'POWER_ON', 0.5, '2025-04-07 19:13:15'),
                                                                               (3, 'POWER_OFF', 0.5, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (4, 'POWER_OFF', 0.2, '2025-04-06 09:03:15'),
                                                                               (4, 'POWER_ON', 0.2, '2025-04-06 12:38:15'),
                                                                               (4, 'POWER_OFF', 0.2, '2025-04-06 19:28:15'),
                                                                               (4, 'POWER_ON', 0.2, '2025-04-07 05:02:15'),
                                                                               (4, 'POWER_OFF', 0.2, '2025-04-07 05:14:15'),
                                                                               (4, 'POWER_ON', 0.2, '2025-04-07 06:06:15'),
                                                                               (4, 'POWER_OFF', 0.2, '2025-04-07 09:53:15'),
                                                                               (4, 'POWER_ON', 0.2, '2025-04-07 16:01:15'),
                                                                               (4, 'POWER_OFF', 0.2, '2025-04-07 17:17:15'),
                                                                               (4, 'POWER_ON', 0.2, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (5, 'POWER_OFF', 7.0, '2025-04-07 03:44:15'),
                                                                               (5, 'POWER_ON', 7.0, '2025-04-07 12:47:15'),
                                                                               (5, 'POWER_OFF', 7.0, '2025-04-07 15:59:15'),
                                                                               (5, 'POWER_ON', 7.0, '2025-04-07 16:08:15'),
                                                                               (5, 'POWER_OFF', 7.0, '2025-04-07 21:16:15'),
                                                                               (5, 'POWER_ON', 7.0, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (6, 'POWER_OFF', 0.3, '2025-04-04 21:23:15'),
                                                                               (6, 'POWER_ON', 0.3, '2025-04-05 00:20:15'),
                                                                               (6, 'POWER_OFF', 0.3, '2025-04-05 05:24:15'),
                                                                               (6, 'POWER_ON', 0.3, '2025-04-05 13:13:15'),
                                                                               (6, 'POWER_OFF', 0.3, '2025-04-05 17:22:15'),
                                                                               (6, 'POWER_ON', 0.3, '2025-04-05 22:50:15'),
                                                                               (6, 'POWER_OFF', 0.3, '2025-04-06 03:57:15'),
                                                                               (6, 'POWER_ON', 0.3, '2025-04-06 13:30:15'),
                                                                               (6, 'POWER_OFF', 0.3, '2025-04-06 22:38:15'),
                                                                               (6, 'POWER_ON', 0.3, '2025-04-07 00:53:15'),
                                                                               (6, 'POWER_OFF', 0.3, '2025-04-07 10:29:15'),
                                                                               (6, 'POWER_ON', 0.3, '2025-04-07 15:31:15'),
                                                                               (6, 'POWER_OFF', 0.3, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (7, 'POWER_OFF', 0.05, '2025-04-05 20:53:15'),
                                                                               (7, 'POWER_ON', 0.05, '2025-04-06 02:30:15'),
                                                                               (7, 'POWER_OFF', 0.05, '2025-04-06 11:59:15'),
                                                                               (7, 'POWER_ON', 0.05, '2025-04-06 21:33:15'),
                                                                               (7, 'POWER_OFF', 0.05, '2025-04-07 03:10:15'),
                                                                               (7, 'POWER_ON', 0.05, '2025-04-07 11:18:15'),
                                                                               (7, 'POWER_OFF', 0.05, '2025-04-07 15:44:15'),
                                                                               (7, 'POWER_ON', 0.05, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (8, 'POWER_OFF', 1.0, '2025-04-05 18:41:15'),
                                                                               (8, 'POWER_ON', 1.0, '2025-04-05 21:40:15'),
                                                                               (8, 'POWER_OFF', 1.0, '2025-04-06 05:34:15'),
                                                                               (8, 'POWER_ON', 1.0, '2025-04-06 13:17:15'),
                                                                               (8, 'POWER_OFF', 1.0, '2025-04-06 15:09:15'),
                                                                               (8, 'POWER_ON', 1.0, '2025-04-06 18:42:15'),
                                                                               (8, 'POWER_OFF', 1.0, '2025-04-06 21:44:15'),
                                                                               (8, 'POWER_ON', 1.0, '2025-04-07 02:09:15'),
                                                                               (8, 'POWER_OFF', 1.0, '2025-04-07 07:59:15'),
                                                                               (8, 'POWER_ON', 1.0, '2025-04-07 08:15:15'),
                                                                               (8, 'POWER_OFF', 1.0, '2025-04-07 11:50:15'),
                                                                               (8, 'POWER_ON', 1.0, '2025-04-07 13:50:15'),
                                                                               (8, 'POWER_OFF', 1.0, '2025-04-07 19:26:15'),
                                                                               (8, 'POWER_ON', 1.0, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (9, 'POWER_ON', 0.2, '2025-04-05 07:25:15'),
                                                                               (9, 'POWER_OFF', 0.2, '2025-04-05 16:40:15'),
                                                                               (9, 'POWER_ON', 0.2, '2025-04-05 21:29:15'),
                                                                               (9, 'POWER_OFF', 0.2, '2025-04-06 04:59:15'),
                                                                               (9, 'POWER_ON', 0.2, '2025-04-06 09:43:15'),
                                                                               (9, 'POWER_OFF', 0.2, '2025-04-06 12:17:15'),
                                                                               (9, 'POWER_ON', 0.2, '2025-04-06 21:56:15'),
                                                                               (9, 'POWER_OFF', 0.2, '2025-04-07 01:02:15'),
                                                                               (9, 'POWER_ON', 0.2, '2025-04-07 04:58:15'),
                                                                               (9, 'POWER_OFF', 0.2, '2025-04-07 08:49:15'),
                                                                               (9, 'POWER_ON', 0.2, '2025-04-07 16:41:15'),
                                                                               (9, 'POWER_OFF', 0.2, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (10, 'POWER_OFF', 0.1, '2025-04-04 19:31:15'),
                                                                               (10, 'POWER_ON', 0.1, '2025-04-05 05:17:15'),
                                                                               (10, 'POWER_OFF', 0.1, '2025-04-05 13:04:15'),
                                                                               (10, 'POWER_ON', 0.1, '2025-04-05 22:17:15'),
                                                                               (10, 'POWER_OFF', 0.1, '2025-04-05 22:23:15'),
                                                                               (10, 'POWER_ON', 0.1, '2025-04-06 05:02:15'),
                                                                               (10, 'POWER_OFF', 0.1, '2025-04-06 09:32:15'),
                                                                               (10, 'POWER_ON', 0.1, '2025-04-06 14:35:15'),
                                                                               (10, 'POWER_OFF', 0.1, '2025-04-06 21:35:15'),
                                                                               (10, 'POWER_ON', 0.1, '2025-04-06 21:58:15'),
                                                                               (10, 'POWER_OFF', 0.1, '2025-04-07 03:16:15'),
                                                                               (10, 'POWER_ON', 0.1, '2025-04-07 07:33:15'),
                                                                               (10, 'POWER_OFF', 0.1, '2025-04-07 14:22:15'),
                                                                               (10, 'POWER_ON', 0.1, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (11, 'POWER_ON', 3.0, '2025-04-07 06:07:15'),
                                                                               (11, 'POWER_OFF', 3.0, '2025-04-07 06:28:15'),
                                                                               (11, 'POWER_ON', 3.0, '2025-04-07 07:56:15'),
                                                                               (11, 'POWER_OFF', 3.0, '2025-04-07 13:10:15'),
                                                                               (11, 'POWER_ON', 3.0, '2025-04-07 15:54:15'),
                                                                               (11, 'POWER_OFF', 3.0, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (12, 'POWER_ON', 0.4, '2025-04-05 10:29:15'),
                                                                               (12, 'POWER_OFF', 0.4, '2025-04-05 16:49:15'),
                                                                               (12, 'POWER_ON', 0.4, '2025-04-06 01:16:15'),
                                                                               (12, 'POWER_OFF', 0.4, '2025-04-06 02:28:15'),
                                                                               (12, 'POWER_ON', 0.4, '2025-04-06 09:01:15'),
                                                                               (12, 'POWER_OFF', 0.4, '2025-04-06 16:39:15'),
                                                                               (12, 'POWER_ON', 0.4, '2025-04-07 00:00:15'),
                                                                               (12, 'POWER_OFF', 0.4, '2025-04-07 05:03:15'),
                                                                               (12, 'POWER_ON', 0.4, '2025-04-07 05:33:15'),
                                                                               (12, 'POWER_OFF', 0.4, '2025-04-07 11:04:15'),
                                                                               (12, 'POWER_ON', 0.4, '2025-04-07 15:02:15'),
                                                                               (12, 'POWER_OFF', 0.4, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (13, 'POWER_OFF', 0.02, '2025-04-05 15:41:15'),
                                                                               (13, 'POWER_ON', 0.02, '2025-04-05 18:28:15'),
                                                                               (13, 'POWER_OFF', 0.02, '2025-04-06 01:29:15'),
                                                                               (13, 'POWER_ON', 0.02, '2025-04-06 03:19:15'),
                                                                               (13, 'POWER_OFF', 0.02, '2025-04-06 10:06:15'),
                                                                               (13, 'POWER_ON', 0.02, '2025-04-06 19:32:15'),
                                                                               (13, 'POWER_OFF', 0.02, '2025-04-06 22:20:15'),
                                                                               (13, 'POWER_ON', 0.02, '2025-04-07 05:58:15'),
                                                                               (13, 'POWER_OFF', 0.02, '2025-04-07 08:02:15'),
                                                                               (13, 'POWER_ON', 0.02, '2025-04-07 13:21:15'),
                                                                               (13, 'POWER_OFF', 0.02, '2025-04-07 13:58:15'),
                                                                               (13, 'POWER_ON', 0.02, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (14, 'POWER_OFF', 12.0, '2025-04-06 11:38:15'),
                                                                               (14, 'POWER_ON', 12.0, '2025-04-06 16:42:15'),
                                                                               (14, 'POWER_OFF', 12.0, '2025-04-07 01:19:15'),
                                                                               (14, 'POWER_ON', 12.0, '2025-04-07 09:52:15'),
                                                                               (14, 'POWER_OFF', 12.0, '2025-04-07 13:38:15'),
                                                                               (14, 'POWER_ON', 12.0, '2025-04-07 16:48:15'),
                                                                               (14, 'POWER_OFF', 12.0, '2025-04-07 20:05:15'),
                                                                               (14, 'POWER_ON', 12.0, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (15, 'POWER_ON', 0.5, '2025-04-05 06:28:15'),
                                                                               (15, 'POWER_OFF', 0.5, '2025-04-05 14:39:15'),
                                                                               (15, 'POWER_ON', 0.5, '2025-04-05 21:51:15'),
                                                                               (15, 'POWER_OFF', 0.5, '2025-04-06 03:38:15'),
                                                                               (15, 'POWER_ON', 0.5, '2025-04-06 13:33:15'),
                                                                               (15, 'POWER_OFF', 0.5, '2025-04-06 18:53:15'),
                                                                               (15, 'POWER_ON', 0.5, '2025-04-06 19:43:15'),
                                                                               (15, 'POWER_OFF', 0.5, '2025-04-06 22:41:15'),
                                                                               (15, 'POWER_ON', 0.5, '2025-04-07 00:05:15'),
                                                                               (15, 'POWER_OFF', 0.5, '2025-04-07 09:43:15'),
                                                                               (15, 'POWER_ON', 0.5, '2025-04-07 13:44:15'),
                                                                               (15, 'POWER_OFF', 0.5, '2025-04-07 14:49:15'),
                                                                               (15, 'POWER_ON', 0.5, '2025-04-07 17:02:15'),
                                                                               (15, 'POWER_OFF', 0.5, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (16, 'POWER_OFF', 0.3, '2025-04-07 04:00:15'),
                                                                               (16, 'POWER_ON', 0.3, '2025-04-07 10:02:15'),
                                                                               (16, 'POWER_OFF', 0.3, '2025-04-07 15:31:15'),
                                                                               (16, 'POWER_ON', 0.3, '2025-04-07 15:52:15'),
                                                                               (16, 'POWER_OFF', 0.3, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (17, 'POWER_OFF', 0.5, '2025-04-04 09:22:15'),
                                                                               (17, 'POWER_ON', 0.5, '2025-04-04 16:15:15'),
                                                                               (17, 'POWER_OFF', 0.5, '2025-04-04 20:00:15'),
                                                                               (17, 'POWER_ON', 0.5, '2025-04-04 23:32:15'),
                                                                               (17, 'POWER_OFF', 0.5, '2025-04-05 03:16:15'),
                                                                               (17, 'POWER_ON', 0.5, '2025-04-05 10:40:15'),
                                                                               (17, 'POWER_OFF', 0.5, '2025-04-05 17:47:15'),
                                                                               (17, 'POWER_ON', 0.5, '2025-04-06 01:29:15'),
                                                                               (17, 'POWER_OFF', 0.5, '2025-04-06 10:37:15'),
                                                                               (17, 'POWER_ON', 0.5, '2025-04-06 18:03:15'),
                                                                               (17, 'POWER_OFF', 0.5, '2025-04-07 03:33:15'),
                                                                               (17, 'POWER_ON', 0.5, '2025-04-07 12:18:15'),
                                                                               (17, 'POWER_OFF', 0.5, '2025-04-07 16:08:15'),
                                                                               (17, 'POWER_ON', 0.5, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (18, 'POWER_ON', 0.3, '2025-04-06 11:49:15'),
                                                                               (18, 'POWER_OFF', 0.3, '2025-04-06 15:44:15'),
                                                                               (18, 'POWER_ON', 0.3, '2025-04-07 01:23:15'),
                                                                               (18, 'POWER_OFF', 0.3, '2025-04-07 04:29:15'),
                                                                               (18, 'POWER_ON', 0.3, '2025-04-07 11:00:15'),
                                                                               (18, 'POWER_OFF', 0.3, '2025-04-07 15:28:15'),
                                                                               (18, 'POWER_ON', 0.3, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (19, 'POWER_OFF', 0.2, '2025-04-06 17:50:15'),
                                                                               (19, 'POWER_ON', 0.2, '2025-04-06 21:02:15'),
                                                                               (19, 'POWER_OFF', 0.2, '2025-04-07 04:31:15'),
                                                                               (19, 'POWER_ON', 0.2, '2025-04-07 06:04:15'),
                                                                               (19, 'POWER_OFF', 0.2, '2025-04-07 15:20:15'),
                                                                               (19, 'POWER_ON', 0.2, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (20, 'POWER_OFF', 0.1, '2025-04-06 01:30:15'),
                                                                               (20, 'POWER_ON', 0.1, '2025-04-06 01:55:15'),
                                                                               (20, 'POWER_OFF', 0.1, '2025-04-06 06:55:15'),
                                                                               (20, 'POWER_ON', 0.1, '2025-04-06 14:51:15'),
                                                                               (20, 'POWER_OFF', 0.1, '2025-04-06 22:59:15'),
                                                                               (20, 'POWER_ON', 0.1, '2025-04-07 03:32:15'),
                                                                               (20, 'POWER_OFF', 0.1, '2025-04-07 09:07:15'),
                                                                               (20, 'POWER_ON', 0.1, '2025-04-07 16:28:15'),
                                                                               (20, 'POWER_OFF', 0.1, '2025-04-07 16:34:15'),
                                                                               (20, 'POWER_ON', 0.1, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (21, 'POWER_ON', 6.0, '2025-04-06 01:31:15'),
                                                                               (21, 'POWER_OFF', 6.0, '2025-04-06 02:41:15'),
                                                                               (21, 'POWER_ON', 6.0, '2025-04-06 12:15:15'),
                                                                               (21, 'POWER_OFF', 6.0, '2025-04-06 20:03:15'),
                                                                               (21, 'POWER_ON', 6.0, '2025-04-07 03:01:15'),
                                                                               (21, 'POWER_OFF', 6.0, '2025-04-07 09:01:15'),
                                                                               (21, 'POWER_ON', 6.0, '2025-04-07 18:56:15'),
                                                                               (21, 'POWER_OFF', 6.0, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (22, 'POWER_ON', 200.0, '2025-04-05 12:48:15'),
                                                                               (22, 'POWER_OFF', 200.0, '2025-04-05 13:15:15'),
                                                                               (22, 'POWER_ON', 200.0, '2025-04-05 14:52:15'),
                                                                               (22, 'POWER_OFF', 200.0, '2025-04-06 00:15:15'),
                                                                               (22, 'POWER_ON', 200.0, '2025-04-06 07:41:15'),
                                                                               (22, 'POWER_OFF', 200.0, '2025-04-06 14:13:15'),
                                                                               (22, 'POWER_ON', 200.0, '2025-04-06 17:11:15'),
                                                                               (22, 'POWER_OFF', 200.0, '2025-04-06 18:49:15'),
                                                                               (22, 'POWER_ON', 200.0, '2025-04-06 19:48:15'),
                                                                               (22, 'POWER_OFF', 200.0, '2025-04-07 03:15:15'),
                                                                               (22, 'POWER_ON', 200.0, '2025-04-07 05:54:15'),
                                                                               (22, 'POWER_OFF', 200.0, '2025-04-07 07:12:15'),
                                                                               (22, 'POWER_ON', 200.0, '2025-04-07 15:51:15'),
                                                                               (22, 'POWER_OFF', 200.0, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (23, 'POWER_OFF', 0.5, '2025-04-06 06:26:15'),
                                                                               (23, 'POWER_ON', 0.5, '2025-04-06 12:13:15'),
                                                                               (23, 'POWER_OFF', 0.5, '2025-04-06 15:57:15'),
                                                                               (23, 'POWER_ON', 0.5, '2025-04-06 23:17:15'),
                                                                               (23, 'POWER_OFF', 0.5, '2025-04-07 03:22:15'),
                                                                               (23, 'POWER_ON', 0.5, '2025-04-07 11:42:15'),
                                                                               (23, 'POWER_OFF', 0.5, '2025-04-07 20:05:15'),
                                                                               (23, 'POWER_ON', 0.5, '2025-04-07 20:35:15'),
                                                                               (23, 'POWER_OFF', 0.5, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (24, 'POWER_ON', 0.2, '2025-04-04 21:56:15'),
                                                                               (24, 'POWER_OFF', 0.2, '2025-04-05 07:25:15'),
                                                                               (24, 'POWER_ON', 0.2, '2025-04-05 13:36:15'),
                                                                               (24, 'POWER_OFF', 0.2, '2025-04-05 16:53:15'),
                                                                               (24, 'POWER_ON', 0.2, '2025-04-05 17:15:15'),
                                                                               (24, 'POWER_OFF', 0.2, '2025-04-05 22:08:15'),
                                                                               (24, 'POWER_ON', 0.2, '2025-04-05 23:55:15'),
                                                                               (24, 'POWER_OFF', 0.2, '2025-04-06 09:14:15'),
                                                                               (24, 'POWER_ON', 0.2, '2025-04-06 14:16:15'),
                                                                               (24, 'POWER_OFF', 0.2, '2025-04-06 21:35:15'),
                                                                               (24, 'POWER_ON', 0.2, '2025-04-06 23:05:15'),
                                                                               (24, 'POWER_OFF', 0.2, '2025-04-07 06:26:15'),
                                                                               (24, 'POWER_ON', 0.2, '2025-04-07 13:55:15'),
                                                                               (24, 'POWER_OFF', 0.2, '2025-04-07 23:31:15'),
                                                                               (24, 'POWER_ON', 0.2, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (25, 'POWER_ON', 1.5, '2025-04-06 01:09:15'),
                                                                               (25, 'POWER_OFF', 1.5, '2025-04-06 10:33:15'),
                                                                               (25, 'POWER_ON', 1.5, '2025-04-06 17:57:15'),
                                                                               (25, 'POWER_OFF', 1.5, '2025-04-06 19:51:15'),
                                                                               (25, 'POWER_ON', 1.5, '2025-04-07 02:27:15'),
                                                                               (25, 'POWER_OFF', 1.5, '2025-04-07 08:44:15'),
                                                                               (25, 'POWER_ON', 1.5, '2025-04-07 13:06:15'),
                                                                               (25, 'POWER_OFF', 1.5, '2025-04-07 14:07:15'),
                                                                               (25, 'POWER_ON', 1.5, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (26, 'POWER_ON', 10.0, '2025-04-06 05:49:15'),
                                                                               (26, 'POWER_OFF', 10.0, '2025-04-06 14:18:15'),
                                                                               (26, 'POWER_ON', 10.0, '2025-04-06 23:48:15'),
                                                                               (26, 'POWER_OFF', 10.0, '2025-04-07 03:07:15'),
                                                                               (26, 'POWER_ON', 10.0, '2025-04-07 11:48:15'),
                                                                               (26, 'POWER_OFF', 10.0, '2025-04-07 21:45:15'),
                                                                               (26, 'POWER_ON', 10.0, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (27, 'POWER_OFF', 10.0, '2025-04-06 10:28:15'),
                                                                               (27, 'POWER_ON', 10.0, '2025-04-06 13:26:15'),
                                                                               (27, 'POWER_OFF', 10.0, '2025-04-06 15:57:15'),
                                                                               (27, 'POWER_ON', 10.0, '2025-04-06 18:28:15'),
                                                                               (27, 'POWER_OFF', 10.0, '2025-04-06 22:49:15'),
                                                                               (27, 'POWER_ON', 10.0, '2025-04-07 03:00:15'),
                                                                               (27, 'POWER_OFF', 10.0, '2025-04-07 12:28:15'),
                                                                               (27, 'POWER_ON', 10.0, '2025-04-07 15:44:15'),
                                                                               (27, 'POWER_OFF', 10.0, '2025-04-07 21:35:15'),
                                                                               (27, 'POWER_ON', 10.0, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (28, 'POWER_ON', 4.0, '2025-04-06 23:27:15'),
                                                                               (28, 'POWER_OFF', 4.0, '2025-04-07 09:12:15'),
                                                                               (28, 'POWER_ON', 4.0, '2025-04-07 14:40:15'),
                                                                               (28, 'POWER_OFF', 4.0, '2025-04-07 19:24:15'),
                                                                               (28, 'POWER_ON', 4.0, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (29, 'POWER_ON', 0.8, '2025-04-05 01:39:15'),
                                                                               (29, 'POWER_OFF', 0.8, '2025-04-05 06:44:15'),
                                                                               (29, 'POWER_ON', 0.8, '2025-04-05 10:11:15'),
                                                                               (29, 'POWER_OFF', 0.8, '2025-04-05 14:19:15'),
                                                                               (29, 'POWER_ON', 0.8, '2025-04-05 16:47:15'),
                                                                               (29, 'POWER_OFF', 0.8, '2025-04-06 01:24:15'),
                                                                               (29, 'POWER_ON', 0.8, '2025-04-06 02:31:15'),
                                                                               (29, 'POWER_OFF', 0.8, '2025-04-06 11:32:15'),
                                                                               (29, 'POWER_ON', 0.8, '2025-04-06 15:50:15'),
                                                                               (29, 'POWER_OFF', 0.8, '2025-04-06 23:53:15'),
                                                                               (29, 'POWER_ON', 0.8, '2025-04-07 06:23:15'),
                                                                               (29, 'POWER_OFF', 0.8, '2025-04-07 14:44:15'),
                                                                               (29, 'POWER_ON', 0.8, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (30, 'POWER_OFF', 0.5, '2025-04-06 19:42:15'),
                                                                               (30, 'POWER_ON', 0.5, '2025-04-06 19:47:15'),
                                                                               (30, 'POWER_OFF', 0.5, '2025-04-06 22:24:15'),
                                                                               (30, 'POWER_ON', 0.5, '2025-04-07 01:41:15'),
                                                                               (30, 'POWER_OFF', 0.5, '2025-04-07 10:52:15'),
                                                                               (30, 'POWER_ON', 0.5, '2025-04-07 12:30:15'),
                                                                               (30, 'POWER_OFF', 0.5, '2025-04-07 16:41:15'),
                                                                               (30, 'POWER_ON', 0.5, '2025-04-07 19:34:15'),
                                                                               (30, 'POWER_OFF', 0.5, '2025-04-07 20:47:15'),
                                                                               (30, 'POWER_ON', 0.5, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (31, 'POWER_OFF', 200.0, '2025-04-05 05:14:15'),
                                                                               (31, 'POWER_ON', 200.0, '2025-04-05 06:28:15'),
                                                                               (31, 'POWER_OFF', 200.0, '2025-04-05 12:40:15'),
                                                                               (31, 'POWER_ON', 200.0, '2025-04-05 18:55:15'),
                                                                               (31, 'POWER_OFF', 200.0, '2025-04-05 20:17:15'),
                                                                               (31, 'POWER_ON', 200.0, '2025-04-06 01:08:15'),
                                                                               (31, 'POWER_OFF', 200.0, '2025-04-06 08:49:15'),
                                                                               (31, 'POWER_ON', 200.0, '2025-04-06 14:27:15'),
                                                                               (31, 'POWER_OFF', 200.0, '2025-04-06 23:13:15'),
                                                                               (31, 'POWER_ON', 200.0, '2025-04-07 03:41:15'),
                                                                               (31, 'POWER_OFF', 200.0, '2025-04-07 08:21:15'),
                                                                               (31, 'POWER_ON', 200.0, '2025-04-07 15:20:15'),
                                                                               (31, 'POWER_OFF', 200.0, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (32, 'POWER_OFF', 3.0, '2025-04-05 08:21:15'),
                                                                               (32, 'POWER_ON', 3.0, '2025-04-05 10:54:15'),
                                                                               (32, 'POWER_OFF', 3.0, '2025-04-05 20:26:15'),
                                                                               (32, 'POWER_ON', 3.0, '2025-04-06 03:44:15'),
                                                                               (32, 'POWER_OFF', 3.0, '2025-04-06 12:20:15'),
                                                                               (32, 'POWER_ON', 3.0, '2025-04-06 14:49:15'),
                                                                               (32, 'POWER_OFF', 3.0, '2025-04-06 23:10:15'),
                                                                               (32, 'POWER_ON', 3.0, '2025-04-07 02:59:15'),
                                                                               (32, 'POWER_OFF', 3.0, '2025-04-07 11:45:15'),
                                                                               (32, 'POWER_ON', 3.0, '2025-04-07 12:40:15'),
                                                                               (32, 'POWER_OFF', 3.0, '2025-04-07 22:22:15'),
                                                                               (32, 'POWER_ON', 3.0, '2025-04-07 23:11:15'),
                                                                               (32, 'POWER_OFF', 3.0, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (33, 'POWER_ON', 25.0, '2025-04-06 08:04:15'),
                                                                               (33, 'POWER_OFF', 25.0, '2025-04-06 15:14:15'),
                                                                               (33, 'POWER_ON', 25.0, '2025-04-07 00:16:15'),
                                                                               (33, 'POWER_OFF', 25.0, '2025-04-07 06:01:15'),
                                                                               (33, 'POWER_ON', 25.0, '2025-04-07 07:37:15'),
                                                                               (33, 'POWER_OFF', 25.0, '2025-04-07 10:27:15'),
                                                                               (33, 'POWER_ON', 25.0, '2025-04-07 11:47:15'),
                                                                               (33, 'POWER_OFF', 25.0, '2025-04-07 16:41:15'),
                                                                               (33, 'POWER_ON', 25.0, '2025-04-07 17:05:15'),
                                                                               (33, 'POWER_OFF', 25.0, '2025-04-07 21:53:15'),
                                                                               (33, 'POWER_ON', 25.0, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (34, 'POWER_OFF', 15.0, '2025-04-05 16:48:15'),
                                                                               (34, 'POWER_ON', 15.0, '2025-04-05 18:17:15'),
                                                                               (34, 'POWER_OFF', 15.0, '2025-04-05 19:27:15'),
                                                                               (34, 'POWER_ON', 15.0, '2025-04-05 21:58:15'),
                                                                               (34, 'POWER_OFF', 15.0, '2025-04-06 01:52:15'),
                                                                               (34, 'POWER_ON', 15.0, '2025-04-06 06:34:15'),
                                                                               (34, 'POWER_OFF', 15.0, '2025-04-06 11:26:15'),
                                                                               (34, 'POWER_ON', 15.0, '2025-04-06 18:57:15'),
                                                                               (34, 'POWER_OFF', 15.0, '2025-04-06 21:50:15'),
                                                                               (34, 'POWER_ON', 15.0, '2025-04-07 07:13:15'),
                                                                               (34, 'POWER_OFF', 15.0, '2025-04-07 12:48:15'),
                                                                               (34, 'POWER_ON', 15.0, '2025-04-07 16:24:15'),
                                                                               (34, 'POWER_OFF', 15.0, '2025-04-07 22:51:15'),
                                                                               (34, 'POWER_ON', 15.0, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (35, 'POWER_OFF', 15.0, '2025-04-05 14:13:15'),
                                                                               (35, 'POWER_ON', 15.0, '2025-04-05 18:30:15'),
                                                                               (35, 'POWER_OFF', 15.0, '2025-04-06 02:47:15'),
                                                                               (35, 'POWER_ON', 15.0, '2025-04-06 03:21:15'),
                                                                               (35, 'POWER_OFF', 15.0, '2025-04-06 04:27:15'),
                                                                               (35, 'POWER_ON', 15.0, '2025-04-06 13:28:15'),
                                                                               (35, 'POWER_OFF', 15.0, '2025-04-06 20:00:15'),
                                                                               (35, 'POWER_ON', 15.0, '2025-04-06 21:31:15'),
                                                                               (35, 'POWER_OFF', 15.0, '2025-04-07 05:44:15'),
                                                                               (35, 'POWER_ON', 15.0, '2025-04-07 09:25:15'),
                                                                               (35, 'POWER_OFF', 15.0, '2025-04-07 14:57:15'),
                                                                               (35, 'POWER_ON', 15.0, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (36, 'POWER_OFF', 0.02, '2025-04-06 09:48:15'),
                                                                               (36, 'POWER_ON', 0.02, '2025-04-06 13:44:15'),
                                                                               (36, 'POWER_OFF', 0.02, '2025-04-06 20:47:15'),
                                                                               (36, 'POWER_ON', 0.02, '2025-04-06 21:35:15'),
                                                                               (36, 'POWER_OFF', 0.02, '2025-04-07 00:33:15'),
                                                                               (36, 'POWER_ON', 0.02, '2025-04-07 07:27:15'),
                                                                               (36, 'POWER_OFF', 0.02, '2025-04-07 11:57:15'),
                                                                               (36, 'POWER_ON', 0.02, '2025-04-07 17:36:15'),
                                                                               (36, 'POWER_OFF', 0.02, '2025-04-07 19:38:15'),
                                                                               (36, 'POWER_ON', 0.02, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (37, 'POWER_ON', 0.6, '2025-04-06 08:34:15'),
                                                                               (37, 'POWER_OFF', 0.6, '2025-04-06 16:12:15'),
                                                                               (37, 'POWER_ON', 0.6, '2025-04-06 21:44:15'),
                                                                               (37, 'POWER_OFF', 0.6, '2025-04-07 07:31:15'),
                                                                               (37, 'POWER_ON', 0.6, '2025-04-07 13:54:15'),
                                                                               (37, 'POWER_OFF', 0.6, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (38, 'POWER_ON', 0.4, '2025-04-06 15:59:15'),
                                                                               (38, 'POWER_OFF', 0.4, '2025-04-06 16:46:15'),
                                                                               (38, 'POWER_ON', 0.4, '2025-04-06 20:49:15'),
                                                                               (38, 'POWER_OFF', 0.4, '2025-04-07 02:31:15'),
                                                                               (38, 'POWER_ON', 0.4, '2025-04-07 11:56:15'),
                                                                               (38, 'POWER_OFF', 0.4, '2025-04-07 19:17:15'),
                                                                               (38, 'POWER_ON', 0.4, '2025-04-07 20:59:15'),
                                                                               (38, 'POWER_OFF', 0.4, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (39, 'POWER_OFF', 0.3, '2025-04-06 08:19:15'),
                                                                               (39, 'POWER_ON', 0.3, '2025-04-06 12:18:15'),
                                                                               (39, 'POWER_OFF', 0.3, '2025-04-06 15:50:15'),
                                                                               (39, 'POWER_ON', 0.3, '2025-04-06 20:31:15'),
                                                                               (39, 'POWER_OFF', 0.3, '2025-04-07 00:33:15'),
                                                                               (39, 'POWER_ON', 0.3, '2025-04-07 03:21:15'),
                                                                               (39, 'POWER_OFF', 0.3, '2025-04-07 04:49:15'),
                                                                               (39, 'POWER_ON', 0.3, '2025-04-07 09:28:15'),
                                                                               (39, 'POWER_OFF', 0.3, '2025-04-07 18:44:15'),
                                                                               (39, 'POWER_ON', 0.3, '2025-04-07 20:57:15'),
                                                                               (39, 'POWER_OFF', 0.3, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (40, 'POWER_ON', 0.4, '2025-04-06 11:31:15'),
                                                                               (40, 'POWER_OFF', 0.4, '2025-04-06 16:29:15'),
                                                                               (40, 'POWER_ON', 0.4, '2025-04-07 02:10:15'),
                                                                               (40, 'POWER_OFF', 0.4, '2025-04-07 10:33:15'),
                                                                               (40, 'POWER_ON', 0.4, '2025-04-07 18:15:15'),
                                                                               (40, 'POWER_OFF', 0.4, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (41, 'POWER_ON', 1.5, '2025-04-06 21:09:15'),
                                                                               (41, 'POWER_OFF', 1.5, '2025-04-07 06:17:15'),
                                                                               (41, 'POWER_ON', 1.5, '2025-04-07 16:09:15'),
                                                                               (41, 'POWER_OFF', 1.5, '2025-04-07 20:50:15'),
                                                                               (41, 'POWER_ON', 1.5, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (42, 'POWER_OFF', 0.5, '2025-04-05 20:44:15'),
                                                                               (42, 'POWER_ON', 0.5, '2025-04-06 04:45:15'),
                                                                               (42, 'POWER_OFF', 0.5, '2025-04-06 05:15:15'),
                                                                               (42, 'POWER_ON', 0.5, '2025-04-06 10:29:15'),
                                                                               (42, 'POWER_OFF', 0.5, '2025-04-06 17:37:15'),
                                                                               (42, 'POWER_ON', 0.5, '2025-04-06 18:42:15'),
                                                                               (42, 'POWER_OFF', 0.5, '2025-04-07 04:10:15'),
                                                                               (42, 'POWER_ON', 0.5, '2025-04-07 07:12:15'),
                                                                               (42, 'POWER_OFF', 0.5, '2025-04-07 13:25:15'),
                                                                               (42, 'POWER_ON', 0.5, '2025-04-07 16:45:15'),
                                                                               (42, 'POWER_OFF', 0.5, '2025-04-07 18:43:15'),
                                                                               (42, 'POWER_ON', 0.5, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (43, 'POWER_OFF', 0.8, '2025-04-06 11:46:15'),
                                                                               (43, 'POWER_ON', 0.8, '2025-04-06 18:43:15'),
                                                                               (43, 'POWER_OFF', 0.8, '2025-04-07 03:55:15'),
                                                                               (43, 'POWER_ON', 0.8, '2025-04-07 08:15:15'),
                                                                               (43, 'POWER_OFF', 0.8, '2025-04-07 14:34:15'),
                                                                               (43, 'POWER_ON', 0.8, '2025-04-07 15:06:15'),
                                                                               (43, 'POWER_OFF', 0.8, '2025-04-07 16:16:15'),
                                                                               (43, 'POWER_ON', 0.8, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (44, 'POWER_ON', 5.0, '2025-04-05 19:44:15'),
                                                                               (44, 'POWER_OFF', 5.0, '2025-04-06 02:23:15'),
                                                                               (44, 'POWER_ON', 5.0, '2025-04-06 09:24:15'),
                                                                               (44, 'POWER_OFF', 5.0, '2025-04-06 10:08:15'),
                                                                               (44, 'POWER_ON', 5.0, '2025-04-06 18:06:15'),
                                                                               (44, 'POWER_OFF', 5.0, '2025-04-06 22:24:15'),
                                                                               (44, 'POWER_ON', 5.0, '2025-04-07 04:39:15'),
                                                                               (44, 'POWER_OFF', 5.0, '2025-04-07 13:10:15'),
                                                                               (44, 'POWER_ON', 5.0, '2025-04-07 22:15:15'),
                                                                               (44, 'POWER_OFF', 5.0, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (45, 'POWER_ON', 0.02, '2025-04-06 19:14:15'),
                                                                               (45, 'POWER_OFF', 0.02, '2025-04-07 05:04:15'),
                                                                               (45, 'POWER_ON', 0.02, '2025-04-07 11:03:15'),
                                                                               (45, 'POWER_OFF', 0.02, '2025-04-07 16:23:15'),
                                                                               (45, 'POWER_ON', 0.02, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (46, 'POWER_OFF', 0.5, '2025-04-06 06:35:15'),
                                                                               (46, 'POWER_ON', 0.5, '2025-04-06 06:56:15'),
                                                                               (46, 'POWER_OFF', 0.5, '2025-04-06 14:47:15'),
                                                                               (46, 'POWER_ON', 0.5, '2025-04-07 00:11:15'),
                                                                               (46, 'POWER_OFF', 0.5, '2025-04-07 07:06:15'),
                                                                               (46, 'POWER_ON', 0.5, '2025-04-07 13:01:15'),
                                                                               (46, 'POWER_OFF', 0.5, '2025-04-07 16:28:15'),
                                                                               (46, 'POWER_ON', 0.5, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (47, 'POWER_ON', 12.0, '2025-04-07 02:59:15'),
                                                                               (47, 'POWER_OFF', 12.0, '2025-04-07 11:07:15'),
                                                                               (47, 'POWER_ON', 12.0, '2025-04-07 15:58:15'),
                                                                               (47, 'POWER_OFF', 12.0, '2025-04-07 19:02:15'),
                                                                               (47, 'POWER_ON', 12.0, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (48, 'POWER_ON', 0.1, '2025-04-05 08:37:15'),
                                                                               (48, 'POWER_OFF', 0.1, '2025-04-05 18:07:15'),
                                                                               (48, 'POWER_ON', 0.1, '2025-04-05 21:51:15'),
                                                                               (48, 'POWER_OFF', 0.1, '2025-04-06 04:16:15'),
                                                                               (48, 'POWER_ON', 0.1, '2025-04-06 04:27:15'),
                                                                               (48, 'POWER_OFF', 0.1, '2025-04-06 13:42:15'),
                                                                               (48, 'POWER_ON', 0.1, '2025-04-06 23:34:15'),
                                                                               (48, 'POWER_OFF', 0.1, '2025-04-07 06:16:15'),
                                                                               (48, 'POWER_ON', 0.1, '2025-04-07 11:21:15'),
                                                                               (48, 'POWER_OFF', 0.1, '2025-04-07 14:45:15'),
                                                                               (48, 'POWER_ON', 0.1, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (49, 'POWER_ON', 20.0, '2025-04-04 19:49:15'),
                                                                               (49, 'POWER_OFF', 20.0, '2025-04-05 01:32:15'),
                                                                               (49, 'POWER_ON', 20.0, '2025-04-05 09:12:15'),
                                                                               (49, 'POWER_OFF', 20.0, '2025-04-05 09:39:15'),
                                                                               (49, 'POWER_ON', 20.0, '2025-04-05 18:25:15'),
                                                                               (49, 'POWER_OFF', 20.0, '2025-04-06 03:06:15'),
                                                                               (49, 'POWER_ON', 20.0, '2025-04-06 03:53:15'),
                                                                               (49, 'POWER_OFF', 20.0, '2025-04-06 10:27:15'),
                                                                               (49, 'POWER_ON', 20.0, '2025-04-06 13:04:15'),
                                                                               (49, 'POWER_OFF', 20.0, '2025-04-06 19:39:15'),
                                                                               (49, 'POWER_ON', 20.0, '2025-04-07 00:03:15'),
                                                                               (49, 'POWER_OFF', 20.0, '2025-04-07 03:08:15'),
                                                                               (49, 'POWER_ON', 20.0, '2025-04-07 08:52:15'),
                                                                               (49, 'POWER_OFF', 20.0, '2025-04-07 17:55:15'),
                                                                               (49, 'POWER_ON', 20.0, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (50, 'POWER_ON', 4.5, '2025-04-06 09:26:15'),
                                                                               (50, 'POWER_OFF', 4.5, '2025-04-06 09:49:15'),
                                                                               (50, 'POWER_ON', 4.5, '2025-04-06 13:26:15'),
                                                                               (50, 'POWER_OFF', 4.5, '2025-04-06 14:43:15'),
                                                                               (50, 'POWER_ON', 4.5, '2025-04-06 18:46:15'),
                                                                               (50, 'POWER_OFF', 4.5, '2025-04-07 02:13:15'),
                                                                               (50, 'POWER_ON', 4.5, '2025-04-07 09:25:15'),
                                                                               (50, 'POWER_OFF', 4.5, '2025-04-07 10:45:15'),
                                                                               (50, 'POWER_ON', 4.5, '2025-04-07 11:05:15'),
                                                                               (50, 'POWER_OFF', 4.5, '2025-04-07 17:06:15'),
                                                                               (50, 'POWER_ON', 4.5, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (51, 'POWER_OFF', 1.0, '2025-04-06 05:33:15'),
                                                                               (51, 'POWER_ON', 1.0, '2025-04-06 06:58:15'),
                                                                               (51, 'POWER_OFF', 1.0, '2025-04-06 14:11:15'),
                                                                               (51, 'POWER_ON', 1.0, '2025-04-06 14:54:15'),
                                                                               (51, 'POWER_OFF', 1.0, '2025-04-06 20:58:15'),
                                                                               (51, 'POWER_ON', 1.0, '2025-04-07 03:50:15'),
                                                                               (51, 'POWER_OFF', 1.0, '2025-04-07 09:06:15'),
                                                                               (51, 'POWER_ON', 1.0, '2025-04-07 11:40:15'),
                                                                               (51, 'POWER_OFF', 1.0, '2025-04-07 14:54:15'),
                                                                               (51, 'POWER_ON', 1.0, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (52, 'POWER_ON', 0.5, '2025-04-03 22:17:15'),
                                                                               (52, 'POWER_OFF', 0.5, '2025-04-04 07:40:15'),
                                                                               (52, 'POWER_ON', 0.5, '2025-04-04 15:31:15'),
                                                                               (52, 'POWER_OFF', 0.5, '2025-04-04 17:15:15'),
                                                                               (52, 'POWER_ON', 0.5, '2025-04-04 20:22:15'),
                                                                               (52, 'POWER_OFF', 0.5, '2025-04-05 04:47:15'),
                                                                               (52, 'POWER_ON', 0.5, '2025-04-05 13:00:15'),
                                                                               (52, 'POWER_OFF', 0.5, '2025-04-05 22:27:15'),
                                                                               (52, 'POWER_ON', 0.5, '2025-04-06 03:20:15'),
                                                                               (52, 'POWER_OFF', 0.5, '2025-04-06 10:34:15'),
                                                                               (52, 'POWER_ON', 0.5, '2025-04-06 16:18:15'),
                                                                               (52, 'POWER_OFF', 0.5, '2025-04-07 02:16:15'),
                                                                               (52, 'POWER_ON', 0.5, '2025-04-07 10:41:15'),
                                                                               (52, 'POWER_OFF', 0.5, '2025-04-07 16:20:15'),
                                                                               (52, 'POWER_ON', 0.5, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (53, 'POWER_ON', 9.0, '2025-04-07 01:01:15'),
                                                                               (53, 'POWER_OFF', 9.0, '2025-04-07 08:19:15'),
                                                                               (53, 'POWER_ON', 9.0, '2025-04-07 12:31:15'),
                                                                               (53, 'POWER_OFF', 9.0, '2025-04-07 18:15:15'),
                                                                               (53, 'POWER_ON', 9.0, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (54, 'POWER_OFF', 0.02, '2025-04-06 05:01:15'),
                                                                               (54, 'POWER_ON', 0.02, '2025-04-06 06:45:15'),
                                                                               (54, 'POWER_OFF', 0.02, '2025-04-06 16:04:15'),
                                                                               (54, 'POWER_ON', 0.02, '2025-04-06 16:12:15'),
                                                                               (54, 'POWER_OFF', 0.02, '2025-04-07 00:58:15'),
                                                                               (54, 'POWER_ON', 0.02, '2025-04-07 10:26:15'),
                                                                               (54, 'POWER_OFF', 0.02, '2025-04-07 16:48:15'),
                                                                               (54, 'POWER_ON', 0.02, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (55, 'POWER_ON', 6.0, '2025-04-07 11:42:15'),
                                                                               (55, 'POWER_OFF', 6.0, '2025-04-07 13:15:15'),
                                                                               (55, 'POWER_ON', 6.0, '2025-04-07 14:51:15'),
                                                                               (55, 'POWER_OFF', 6.0, '2025-04-07 21:14:15'),
                                                                               (55, 'POWER_ON', 6.0, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (56, 'POWER_ON', 0.2, '2025-04-06 05:19:15'),
                                                                               (56, 'POWER_OFF', 0.2, '2025-04-06 08:19:15'),
                                                                               (56, 'POWER_ON', 0.2, '2025-04-06 15:07:15'),
                                                                               (56, 'POWER_OFF', 0.2, '2025-04-06 19:32:15'),
                                                                               (56, 'POWER_ON', 0.2, '2025-04-06 21:08:15'),
                                                                               (56, 'POWER_OFF', 0.2, '2025-04-06 22:28:15'),
                                                                               (56, 'POWER_ON', 0.2, '2025-04-06 23:49:15'),
                                                                               (56, 'POWER_OFF', 0.2, '2025-04-07 07:44:15'),
                                                                               (56, 'POWER_ON', 0.2, '2025-04-07 10:01:15'),
                                                                               (56, 'POWER_OFF', 0.2, '2025-04-07 18:46:15'),
                                                                               (56, 'POWER_ON', 0.2, '2025-04-07 22:02:15'),
                                                                               (56, 'POWER_OFF', 0.2, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (57, 'POWER_OFF', 0.2, '2025-04-06 07:44:15'),
                                                                               (57, 'POWER_ON', 0.2, '2025-04-06 08:05:15'),
                                                                               (57, 'POWER_OFF', 0.2, '2025-04-06 17:03:15'),
                                                                               (57, 'POWER_ON', 0.2, '2025-04-07 01:58:15'),
                                                                               (57, 'POWER_OFF', 0.2, '2025-04-07 06:41:15'),
                                                                               (57, 'POWER_ON', 0.2, '2025-04-07 11:58:15'),
                                                                               (57, 'POWER_OFF', 0.2, '2025-04-07 18:52:15'),
                                                                               (57, 'POWER_ON', 0.2, '2025-04-07 21:20:15'),
                                                                               (57, 'POWER_OFF', 0.2, '2025-04-07 22:16:15'),
                                                                               (57, 'POWER_ON', 0.2, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (58, 'POWER_ON', 0.5, '2025-04-06 22:12:15'),
                                                                               (58, 'POWER_OFF', 0.5, '2025-04-07 07:56:15'),
                                                                               (58, 'POWER_ON', 0.5, '2025-04-07 15:42:15'),
                                                                               (58, 'POWER_OFF', 0.5, '2025-04-07 22:29:15'),
                                                                               (58, 'POWER_ON', 0.5, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (59, 'POWER_OFF', 18.0, '2025-04-06 11:24:15'),
                                                                               (59, 'POWER_ON', 18.0, '2025-04-06 12:52:15'),
                                                                               (59, 'POWER_OFF', 18.0, '2025-04-06 22:47:15'),
                                                                               (59, 'POWER_ON', 18.0, '2025-04-07 04:14:15'),
                                                                               (59, 'POWER_OFF', 18.0, '2025-04-07 08:42:15'),
                                                                               (59, 'POWER_ON', 18.0, '2025-04-07 09:13:15'),
                                                                               (59, 'POWER_OFF', 18.0, '2025-04-07 09:54:15'),
                                                                               (59, 'POWER_ON', 18.0, '2025-04-07 13:55:15'),
                                                                               (59, 'POWER_OFF', 18.0, '2025-04-07 15:42:15'),
                                                                               (59, 'POWER_ON', 18.0, '2025-04-07 18:03:15'),
                                                                               (59, 'POWER_OFF', 18.0, '2025-04-07 18:50:15'),
                                                                               (59, 'POWER_ON', 18.0, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (60, 'POWER_ON', 0.02, '2025-04-06 09:50:15'),
                                                                               (60, 'POWER_OFF', 0.02, '2025-04-06 09:59:15'),
                                                                               (60, 'POWER_ON', 0.02, '2025-04-06 12:49:15'),
                                                                               (60, 'POWER_OFF', 0.02, '2025-04-06 18:07:15'),
                                                                               (60, 'POWER_ON', 0.02, '2025-04-06 18:51:15'),
                                                                               (60, 'POWER_OFF', 0.02, '2025-04-06 21:39:15'),
                                                                               (60, 'POWER_ON', 0.02, '2025-04-07 01:13:15'),
                                                                               (60, 'POWER_OFF', 0.02, '2025-04-07 02:48:15'),
                                                                               (60, 'POWER_ON', 0.02, '2025-04-07 05:42:15'),
                                                                               (60, 'POWER_OFF', 0.02, '2025-04-07 15:33:15'),
                                                                               (60, 'POWER_ON', 0.02, '2025-04-07 20:31:15'),
                                                                               (60, 'POWER_OFF', 0.02, '2025-04-07 23:18:15'),
                                                                               (60, 'POWER_ON', 0.02, '2025-04-07 23:54:15');

INSERT INTO PowerLog (`deviceId`, `status`, `energyConsumption`, `time`) VALUES
                                                                               (61, 'POWER_ON', 0.05, '2025-04-05 07:46:15'),
                                                                               (61, 'POWER_OFF', 0.05, '2025-04-05 12:16:15'),
                                                                               (61, 'POWER_ON', 0.05, '2025-04-05 16:09:15'),
                                                                               (61, 'POWER_OFF', 0.05, '2025-04-05 23:47:15'),
                                                                               (61, 'POWER_ON', 0.05, '2025-04-05 23:57:15'),
                                                                               (61, 'POWER_OFF', 0.05, '2025-04-06 02:13:15'),
                                                                               (61, 'POWER_ON', 0.05, '2025-04-06 09:16:15'),
                                                                               (61, 'POWER_OFF', 0.05, '2025-04-06 14:20:15'),
                                                                               (61, 'POWER_ON', 0.05, '2025-04-06 21:40:15'),
                                                                               (61, 'POWER_OFF', 0.05, '2025-04-07 01:35:15'),
                                                                               (61, 'POWER_ON', 0.05, '2025-04-07 02:07:15'),
                                                                               (61, 'POWER_OFF', 0.05, '2025-04-07 07:03:15'),
                                                                               (61, 'POWER_ON', 0.05, '2025-04-07 15:29:15'),
                                                                               (61, 'POWER_OFF', 0.05, '2025-04-07 20:00:15'),
                                                                               (61, 'POWER_ON', 0.05, '2025-04-07 23:54:15');