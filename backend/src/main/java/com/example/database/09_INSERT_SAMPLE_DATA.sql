-- ================================================
-- ORNEK VERI EKLEME SCRIPTI
-- Tum tablolar icin kapsamli test verileri
-- ================================================

USE CarRentalDB;
GO

-- ================================================
-- 1. ULKELER VE SEHIRLER
-- ================================================
INSERT INTO Countries (CountryName, CountryCode) VALUES
('Turkiye', 'TR'),
('Amerika Birlesik Devletleri', 'US'),
('Almanya', 'DE'),
('Fransa', 'FR'),
('Birlesik Krallik', 'GB');

INSERT INTO Cities (CityName, CountryID, PostalCode) VALUES
('Istanbul', 1, '34000'),
('Ankara', 1, '06000'),
('Izmir', 1, '35000'),
('Antalya', 1, '07000'),
('New York', 2, '10001'),
('Los Angeles', 2, '90001'),
('Berlin', 3, '10115'),
('Munih', 3, '80331'),
('Paris', 4, '75001'),
('Londra', 5, 'SW1A');

-- ================================================
-- 2. KULLANICI ROLLERI
-- ================================================
INSERT INTO UserRoles (RoleName, RoleDescription, Permissions) VALUES
('Yonetici', 'Sistem Yoneticisi', '{"all": true}'),
('Mudur', 'Lokasyon Muduru', '{"users": "read", "rentals": "all", "vehicles": "all", "reports": "read"}'),
('Calisan', 'Musteri Hizmetleri Calisani', '{"rentals": "create_update", "customers": "read", "vehicles": "read"}'),
('Musteri', 'Kiralama Musterisi', '{"profile": "update", "rentals": "read_own"}');

-- ================================================
-- 3. KULLANICILAR (YONETICI, MUDURLER, CALISANLAR DAHIL)
-- ================================================
INSERT INTO Users (Username, Email, PasswordHash, FirstName, LastName, PhoneNumber, DateOfBirth, RoleID, CityID, Address, CreatedBy) VALUES
-- Yonetici
('admin', 'admin@arackiralamauygulamasi.com', 'hashed_password_admin', 'Sistem', 'Yoneticisi', '+90-212-555-0001', '1980-01-01', 1, 1, 'Yonetim Ofisi', NULL),

-- Mudurler
('mudur1', 'mudur1@arackiralamauygulamasi.com', 'hashed_password_mudur1', 'Mehmet', 'Yilmaz', '+90-212-555-0002', '1975-05-15', 2, 1, 'Istanbul Merkez', 1),
('mudur2', 'mudur2@arackiralamauygulamasi.com', 'hashed_password_mudur2', 'Ayse', 'Demir', '+90-312-555-0003', '1978-08-22', 2, 2, 'Ankara Merkez', 1),
('mudur3', 'mudur3@arackiralamauygulamasi.com', 'hashed_password_mudur3', 'Ali', 'Kaya', '+90-232-555-0004', '1982-12-10', 2, 3, 'Izmir Merkez', 1),

-- Calisanlar
('calisan1', 'calisan1@arackiralamauygulamasi.com', 'hashed_password_cal1', 'Fatma', 'Ozkan', '+90-212-555-0005', '1990-03-18', 3, 1, 'Besiktas', 1),
('calisan2', 'calisan2@arackiralamauygulamasi.com', 'hashed_password_cal2', 'Mustafa', 'Celik', '+90-312-555-0006', '1988-07-25', 3, 2, 'Cankaya', 1),
('calisan3', 'calisan3@arackiralamauygulamasi.com', 'hashed_password_cal3', 'Zeynep', 'Arslan', '+90-232-555-0007', '1992-11-08', 3, 3, 'Karsiyaka', 1),

-- Musteriler
('berkekamisoglu', 'berkekamisoglu@hotmail.com', 'Berke0204', 'Berke', 'Kamisoglu', '+90-555-123-4567', '1995-01-01', 4, 1, 'Istanbul', 1),
('musteri1', 'ahmet.yilmaz@email.com', 'hashed_password_mus1', 'Ahmet', 'Yilmaz', '+90-532-555-0101', '1985-04-12', 4, 1, 'Kadikoy Mah. No:15', 1),
('musteri2', 'elif.demir@email.com', 'hashed_password_mus2', 'Elif', 'Demir', '+90-533-555-0102', '1990-09-28', 4, 1, 'Sisli Mah. No:22', 1),
('musteri3', 'burak.kaya@email.com', 'hashed_password_mus3', 'Burak', 'Kaya', '+90-534-555-0103', '1987-02-14', 4, 2, 'Kizilay Mah. No:8', 1),
('musteri4', 'seda.ozkan@email.com', 'hashed_password_mus4', 'Seda', 'Ozkan', '+90-535-555-0104', '1993-06-05', 4, 2, 'Bahcelievler Mah. No:33', 1),
('musteri5', 'emre.celik@email.com', 'hashed_password_mus5', 'Emre', 'Celik', '+90-536-555-0105', '1991-11-17', 4, 3, 'Alsancak Mah. No:45', 1),
('musteri6', 'aylin.arslan@email.com', 'hashed_password_mus6', 'Aylin', 'Arslan', '+90-537-555-0106', '1989-08-30', 4, 3, 'Bornova Mah. No:12', 1),
('musteri7', 'john.smith@email.com', 'hashed_password_mus7', 'John', 'Smith', '+1-212-555-0201', '1985-03-20', 4, 5, '123 Broadway Cad', 1),
('musteri8', 'maria.garcia@email.com', 'hashed_password_mus8', 'Maria', 'Garcia', '+1-213-555-0202', '1988-12-15', 4, 6, '456 Sunset Bulvari', 1),
('musteri9', 'hans.mueller@email.com', 'hashed_password_mus9', 'Hans', 'Mueller', '+49-30-555-0301', '1982-07-08', 4, 7, 'Unter den Linden 1', 1),
('musteri10', 'sophie.martin@email.com', 'hashed_password_mus10', 'Sophie', 'Martin', '+33-1-555-0401', '1995-01-25', 4, 9, '1 Avenue des Champs', 1);

-- ================================================
-- 4. ARAC KATEGORILERI
-- ================================================
INSERT INTO VehicleCategories (CategoryName, CategoryDescription, BasePrice) VALUES
('Ekonomik', 'Yakit tasarruflu butce dostu araclar', 25.00),
('Kompakt', 'Sehir ici surus icin ideal kucuk araclar', 35.00),
('Orta Sinif', 'Uzun yolculuklar icin konforlu araclar', 45.00),
('Tam Boy', 'Aileler ve gruplar icin genis araclar', 55.00),
('Luks', 'Gelismis ozelliklere sahip premium araclar', 85.00),
('SUV', 'Her turlu arazide kullanilabilen spor araclar', 65.00),
('Van', 'Gruplar ve yuk tasimaciligi icin buyuk araclar', 75.00),
('Cabrio', 'Ozel gunler icin ustu acik araclar', 95.00);

-- ================================================
-- 5. ARAC MARKALARI
-- ================================================
INSERT INTO VehicleBrands (BrandName, BrandCountry, Website) VALUES
('Toyota', 'Japonya', 'www.toyota.com'),
('Volkswagen', 'Almanya', 'www.volkswagen.com'),
('Ford', 'ABD', 'www.ford.com'),
('BMW', 'Almanya', 'www.bmw.com'),
('Mercedes-Benz', 'Almanya', 'www.mercedes-benz.com'),
('Audi', 'Almanya', 'www.audi.com'),
('Nissan', 'Japonya', 'www.nissan.com'),
('Hyundai', 'Guney Kore', 'www.hyundai.com'),
('Kia', 'Guney Kore', 'www.kia.com'),
('Chevrolet', 'ABD', 'www.chevrolet.com'),
('Honda', 'Japonya', 'www.honda.com'),
('Peugeot', 'Fransa', 'www.peugeot.com');

-- ================================================
-- 6. ARAC MODELLERI
-- ================================================
INSERT INTO VehicleModels (ModelName, BrandID, CategoryID, ManufactureYear, EngineType, FuelType, TransmissionType, SeatingCapacity) VALUES
-- Toyota Modelleri
('Corolla', 1, 2, 2023, '1.6L I4', 'Benzin', 'Otomatik', 5),
('Camry', 1, 3, 2023, '2.5L I4', 'Hibrit', 'Otomatik', 5),
('RAV4', 1, 6, 2023, '2.5L I4', 'Hibrit', '4x4', 5),
('Prius', 1, 1, 2023, '1.8L I4', 'Hibrit', 'CVT', 5),

-- Volkswagen Modelleri
('Golf', 2, 2, 2023, '1.4L TSI', 'Benzin', 'Manuel', 5),
('Passat', 2, 3, 2023, '2.0L TSI', 'Benzin', 'Otomatik', 5),
('Tiguan', 2, 6, 2023, '2.0L TSI', 'Benzin', '4x4', 5),

-- Ford Modelleri
('Focus', 3, 2, 2023, '1.5L EcoBoost', 'Benzin', 'Otomatik', 5),
('Mustang', 3, 8, 2023, '5.0L V8', 'Benzin', 'Manuel', 4),
('Explorer', 3, 6, 2023, '3.0L V6', 'Benzin', '4x4', 7),

-- BMW Modelleri
('3 Serisi', 4, 5, 2023, '2.0L Turbo', 'Benzin', 'Otomatik', 5),
('X3', 4, 6, 2023, '3.0L Turbo', 'Benzin', '4x4', 5),
('5 Serisi', 4, 5, 2023, '3.0L Turbo', 'Benzin', 'Otomatik', 5),

-- Mercedes-Benz Modelleri
('C-Serisi', 5, 5, 2023, '2.0L Turbo', 'Benzin', 'Otomatik', 5),
('E-Serisi', 5, 5, 2023, '3.0L V6', 'Benzin', 'Otomatik', 5),
('GLE', 5, 6, 2023, '3.0L V6', 'Benzin', '4x4', 5),

-- Cesitlilik icin daha fazla model
('A4', 6, 5, 2023, '2.0L TFSI', 'Benzin', 'Otomatik', 5),
('Sentra', 7, 2, 2023, '1.6L I4', 'Benzin', 'CVT', 5),
('Elantra', 8, 2, 2023, '2.0L I4', 'Benzin', 'Otomatik', 5),
('Forte', 9, 2, 2023, '2.0L I4', 'Benzin', 'CVT', 5),
('Malibu', 10, 3, 2023, '1.5L Turbo', 'Benzin', 'Otomatik', 5),
('Accord', 11, 3, 2023, '1.5L Turbo', 'Benzin', 'CVT', 5);

-- ================================================
-- 7. ARAC DURUMLARI
-- ================================================
INSERT INTO VehicleStatus (StatusName, StatusDescription, IsAvailableForRent) VALUES
('Musait', 'Arac kiralamaya hazir', 1),
('Rezerve', 'Arac rezerve edildi ancak henuz teslim alinmadi', 0),
('Kiralandi', 'Arac su anda kirada', 0),
('Bakimda', 'Arac bakimda', 0),
('Hasarli', 'Arac hasarli ve tamir edilmesi gerekiyor', 0),
('Kontrol Gerekli', 'Sonraki kiralamadan once kontrol edilmeli', 0),
('Bakim Planlandi', 'Yaklasan bakimi var', 1),
('Hizmet Disi', 'Arac kalici olarak hizmet disi', 0);

-- ================================================
-- 8. LOKASYONLAR
-- ================================================
INSERT INTO Locations (LocationName, CityID, Address, PhoneNumber, Email, ManagerID) VALUES
('Istanbul Ataturk Havalimani', 1, 'Ataturk Havalimani Dis Hatlar Terminal', '+90-212-555-1001', 'ataturk@arackiralamauygulamasi.com', 2),
('Istanbul Sabiha Gokcen Havalimani', 1, 'Sabiha Gokcen Havalimani Terminal', '+90-216-555-1002', 'sabiha@arackiralamauygulamasi.com', 2),
('Istanbul Sehir Merkezi', 1, 'Taksim Meydani No:1', '+90-212-555-1003', 'taksim@arackiralamauygulamasi.com', 2),
('Ankara Esenboga Havalimani', 2, 'Esenboga Havalimani Terminal', '+90-312-555-2001', 'esenboga@arackiralamauygulamasi.com', 3),
('Ankara Sehir Merkezi', 2, 'Kizilay Meydani No:1', '+90-312-555-2002', 'kizilay@arackiralamauygulamasi.com', 3),
('Izmir Adnan Menderes Havalimani', 3, 'Adnan Menderes Havalimani Terminal', '+90-232-555-3001', 'adnanmenderes@arackiralamauygulamasi.com', 4),
('Izmir Sehir Merkezi', 3, 'Konak Meydani No:1', '+90-232-555-3002', 'konak@arackiralamauygulamasi.com', 4),
('Antalya Havalimani', 4, 'Antalya Havalimani Terminal', '+90-242-555-4001', 'antalya@arackiralamauygulamasi.com', 2);

-- ================================================
-- 9. ARACLAR
-- ================================================
INSERT INTO Vehicles (VehicleRegistration, ModelID, Color, Mileage, PurchaseDate, PurchasePrice, CurrentStatusID, CurrentLocationID, DailyRentalRate, InsurancePolicyNumber, NextMaintenanceDate, VehicleDescription, CreatedBy) VALUES
-- Ekonomik araclar
('34ABC123', 4, 'Beyaz', 15000, '2023-01-15', 22000.00, 1, 1, 30.00, 'SIG-2023-001', '2024-07-15', 'Sehir ici surus icin mukemmel yakit tasarruflu hibrit arac', 1),
('34ABC124', 1, 'Gumus', 12000, '2023-02-20', 25000.00, 1, 2, 32.00, 'SIG-2023-002', '2024-08-20', 'Mukemmel guvenlik derecelerine sahip guvenilir kompakt arac', 1),
('34ABC125', 18, 'Mavi', 8000, '2023-03-10', 24000.00, 1, 3, 31.00, 'SIG-2023-003', '2024-09-10', 'Gelismis teknoloji ozelliklerine sahip modern sedan', 1),

-- Kompakt araclar
('34DEF456', 5, 'Kirmizi', 20000, '2022-11-05', 28000.00, 1, 1, 38.00, 'SIG-2022-004', '2024-06-05', 'Mukemmel yol tutusu olan sportif hatchback', 1),
('34DEF457', 8, 'Siyah', 18000, '2023-01-25', 27000.00, 1, 4, 36.00, 'SIG-2023-005', '2024-07-25', 'Genis ic mekani olan konforlu aile araci', 1),

-- Orta sinif araclar
('06GHI789', 2, 'Beyaz', 25000, '2022-09-15', 32000.00, 1, 5, 48.00, 'SIG-2022-006', '2024-05-15', 'Olaganusta yakit ekonomisine sahip hibrit sedan', 1),
('06GHI790', 6, 'Gri', 22000, '2022-12-08', 35000.00, 1, 5, 52.00, 'SIG-2022-007', '2024-06-08', 'Premium konfor ozelliklerine sahip yonetici sedani', 1),
('35JKL012', 21, 'Mavi', 16000, '2023-02-14', 31000.00, 1, 6, 46.00, 'SIG-2023-008', '2024-08-14', 'Gelismis guvenlik sistemli guvenilir orta sinif sedan', 1),

-- Luks araclar
('34MNO345', 11, 'Siyah', 8000, '2023-04-20', 45000.00, 1, 1, 88.00, 'SIG-2023-009', '2024-10-20', 'Luks donanim ve performansa sahip premium BMW', 1),
('34MNO346', 14, 'Gumus', 12000, '2023-03-15', 52000.00, 1, 2, 95.00, 'SIG-2023-010', '2024-09-15', 'En yeni teknolojiye sahip Mercedes-Benz luks sedan', 1),
('06PQR678', 15, 'Beyaz', 6000, '2023-05-10', 58000.00, 1, 4, 105.00, 'SIG-2023-011', '2024-11-10', 'Premium ic mekana sahip yonetici sinifi Mercedes', 1),

-- SUV'lar
('34STU901', 3, 'Yesil', 14000, '2023-01-30', 38000.00, 1, 1, 68.00, 'SIG-2023-012', '2024-07-30', 'Her hava kosulunda mukemmel olan dort ceker SUV', 1),
('35VWX234', 7, 'Mavi', 18000, '2022-10-25', 36000.00, 1, 6, 65.00, 'SIG-2022-013', '2024-04-25', 'Mukemmel arazi kabiliyetine sahip kompakt SUV', 1),
('06YZA567', 10, 'Siyah', 20000, '2022-11-15', 42000.00, 1, 5, 72.00, 'SIG-2022-014', '2024-05-15', 'Yedi kisilik oturma kapasiteli buyuk SUV', 1),
('35BCD890', 12, 'Beyaz', 10000, '2023-03-20', 48000.00, 1, 7, 78.00, 'SIG-2023-015', '2024-09-20', 'Premium ozellikler ve teknolojiye sahip luks SUV', 1),
('07EFG123', 16, 'Gri', 15000, '2023-02-05', 55000.00, 1, 8, 85.00, 'SIG-2023-016', '2024-08-05', 'Gelismis surucu destek sistemlerine sahip premium SUV', 1),

-- Cabrio
('34HIJ456', 9, 'Kirmizi', 5000, '2023-06-01', 48000.00, 1, 3, 115.00, 'SIG-2023-017', '2024-12-01', 'Ozel gunler icin ikonik Mustang cabrio', 1),

-- Daha iyi envanter icin ek araclar
('34KLM789', 17, 'Gumus', 11000, '2023-04-15', 42000.00, 1, 1, 82.00, 'SIG-2023-018', '2024-10-15', 'Quattro dort ceker sistemli luks Audi sedan', 1),
('35NOP012', 19, 'Mavi', 13000, '2023-03-25', 26000.00, 1, 6, 34.00, 'SIG-2023-019', '2024-09-25', 'CVT sanzimmanli kompakt sedan', 1),
('06QRS345', 20, 'Beyaz', 9000, '2023-05-20', 29000.00, 1, 4, 40.00, 'SIG-2023-020', '2024-11-20', 'Mukemmel garantiye sahip Kore muhendisligi', 1);

-- ================================================
-- 10. KIRALAMA DURUMLARI
-- ================================================
INSERT INTO RentalStatus (StatusName, StatusDescription) VALUES
('Rezerve Edildi', 'Rezervasyon onaylandi, teslim alinmayi bekliyor'),
('Aktif', 'Arac teslim alindi ve kullanimda'),
('Gecikmis', 'Iade tarihi gecti, arac iade edilmedi'),
('Tamamlandi', 'Arac basariyla iade edildi'),
('Iptal Edildi', 'Rezervasyon iptal edildi'),
('Odendi', 'Kiralama tamamlandi ve odeme alindi');

-- ================================================
-- 11. ODEME YONTEMLERI
-- ================================================
INSERT INTO PaymentMethods (MethodName, MethodDescription, ProcessingFeePercentage) VALUES
('Kredi Karti', 'Visa, MasterCard, American Express', 0.0250),
('Banka Karti', 'Banka banka kartlari', 0.0150),
('Nakit', 'Nakit odeme', 0.0000),
('Banka Havalesi', 'Dogrudan banka havalesi', 0.0100),
('PayPal', 'PayPal cevrimici odeme', 0.0350),
('Apple Pay', 'Apple Pay mobil odeme', 0.0200),
('Google Pay', 'Google Pay mobil odeme', 0.0200);

-- ================================================
-- 12. BAKIM TURLERI
-- ================================================
INSERT INTO MaintenanceTypes (TypeName, TypeDescription, EstimatedDuration, AverageCost) VALUES
('Yag Degisimi', 'Motor yagi ve filtre degisimi', 1, 50.00),
('Lastik Rotasyonu', 'Esit asinma icin lastiklerin dondurulmesi', 1, 30.00),
('Fren Kontrolu', 'Fren balatasi ve sivi kontrolu', 2, 75.00),
('Genel Servis', 'Kapsamli arac muayenesi', 4, 200.00),
('Hava Filtresi Degisimi', 'Motor hava filtresi degisimi', 1, 25.00),
('Sanziman Servisi', 'Sanziman yagi degisimi', 2, 150.00),
('Aku Kontrolu', 'Aku ve sarj sistemi testi', 1, 40.00),
('Lastik Degisimi', 'Asinmis lastiklerin degistirilmesi', 2, 400.00),
('Yillik Muayene', 'Zorunlu yillik guvenlik muayenesi', 3, 100.00),
('Rot Balans Ayari', 'Tekerlek hizalama servisi', 2, 80.00);

-- ================================================
-- 13. HASAR TURLERI
-- ================================================
INSERT INTO DamageTypes (DamageTypeName, DamageDescription, AverageRepairCost) VALUES
('Cizik', 'Kucuk boya cizikleri', 150.00),
('Gocuk', 'Kaporta gocukleri', 300.00),
('Lastik Hasari', 'Lastik delinmesi veya asinmasi', 120.00),
('Ic Mekan Hasari', 'Koltuk veya ic mekan bileseni hasari', 250.00),
('Cam Hasari', 'Catlak veya kirik camlar', 200.00),
('Tampon Hasari', 'On veya arka tampon hasari', 400.00),
('Yan Ayna', 'Hasarli veya eksik yan ayna', 180.00),
('Far', 'Kirik far grubu', 350.00),
('Buyuk Kaza', 'Onemli kaza hasari', 2500.00),
('Motor Sorunlari', 'Motor mekanik problemleri', 1500.00);

PRINT 'Ornek veriler basariyla eklendi!';
PRINT 'Veritabani test ve gosterim icin hazir.';

-- ================================================
-- 14. ORNEK KIRALAMALAR OLUSTUR
-- ================================================

-- Mevcut aktif kiralamalar
INSERT INTO Rentals (CustomerID, VehicleID, PickupLocationID, ReturnLocationID, PlannedPickupDate, PlannedReturnDate, RentalStatusID, BaseAmount, TaxAmount, TotalAmount, SecurityDeposit, CreatedBy) VALUES
(8, 1, 1, 1, '2024-06-08', '2024-06-15', 2, 210.00, 37.80, 247.80, 42.00, 5),
(9, 5, 4, 5, '2024-06-09', '2024-06-12', 2, 108.00, 19.44, 127.44, 21.60, 6),
(10, 11, 1, 2, '2024-06-10', '2024-06-17', 1, 616.00, 110.88, 726.88, 123.20, 5);

-- Tamamlanmis kiralamalar (gecmis icin)
INSERT INTO Rentals (CustomerID, VehicleID, PickupLocationID, ReturnLocationID, PlannedPickupDate, PlannedReturnDate, ActualReturnDate, RentalStatusID, BaseAmount, TaxAmount, TotalAmount, SecurityDeposit, CreatedBy) VALUES
(8, 3, 1, 1, '2024-05-01', '2024-05-05', '2024-05-05', 4, 124.00, 22.32, 146.32, 24.80, 5),
(9, 7, 5, 5, '2024-05-15', '2024-05-18', '2024-05-18', 4, 156.00, 28.08, 184.08, 31.20, 6),
(11, 9, 1, 1, '2024-04-20', '2024-04-25', '2024-04-25', 4, 440.00, 79.20, 519.20, 88.00, 5),
(12, 15, 6, 7, '2024-05-10', '2024-05-14', '2024-05-14', 4, 312.00, 56.16, 368.16, 62.40, 7),
(13, 4, 4, 4, '2024-04-01', '2024-04-03', '2024-04-03', 4, 76.00, 13.68, 89.68, 15.20, 6);

-- ================================================
-- 15. ORNEK ODEMELER
-- ================================================
INSERT INTO Payments (RentalID, PaymentMethodID, PaymentAmount, TransactionReference, PaymentStatus, ProcessingFee, CreatedBy) VALUES
(4, 1, 146.32, 'KK-2024-001', 'Tamamlandi', 3.66, 5),
(5, 2, 184.08, 'BK-2024-002', 'Tamamlandi', 2.76, 6),
(6, 1, 519.20, 'KK-2024-003', 'Tamamlandi', 12.98, 5),
(7, 3, 368.16, 'NAKIT-2024-001', 'Tamamlandi', 0.00, 7),
(8, 1, 89.68, 'KK-2024-004', 'Tamamlandi', 2.24, 6),
(1, 1, 150.00, 'KK-2024-005', 'Tamamlandi', 3.75, 5),
(2, 2, 100.00, 'BK-2024-006', 'Tamamlandi', 1.50, 6);

-- ================================================
-- 16. ORNEK BAKIM KAYITLARI
-- ================================================
INSERT INTO VehicleMaintenance (VehicleID, MaintenanceTypeID, ScheduledDate, CompletedDate, Cost, ServiceProvider, Description, CreatedBy) VALUES
(1, 1, '2024-05-15', '2024-05-15', 55.00, 'Hizli Yag Servisi', 'Duzenli yag degisimi - 5W-30 sentetik yag', 2),
(3, 4, '2024-06-01', '2024-06-01', 225.00, 'Toyota Servis Merkezi', 'Kapsamli 20K kilometre servisi', 2),
(5, 2, '2024-05-20', '2024-05-20', 35.00, 'Lastik Uzmani', 'Lastik rotasyonu ve basinc kontrolu', 3),
(7, 3, '2024-05-25', '2024-05-25', 85.00, 'Fren Ustalari', 'Fren kontrolu - balatalar %60 seviyesinde', 3),
(9, 1, '2024-07-01', NULL, NULL, 'Ford Servis', 'Planlanmis yag degisimi', 2),
(11, 4, '2024-07-15', NULL, NULL, 'BMW Servis Merkezi', 'Planlanmis kapsamli servis', 2);

-- ================================================
-- 17. ORNEK ARAC HASARLARI
-- ================================================
INSERT INTO VehicleDamages (VehicleID, RentalID, DamageTypeID, DamageDate, DamageDescription, RepairCost, RepairDate, IsRepaired, ResponsibleParty, CreatedBy) VALUES
(3, 4, 1, '2024-05-05', 'Arka tamponda kucuk cizik', 180.00, '2024-05-08', 1, 'Musteri', 5),
(7, 5, 3, '2024-05-18', 'On lastikte kucuk delik', 125.00, '2024-05-19', 1, 'Yol tehlikesi', 6),
(15, NULL, 2, '2024-05-30', 'Otoparkta kapi gocugu', 320.00, '2024-06-02', 1, 'Bilinmiyor', 7);

-- ================================================
-- 18. ORNEK MUSTERI GERI BILDIRIMLERI
-- ================================================
INSERT INTO CustomerFeedback (RentalID, CustomerID, Rating, FeedbackText, FeedbackDate) VALUES
(4, 8, 5, 'Mukemmel hizmet! Arac temiz ve bakimliydi. Personel cok profesyoneldi.', '2024-05-06'),
(5, 9, 4, 'Genel olarak iyi bir deneyim. Arac tanitildigi gibiydi. Teslim alirken kucuk bir gecikme oldu.', '2024-05-19'),
(6, 11, 5, 'Olaganusta luks arac ve hizmet. Kesinlikle tekrar kiralayacagim!', '2024-04-26'),
(7, 12, 3, 'Arac fena degildi ama daha temiz olabilirdi. Kayit islemi yavastu.', '2024-05-15'),
(8, 13, 4, 'Guvenilir ulasim. Paranin karsiligi iyi. Kolay iade sureci.', '2024-04-04');

PRINT 'Tum ornek veriler basariyla eklendi!';
PRINT 'Veritabani icerigi:';
PRINT '- Uygun iliskilerle 15+ ilgili tablo';
PRINT '- Birden fazla kategoride 20+ arac';
PRINT '- Kiralama gecmisi olan 10+ musteri';
PRINT '- Aktif ve tamamlanmis kiralamalar';
PRINT '- Bakim programlari ve gecmisi';
PRINT '- Odeme kayitlari';
PRINT '- Musteri geri bildirimleri';
PRINT '- Arac hasar takibi';
PRINT '';
PRINT 'Uygulama entegrasyonu ve test icin hazir!';