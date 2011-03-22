BEGIN TRANSACTION;
DROP TABLE IF EXISTS "bibles";
CREATE TABLE "bibles" ("_id" INTEGER PRIMARY KEY  NOT NULL ,"language" TEXT,"biblename" TEXT,"biblepath" TEXT,"tablename" TEXT);
INSERT INTO "bibles" VALUES(1,'한글','개역개정','revision.sqlite','bible');
INSERT INTO "bibles" VALUES(2,'한글','공동번역','public.sqlite','bible');
INSERT INTO "bibles" VALUES(3,'English','NIv','niv.sqlite','bible');
COMMIT;
