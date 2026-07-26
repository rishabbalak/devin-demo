-- ---------------------------------------------------------------------------------------
-- Seed data for the simulated WMSDTA library.
--
-- Dates are CYYMMDD, the 7-digit packed-decimal convention used throughout these files:
-- a leading century digit (0 = 19xx, 1 = 20xx) followed by YYMMDD. 0 is the legacy null.
--
-- Volumes here are deliberately small, but the shape is drawn from Costco's disclosed
-- FY2025 figures: three membership tiers, fewer than 4,000 active SKUs per warehouse, and
-- a warehouse network spanning several countries.
-- ---------------------------------------------------------------------------------------

-- WHSMAST -- warehouse master
INSERT INTO WHSMAST (WHWHS, WHNAME, WHCITY, WHSTAT, WHCTRY) VALUES
  ('W001', 'ISSAQUAH', 'ISSAQUAH', 'WA', 'USA'),
  ('W002', 'SEATTLE FOURTH AVE', 'SEATTLE', 'WA', 'USA'),
  ('W003', 'SAN DIEGO MORENA', 'SAN DIEGO', 'CA', 'USA'),
  ('W004', 'AUSTIN SOUTHWEST', 'AUSTIN', 'TX', 'USA'),
  ('D010', 'MIRA LOMA DEPOT', 'MIRA LOMA', 'CA', 'USA'),
  ('D020', 'SUMNER DEPOT', 'SUMNER', 'WA', 'USA');

-- MBRMAST -- member master
-- MBTIER: GS = Gold Star, BU = Business, EX = Executive.  MBSTAT: A = active, I = inactive.
INSERT INTO MBRMAST (MBMBRN, MBNAME, MBTIER, MBSTAT, MBJOIN, MBRNWD, MBWHS) VALUES
  ('111000000042', 'HALVORSEN, ANNIKA', 'EX', 'A', 1120318, 1261130, 'W001'),
  ('111000000156', 'OYELARAN, TUNDE', 'GS', 'A', 1190904, 1260930, 'W002'),
  ('111000000287', 'NAKASHIMA FARMS LLC', 'BU', 'A', 1080612, 1270612, 'W004'),
  ('111000000341', 'PETROV, MILENA', 'EX', 'A', 1150127, 1260815, 'W003'),
  ('111000000498', 'ABERNATHY, COLE', 'GS', 'I', 1170722, 1250722, 'W002'),
  ('111000000502', 'RIVERA CATERING CO', 'BU', 'A', 1210419, 1270419, 'W004'),
  ('111000000633', 'SZABO, ISTVAN', 'EX', 'A', 1230208, 1260208, 'W001'),
  ('111000000774', 'DIALLO, FATOUMATA', 'GS', 'A', 1240516, 1270516, 'W003');

-- ITEMMAST -- item master.  IMSTAT: A = active, D = discontinued.
INSERT INTO ITEMMAST (IMITEM, IMDESC, IMDEPT, IMUOM, IMPRICE, IMSTAT) VALUES
  ('1204471', 'ORGANIC OLIVE OIL 2L', 'GROC', 'EA', 21.99, 'A'),
  ('1204488', 'BATH TISSUE 30 ROLL', 'PAPR', 'CS', 24.99, 'A'),
  ('1204512', 'ROTISSERIE CHICKEN', 'DELI', 'EA', 4.99, 'A'),
  ('1204536', 'ALMOND BUTTER 27OZ', 'GROC', 'EA', 12.49, 'A'),
  ('1204573', 'PAPER TOWELS 12 ROLL', 'PAPR', 'CS', 22.99, 'A'),
  ('1204590', 'COFFEE WHOLE BEAN 3LB', 'GROC', 'EA', 18.99, 'A'),
  ('1204617', 'LAUNDRY DETERGENT 194OZ', 'HHLD', 'EA', 19.99, 'A'),
  ('1204624', 'TRAIL MIX 4LB', 'GROC', 'EA', 14.99, 'A'),
  ('1204658', 'BATTERIES AA 48 PACK', 'HHLD', 'PK', 17.99, 'A'),
  ('1204671', 'MAPLE SYRUP 1L', 'GROC', 'EA', 13.99, 'A'),
  ('1204695', 'DISH SOAP 90OZ', 'HHLD', 'EA', 9.99, 'A'),
  ('1204702', 'MIXED NUTS 2.5LB', 'GROC', 'EA', 19.99, 'A'),
  ('1204719', 'WATER 40 PACK', 'BEVG', 'CS', 4.99, 'A'),
  ('1204726', 'PROTEIN BARS 20 CT', 'GROC', 'PK', 21.99, 'A'),
  ('1204733', 'SPARKLING WATER 36 CT', 'BEVG', 'CS', 12.99, 'A'),
  ('1204740', 'SEASONAL GIFT BASKET', 'SEAS', 'EA', 49.99, 'D');

-- INVBAL -- inventory balance by warehouse, item and location.
-- IBSTAT: AV = available, XD = cross-dock staging, QC = quality hold, DM = damaged.
-- QC and DM rows carry real on-hand quantities but are never nettable.
INSERT INTO INVBAL (IBWHS, IBITEM, IBLOCN, IBQOH, IBQALC, IBSTAT) VALUES
  ('W001', '1204471', 'A12-04-B', 1440, 360, 'AV'),
  ('W001', '1204471', 'A12-04-C', 960, 0, 'AV'),
  ('W001', '1204471', 'QC-HOLD', 36, 0, 'QC'),
  ('W001', '1204488', 'A14-01-A', 288, 288, 'AV'),
  ('W001', '1204488', 'B02-11-D', 2016, 144, 'AV'),
  ('W001', '1204512', 'D01-00-01', 180, 24, 'AV'),
  ('W001', '1204536', 'B02-12-A', 72, 72, 'AV'),
  ('W001', '1204573', 'C04-08-B', 864, 120, 'AV'),
  ('W001', '1204590', 'A09-02-C', 1200, 240, 'AV'),
  ('W001', '1204617', 'C07-03-A', 540, 60, 'AV'),
  ('W001', '1204624', 'A11-05-D', 396, 0, 'AV'),
  ('W001', '1204658', 'E02-01-A', 720, 96, 'AV'),
  ('W001', '1204671', 'A10-07-B', 264, 264, 'AV'),
  ('W001', '1204695', 'C07-04-C', 480, 0, 'AV'),
  ('W001', '1204702', 'A11-06-A', 312, 48, 'AV'),
  ('W001', '1204719', 'F01-02-A', 3600, 480, 'AV'),
  ('W001', '1204726', 'A13-01-B', 228, 0, 'AV'),
  ('W001', '1204733', 'F01-03-C', 1440, 216, 'AV'),
  ('W001', '1204740', 'R41-00-00', 24, 0, 'DM'),
  ('W002', '1204471', 'A03-01-A', 720, 120, 'AV'),
  ('W002', '1204488', 'A03-02-B', 1080, 0, 'AV'),
  ('W002', '1204512', 'D01-00-01', 96, 12, 'AV'),
  ('W002', '1204590', 'B01-04-C', 600, 72, 'AV'),
  ('W002', '1204719', 'F02-01-A', 2400, 300, 'AV'),
  ('W003', '1204471', 'A01-01-A', 480, 48, 'AV'),
  ('W003', '1204573', 'C01-01-B', 384, 0, 'AV'),
  ('W003', '1204624', 'A02-03-C', 192, 24, 'AV'),
  ('W003', '1204733', 'F01-01-A', 960, 0, 'AV'),
  ('W004', '1204488', 'A05-02-A', 648, 72, 'AV'),
  ('W004', '1204617', 'C02-01-D', 300, 0, 'AV'),
  ('W004', '1204702', 'A06-01-B', 156, 156, 'AV'),
  ('D010', '1204719', 'XD-STAGE', 7200, 7200, 'XD'),
  ('D010', '1204488', 'XD-STAGE', 2880, 1440, 'XD'),
  ('D020', '1204471', 'XD-STAGE', 1920, 960, 'XD');

-- ORDHDR -- order header.  OHSTAT: AL = allocated, SH = shipped, BO = backorder, HL = held.
-- OHSRC records the channel the order arrived through.
INSERT INTO ORDHDR (OHORDN, OHMBRN, OHWHS, OHSTAT, OHORDT, OHTOTL, OHSRC) VALUES
  ('0000148223', '111000000042', 'W001', 'AL', 1260714, 402.83, 'WEB'),
  ('0000148224', '111000000287', 'W004', 'SH', 1260709, 1124.55, 'TRM'),
  ('0000148225', '111000000341', 'W003', 'BO', 1260721, 287.90, 'WEB'),
  ('0000148226', '111000000502', 'W004', 'HL', 1260723, 659.67, 'TRM'),
  ('0000148227', '111000000633', 'W001', 'AL', 1260725, 178.94, 'WEB');

-- ORDDTL -- order detail lines, joined to ORDHDR on order number in application code.
INSERT INTO ORDDTL (ODORDN, ODLINE, ODITEM, ODQORD, ODQALC, ODUOM, ODPRIC) VALUES
  ('0000148223', 10, '1204471', 6, 6, 'EA', 21.99),
  ('0000148223', 20, '1204488', 4, 4, 'CS', 24.99),
  ('0000148223', 30, '1204590', 8, 8, 'EA', 18.99),
  ('0000148223', 40, '1204719', 5, 5, 'CS', 4.99),
  ('0000148224', 10, '1204488', 24, 24, 'CS', 24.99),
  ('0000148224', 20, '1204573', 18, 18, 'CS', 22.99),
  ('0000148224', 30, '1204617', 6, 6, 'EA', 19.99),
  ('0000148225', 10, '1204471', 4, 4, 'EA', 21.99),
  ('0000148225', 20, '1204624', 6, 6, 'EA', 14.99),
  ('0000148225', 30, '1204702', 8, 0, 'EA', 19.99),
  ('0000148226', 10, '1204702', 12, 12, 'EA', 19.99),
  ('0000148226', 20, '1204617', 15, 15, 'EA', 19.99),
  ('0000148226', 30, '1204488', 5, 5, 'CS', 24.99),
  ('0000148227', 10, '1204536', 4, 4, 'EA', 12.49),
  ('0000148227', 20, '1204671', 3, 3, 'EA', 13.99),
  ('0000148227', 30, '1204726', 4, 4, 'PK', 21.99);
