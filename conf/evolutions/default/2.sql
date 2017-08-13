--Cohorts
INSERT INTO cohorts (Id, OwnerId, Name)
VALUES (1, 2, '2017 MSc Computer Science FT');
INSERT INTO cohorts (Id, OwnerId, Name)
VALUES (2, 3, '2017 MSc CS FT');


--Cohort Members
INSERT INTO cohort_members (Id, OwnerId, Name, ParentId)
VALUES (1, 2, '2017 MSc CS FT Group');
INSERT INTO cohort_members (Id, OwnerId, Name, ParentId)
VALUES (1, 3, '2017 MSc CS FT Grp');


--Content Folders
INSERT INTO content_folders (Id, OwnerId, Name, ParentId)
VALUES(1,3,'CS',NULL);
INSERT INTO content_folders (Id, OwnerId, Name, ParentId)
VALUES(2,2,'Computer Science',NULL);


--Content Packages
INSERT INTO content_packages (Id, Name, FolderId, OwnerId)
VALUES('1','SDLC','1','3');
INSERT INTO content_packages (Id, Name, FolderId, OwnerId)
VALUES('2','Design Patterns','2','2');


--Content Assigned
INSERT INTO content_assigned (Id, Name, ExamDate, Active, OwnerId)
VALUES(1,'2017 MSc SDP','2017-10-01', 1, 2);
VALUES(2,'2017 MSc IS','2017-12-25', 1, 3);


--Content Assigned Cohorts
INSERT INTO content_assigned_cohorts (AssignedId, CohortId)
VALUES(1, 1);
INSERT INTO content_assigned_cohorts (AssignedId, CohortId)
VALUES(2, 1);


--Content Assigned Packages
INSERT INTO content_assigned_packages (AssignedId, PackageId)
VALUES(1, 2);
INSERT INTO content_assigned_packages (AssignedId, PackageId)
VALUES(2, 1);
