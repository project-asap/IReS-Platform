PRAGMA foreign_keys=OFF;
BEGIN TRANSACTION;
CREATE TABLE weka_tfidf 
(id INTEGER PRIMARY KEY AUTOINCREMENT, documents INTEGER, input_size INTEGER, output_size INTEGER, 
	time INTEGER, minDF INTEGER, dimensions INTEGER, metrics TEXT, date DATE DEFAULT (datetime('now','localtime')));
INSERT INTO "weka_tfidf" VALUES(1,300,2384486,433265,2142,110,402,'','2015-07-28 15:59:10');
INSERT INTO "weka_tfidf" VALUES(2,300,2384486,642591,1939,60,793,'','2015-07-28 15:59:16');
INSERT INTO "weka_tfidf" VALUES(3,300,2384486,1423344,2212,10,4087,'','2015-07-28 15:59:27');
INSERT INTO "weka_tfidf" VALUES(4,5300,42079918,23099550,23363,110,5230,'','2015-07-28 16:05:52');
INSERT INTO "weka_tfidf" VALUES(5,5300,42079918,25177658,25974,60,7659,'','2015-07-28 16:36:57');
INSERT INTO "weka_tfidf" VALUES(6,5300,42079918,30066045,26533,10,21478,'','2015-07-28 17:17:14');
INSERT INTO "weka_tfidf" VALUES(7,10300,85126270,49191719,46669,110,8261,'','2015-07-28 19:20:31');
INSERT INTO "weka_tfidf" VALUES(8,300,2384486,1423344,2189,10,4087,'','2015-07-28 21:36:38');
INSERT INTO "weka_tfidf" VALUES(9,1300,10615134,6809176,6908,10,9905,'','2015-07-28 21:41:13');
INSERT INTO "weka_tfidf" VALUES(10,2300,18515500,12518881,11614,10,13456,'','2015-07-28 21:49:42');
INSERT INTO "weka_tfidf" VALUES(11,3300,26158981,18436938,16690,10,16239,'','2015-07-28 22:10:49');
INSERT INTO "weka_tfidf" VALUES(12,4300,34270414,24414604,20687,10,18977,'','2015-07-28 22:38:49');
INSERT INTO "weka_tfidf" VALUES(13,300,2384486,323847,1681,160,259,'','2015-07-29 15:52:53');
INSERT INTO "weka_tfidf" VALUES(14,300,2384486,433265,1705,110,402,'','2015-07-29 15:52:56');
INSERT INTO "weka_tfidf" VALUES(15,300,2384486,642591,1743,60,793,'','2015-07-29 15:52:58');
INSERT INTO "weka_tfidf" VALUES(16,1300,10615134,3372413,6029,160,1302,'','2015-07-29 15:56:54');
INSERT INTO "weka_tfidf" VALUES(17,1300,10615134,3988722,6137,110,1835,'','2015-07-29 15:57:10');
INSERT INTO "weka_tfidf" VALUES(18,1300,10615134,4971867,6256,60,3066,'','2015-07-29 15:57:26');
INSERT INTO "weka_tfidf" VALUES(19,2300,18515500,7530642,10177,160,2156,'','2015-07-29 16:02:38');
INSERT INTO "weka_tfidf" VALUES(20,2300,18515500,8526932,10398,110,2907,'','2015-07-29 16:03:16');
INSERT INTO "weka_tfidf" VALUES(21,2300,18515500,9913394,10423,60,4554,'','2015-07-29 16:04:22');
INSERT INTO "weka_tfidf" VALUES(22,3300,26158981,12243914,14295,160,2865,'','2015-07-29 16:09:52');
INSERT INTO "weka_tfidf" VALUES(23,3300,26158981,13541005,14371,110,3793,'','2015-07-29 16:11:29');
INSERT INTO "weka_tfidf" VALUES(24,3300,26158981,15063325,14653,60,5631,'','2015-07-29 16:13:53');
INSERT INTO "weka_tfidf" VALUES(25,4300,34270414,17028537,18499,160,3486,'','2015-07-29 16:22:20');
INSERT INTO "weka_tfidf" VALUES(26,4300,34270414,18455377,18651,110,4549,'','2015-07-29 16:25:09');
INSERT INTO "weka_tfidf" VALUES(27,4300,34270414,20183429,19035,60,6629,'','2015-07-29 16:28:19');
INSERT INTO "weka_tfidf" VALUES(28,5300,42079918,21502226,25239,160,4053,'','2015-07-29 20:53:34');
INSERT INTO "weka_tfidf" VALUES(29,5300,42079918,21502226,24079,160,4053,'','2015-07-29 21:02:37');
INSERT INTO "weka_tfidf" VALUES(30,5300,42079918,23099550,25356,110,5230,'','2015-07-29 21:17:14');
INSERT INTO "weka_tfidf" VALUES(31,5300,42079918,25177658,25579,60,7659,'','2015-07-29 21:26:15');
INSERT INTO "weka_tfidf" VALUES(32,5300,42079918,30066045,26088,10,21478,'','2015-07-29 21:36:43');
INSERT INTO "weka_tfidf" VALUES(33,10300,85126270,46739333,50219,160,6525,'','2015-07-29 22:24:40');
INSERT INTO "weka_tfidf" VALUES(34,10300,85126270,49191719,48109,110,8261,'','2015-07-29 22:52:16');
INSERT INTO "weka_tfidf" VALUES(35,10300,85126270,52650040,51139,60,11660,'','2015-07-29 23:07:37');
INSERT INTO "weka_tfidf" VALUES(36,10300,85126270,60176477,50730,10,30791,'','2015-07-29 23:32:08');
INSERT INTO "weka_tfidf" VALUES(37,15300,125439008,71961132,69122,160,8168,'','2015-07-30 00:07:30');
INSERT INTO "weka_tfidf" VALUES(38,15300,125439008,74920509,71352,110,10207,'','2015-07-30 00:08:42');
INSERT INTO "weka_tfidf" VALUES(39,15300,125439008,79907903,71632,60,14179,'','2015-07-30 00:09:53');
INSERT INTO "weka_tfidf" VALUES(40,15300,125439008,88996866,74841,10,37079,'','2015-07-30 00:11:08');
INSERT INTO "weka_tfidf" VALUES(41,20300,169566173,98626468,97248,160,9657,'','2015-07-30 00:25:01');
INSERT INTO "weka_tfidf" VALUES(42,20300,169566173,102917520,94797,110,11907,'','2015-07-30 00:26:36');
INSERT INTO "weka_tfidf" VALUES(43,20300,169566173,108727319,97529,60,16495,'','2015-07-30 00:28:14');
INSERT INTO "weka_tfidf" VALUES(44,20300,169566173,119017161,101521,10,42927,'','2015-07-30 00:29:55');
INSERT INTO "weka_tfidf" VALUES(45,25300,208459106,122867896,116082,160,11195,'','2015-07-30 00:45:54');
INSERT INTO "weka_tfidf" VALUES(46,25300,208459106,128045957,121120,110,13806,'','2015-07-30 00:47:55');
INSERT INTO "weka_tfidf" VALUES(47,25300,208459106,134748433,118755,60,19116,'','2015-07-30 00:49:55');
INSERT INTO "weka_tfidf" VALUES(48,25300,208459106,146422438,122739,10,50451,'','2015-07-30 00:51:58');
INSERT INTO "weka_tfidf" VALUES(49,30300,245805370,145205334,138700,160,12490,'','2015-07-30 01:10:26');
INSERT INTO "weka_tfidf" VALUES(50,30300,245805370,151049484,138111,110,15450,'','2015-07-30 01:12:45');
INSERT INTO "weka_tfidf" VALUES(51,30300,245805370,158146847,141796,60,21442,'','2015-07-30 01:15:07');
INSERT INTO "weka_tfidf" VALUES(52,30300,245805370,171389912,144324,10,57195,'','2015-07-30 01:17:31');
INSERT INTO "weka_tfidf" VALUES(53,35300,286585839,170422694,161829,160,13641,'','2015-07-30 02:35:23');
INSERT INTO "weka_tfidf" VALUES(54,35300,286585839,176564566,165732,110,16790,'','2015-07-30 02:38:09');
INSERT INTO "weka_tfidf" VALUES(55,35300,286585839,184606996,166011,60,23499,'','2015-07-30 02:40:55');
INSERT INTO "weka_tfidf" VALUES(56,35300,286585839,199367750,171001,10,64072,'','2015-07-30 02:43:47');
INSERT INTO "weka_tfidf" VALUES(57,45300,364375904,219967767,211980,160,15671,'','2015-07-30 03:09:53');
INSERT INTO "weka_tfidf" VALUES(58,45300,364375904,226897100,212312,110,19232,'','2015-07-30 03:13:27');
INSERT INTO "weka_tfidf" VALUES(59,45300,364375904,235982029,214866,60,26915,'','2015-07-30 03:17:02');
INSERT INTO "weka_tfidf" VALUES(60,45300,364375904,253518238,230186,10,73309,'','2015-07-30 03:20:53');
INSERT INTO "weka_tfidf" VALUES(61,55300,449910132,274114452,267608,160,17794,'','2015-07-30 03:52:07');
INSERT INTO "weka_tfidf" VALUES(62,55300,449910132,282214176,262943,110,22040,'','2015-07-30 03:56:31');
INSERT INTO "weka_tfidf" VALUES(63,55300,449910132,292170865,261710,60,30641,'','2015-07-30 04:00:53');
INSERT INTO "weka_tfidf" VALUES(64,55300,449910132,312428219,275171,10,85187,'','2015-07-30 04:05:28');
CREATE TABLE weka_kmeans_text 
(id INTEGER PRIMARY KEY AUTOINCREMENT, documents INTEGER, k INTEGER, dimensions INTEGER,input_size INTEGER,
	output_size INTEGER, time INTEGER, metrics TEXT, date DATE DEFAULT (datetime('now','localtime')));
INSERT INTO "weka_kmeans_text" VALUES(1,300,5,402,433265,32433,941,'','2015-07-28 15:59:11');
INSERT INTO "weka_kmeans_text" VALUES(2,300,10,402,433265,54863,848,'','2015-07-28 15:59:12');
INSERT INTO "weka_kmeans_text" VALUES(3,300,15,402,433265,77288,1036,'','2015-07-28 15:59:13');
INSERT INTO "weka_kmeans_text" VALUES(4,300,20,402,433265,99712,1217,'','2015-07-28 15:59:14');
INSERT INTO "weka_kmeans_text" VALUES(5,300,5,793,642591,67313,1956,'','2015-07-28 15:59:18');
INSERT INTO "weka_kmeans_text" VALUES(6,300,10,793,642591,111247,2031,'','2015-07-28 15:59:20');
INSERT INTO "weka_kmeans_text" VALUES(7,300,15,793,642591,155177,2020,'','2015-07-28 15:59:22');
INSERT INTO "weka_kmeans_text" VALUES(8,300,20,793,642591,199107,2245,'','2015-07-28 15:59:24');
INSERT INTO "weka_kmeans_text" VALUES(9,300,5,4087,1423344,372652,3361,'','2015-07-28 15:59:30');
INSERT INTO "weka_kmeans_text" VALUES(10,300,10,4087,1423344,597757,3776,'','2015-07-28 15:59:34');
INSERT INTO "weka_kmeans_text" VALUES(11,300,15,4087,1423344,822857,3883,'','2015-07-28 15:59:38');
INSERT INTO "weka_kmeans_text" VALUES(12,300,20,4087,1423344,1047957,6006,'','2015-07-28 15:59:44');
INSERT INTO "weka_kmeans_text" VALUES(13,5300,5,5230,23099550,476670,443262,'','2015-07-28 16:13:16');
INSERT INTO "weka_kmeans_text" VALUES(14,5300,10,5230,23099550,764645,522415,'','2015-07-28 16:21:58');
INSERT INTO "weka_kmeans_text" VALUES(15,5300,15,5230,23099550,1052615,387630,'','2015-07-28 16:28:26');
INSERT INTO "weka_kmeans_text" VALUES(16,5300,20,5230,23099550,1340585,484641,'','2015-07-28 16:36:31');
INSERT INTO "weka_kmeans_text" VALUES(17,5300,5,7659,25177658,697709,216883,'','2015-07-28 16:40:33');
INSERT INTO "weka_kmeans_text" VALUES(18,5300,10,7659,25177658,1119279,561483,'','2015-07-28 16:49:55');
INSERT INTO "weka_kmeans_text" VALUES(19,5300,15,7659,25177658,1540844,764106,'','2015-07-28 17:02:39');
INSERT INTO "weka_kmeans_text" VALUES(20,5300,20,7659,25177658,1962409,848431,'','2015-07-28 17:16:48');
INSERT INTO "weka_kmeans_text" VALUES(21,5300,5,21478,30066045,2084136,534022,'','2015-07-28 17:26:08');
INSERT INTO "weka_kmeans_text" VALUES(22,5300,10,21478,30066045,3265751,2301846,'','2015-07-28 18:04:30');
INSERT INTO "weka_kmeans_text" VALUES(23,5300,15,21478,30066045,4447362,2759380,'','2015-07-28 18:50:29');
INSERT INTO "weka_kmeans_text" VALUES(24,5300,20,21478,30066045,5628972,1311973,'','2015-07-28 19:12:21');
INSERT INTO "weka_kmeans_text" VALUES(25,10300,5,8261,49191719,752496,605730,'','2015-07-28 19:30:37');
INSERT INTO "weka_kmeans_text" VALUES(26,10300,10,8261,49191719,1207182,860418,'','2015-07-28 19:44:58');
INSERT INTO "weka_kmeans_text" VALUES(27,10300,15,8261,49191719,1661862,2421481,'','2015-07-28 20:25:19');
INSERT INTO "weka_kmeans_text" VALUES(28,300,5,4087,1423344,372652,3077,'','2015-07-28 21:36:41');
INSERT INTO "weka_kmeans_text" VALUES(29,300,10,4087,1423344,597757,3432,'','2015-07-28 21:36:45');
INSERT INTO "weka_kmeans_text" VALUES(30,300,15,4087,1423344,822857,3809,'','2015-07-28 21:36:48');
INSERT INTO "weka_kmeans_text" VALUES(31,300,20,4087,1423344,1047957,6248,'','2015-07-28 21:36:55');
INSERT INTO "weka_kmeans_text" VALUES(32,1300,5,9905,6809176,902094,44021,'','2015-07-28 21:41:57');
INSERT INTO "weka_kmeans_text" VALUES(33,1300,10,9905,6809176,1447195,42907,'','2015-07-28 21:42:40');
INSERT INTO "weka_kmeans_text" VALUES(34,1300,15,9905,6809176,1992288,45620,'','2015-07-28 21:43:25');
INSERT INTO "weka_kmeans_text" VALUES(35,1300,20,9905,6809176,2537384,93785,'','2015-07-28 21:44:59');
INSERT INTO "weka_kmeans_text" VALUES(36,2300,5,13456,12518881,1238697,115027,'','2015-07-28 21:51:37');
INSERT INTO "weka_kmeans_text" VALUES(37,2300,10,13456,12518881,1979101,217190,'','2015-07-28 21:55:14');
INSERT INTO "weka_kmeans_text" VALUES(38,2300,15,13456,12518881,2719502,315864,'','2015-07-28 22:00:30');
INSERT INTO "weka_kmeans_text" VALUES(39,2300,20,13456,12518881,3459902,308670,'','2015-07-28 22:05:39');
INSERT INTO "weka_kmeans_text" VALUES(40,3300,5,16239,18436938,1527221,265704,'','2015-07-28 22:15:15');
INSERT INTO "weka_kmeans_text" VALUES(41,3300,10,16239,18436938,2420691,422411,'','2015-07-28 22:22:17');
INSERT INTO "weka_kmeans_text" VALUES(42,3300,15,16239,18436938,3314156,276986,'','2015-07-28 22:26:54');
INSERT INTO "weka_kmeans_text" VALUES(43,3300,20,16239,18436938,4207621,350473,'','2015-07-28 22:32:45');
INSERT INTO "weka_kmeans_text" VALUES(44,4300,5,18977,24414604,1784593,368400,'','2015-07-28 22:44:57');
INSERT INTO "weka_kmeans_text" VALUES(45,4300,10,18977,24414604,2828653,302883,'','2015-07-28 22:50:00');
INSERT INTO "weka_kmeans_text" VALUES(46,4300,15,18977,24414604,3872708,562724,'','2015-07-28 22:59:23');
INSERT INTO "weka_kmeans_text" VALUES(47,4300,20,18977,24414604,4916763,619051,'','2015-07-28 23:09:42');
INSERT INTO "weka_kmeans_text" VALUES(48,300,10,259,323847,35700,753,'','2015-07-29 15:52:54');
INSERT INTO "weka_kmeans_text" VALUES(49,300,10,402,433265,54863,789,'','2015-07-29 15:52:56');
INSERT INTO "weka_kmeans_text" VALUES(50,300,10,793,642591,111247,1812,'','2015-07-29 15:53:00');
INSERT INTO "weka_kmeans_text" VALUES(51,1300,10,1302,3372413,183313,9424,'','2015-07-29 15:57:03');
INSERT INTO "weka_kmeans_text" VALUES(52,1300,10,1835,3988722,257935,10073,'','2015-07-29 15:57:20');
INSERT INTO "weka_kmeans_text" VALUES(53,1300,10,3066,4971867,442559,55998,'','2015-07-29 15:58:22');
INSERT INTO "weka_kmeans_text" VALUES(54,2300,10,2156,7530642,302875,27565,'','2015-07-29 16:03:05');
INSERT INTO "weka_kmeans_text" VALUES(55,2300,10,2907,8526932,413839,55565,'','2015-07-29 16:04:11');
INSERT INTO "weka_kmeans_text" VALUES(56,2300,10,4554,9913394,665948,53684,'','2015-07-29 16:05:15');
INSERT INTO "weka_kmeans_text" VALUES(57,3300,10,2865,12243914,407876,82843,'','2015-07-29 16:11:14');
INSERT INTO "weka_kmeans_text" VALUES(58,3300,10,3793,13541005,547247,129013,'','2015-07-29 16:13:38');
INSERT INTO "weka_kmeans_text" VALUES(59,3300,10,5631,15063325,823191,207685,'','2015-07-29 16:17:20');
INSERT INTO "weka_kmeans_text" VALUES(60,4300,10,3486,17028537,503039,150929,'','2015-07-29 16:24:51');
INSERT INTO "weka_kmeans_text" VALUES(61,4300,10,4549,18455377,665220,170304,'','2015-07-29 16:28:00');
INSERT INTO "weka_kmeans_text" VALUES(62,4300,10,6629,20183429,968899,210130,'','2015-07-29 16:31:49');
INSERT INTO "weka_kmeans_text" VALUES(63,5300,10,4053,21502226,584687,851086,'','2015-07-29 21:16:49');
INSERT INTO "weka_kmeans_text" VALUES(64,5300,10,5230,23099550,764645,515562,'','2015-07-29 21:25:50');
INSERT INTO "weka_kmeans_text" VALUES(65,5300,10,7659,25177658,1119279,601284,'','2015-07-29 21:36:16');
INSERT INTO "weka_kmeans_text" VALUES(66,5300,10,21478,30066045,3265751,2344064,'','2015-07-29 22:15:47');
INSERT INTO "weka_kmeans_text" VALUES(67,10300,10,6525,46739333,940665,1608364,'','2015-07-29 22:51:28');
INSERT INTO "weka_kmeans_text" VALUES(68,10300,10,8261,49191719,1207182,869794,'','2015-07-29 23:06:46');
INSERT INTO "weka_kmeans_text" VALUES(69,10300,10,11660,52650040,1703435,1420239,'','2015-07-29 23:31:17');
CREATE TABLE arff2mahout
(id INTEGER PRIMARY KEY AUTOINCREMENT, documents INTEGER, dimensions INTEGER, input_size INTEGER, output_size INTEGER,
	time INTEGER, metrics TEXT,  date DATE DEFAULT (datetime('now','localtime')));
CREATE TABLE arff2spark
(id INTEGER PRIMARY KEY AUTOINCREMENT, documents INTEGER, dimensions INTEGER, input_size INTEGER, output_size INTEGER, time INTEGER, metrics TEXT, date DATE DEFAULT (datetime('now','localtime')));
DELETE FROM sqlite_sequence;
INSERT INTO "sqlite_sequence" VALUES('weka_tfidf',64);
INSERT INTO "sqlite_sequence" VALUES('weka_kmeans_text',69);
COMMIT;