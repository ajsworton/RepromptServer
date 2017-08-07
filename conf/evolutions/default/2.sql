/*
SQLyog Ultimate v12.4.3 (64 bit)
MySQL - 5.7.11-log 
*********************************************************************
*/
/*!40101 SET NAMES utf8 */;

create table `users` (
	`Id` int (11),
	`FirstName` varchar (765),
	`SurName` varchar (765),
	`Email` varchar (765),
	`IsEmailVerified` tinyint (1),
	`IsEducator` tinyint (1),
	`IsAdministrator` tinyint (1),
	`AvatarUrl` varchar (765)
); 
insert into `users` (`Id`, `FirstName`, `SurName`, `Email`, `IsEmailVerified`, `IsEducator`, `IsAdministrator`, `AvatarUrl`) values('1','Admin','User','Undefined','1','1','1',NULL);
insert into `users` (`Id`, `FirstName`, `SurName`, `Email`, `IsEmailVerified`, `IsEducator`, `IsAdministrator`, `AvatarUrl`) values('2','Andrew','Gray','a@gray.com','1','0','0',NULL);
insert into `users` (`Id`, `FirstName`, `SurName`, `Email`, `IsEmailVerified`, `IsEducator`, `IsAdministrator`, `AvatarUrl`) values('5','Kirsten','Davis','k@davis.com','1','0','0',NULL);
insert into `users` (`Id`, `FirstName`, `SurName`, `Email`, `IsEmailVerified`, `IsEducator`, `IsAdministrator`, `AvatarUrl`) values('6','Leah','Matthews','l@matthews.com','1','0','0',NULL);
insert into `users` (`Id`, `FirstName`, `SurName`, `Email`, `IsEmailVerified`, `IsEducator`, `IsAdministrator`, `AvatarUrl`) values('7','Pauline','Powell','p@powell.com','0','0','0',NULL);
insert into `users` (`Id`, `FirstName`, `SurName`, `Email`, `IsEmailVerified`, `IsEducator`, `IsAdministrator`, `AvatarUrl`) values('8','Peter','Morris','p@morris.com','0','0','0',NULL);
insert into `users` (`Id`, `FirstName`, `SurName`, `Email`, `IsEmailVerified`, `IsEducator`, `IsAdministrator`, `AvatarUrl`) values('9','Christopher','Harris','c@harris.com','0','0','0',NULL);
insert into `users` (`Id`, `FirstName`, `SurName`, `Email`, `IsEmailVerified`, `IsEducator`, `IsAdministrator`, `AvatarUrl`) values('10','Phoebe','Roberts','p@roberts.com','0','0','0',NULL);
insert into `users` (`Id`, `FirstName`, `SurName`, `Email`, `IsEmailVerified`, `IsEducator`, `IsAdministrator`, `AvatarUrl`) values('11','Craig','Moran','c@moran.com','0','0','0',NULL);
insert into `users` (`Id`, `FirstName`, `SurName`, `Email`, `IsEmailVerified`, `IsEducator`, `IsAdministrator`, `AvatarUrl`) values('12','Paul','Hill','p@hill.com','0','0','0',NULL);
insert into `users` (`Id`, `FirstName`, `SurName`, `Email`, `IsEmailVerified`, `IsEducator`, `IsAdministrator`, `AvatarUrl`) values('13','Christopher','James','c@james.com','0','0','0',NULL);
insert into `users` (`Id`, `FirstName`, `SurName`, `Email`, `IsEmailVerified`, `IsEducator`, `IsAdministrator`, `AvatarUrl`) values('14','Martin','Simpson','m@simpson.com','0','0','0',NULL);
insert into `users` (`Id`, `FirstName`, `SurName`, `Email`, `IsEmailVerified`, `IsEducator`, `IsAdministrator`, `AvatarUrl`) values('15','Bethany','Clarke','b@clarke.com','0','0','0',NULL);
insert into `users` (`Id`, `FirstName`, `SurName`, `Email`, `IsEmailVerified`, `IsEducator`, `IsAdministrator`, `AvatarUrl`) values('16','Dean','Young','d@young.com','0','0','0',NULL);
insert into `users` (`Id`, `FirstName`, `SurName`, `Email`, `IsEmailVerified`, `IsEducator`, `IsAdministrator`, `AvatarUrl`) values('17','Mohammed','Irfaz','m-iraz.com','0','0','0',NULL);
insert into `users` (`Id`, `FirstName`, `SurName`, `Email`, `IsEmailVerified`, `IsEducator`, `IsAdministrator`, `AvatarUrl`) values('18','Lewis','Harrison','l-harrison.com','0','0','0',NULL);
insert into `users` (`Id`, `FirstName`, `SurName`, `Email`, `IsEmailVerified`, `IsEducator`, `IsAdministrator`, `AvatarUrl`) values('19','Patrick','Richardson','p@Richardson.com','0','0','0',NULL);
insert into `users` (`Id`, `FirstName`, `SurName`, `Email`, `IsEmailVerified`, `IsEducator`, `IsAdministrator`, `AvatarUrl`) values('20','Gary','Mason','g@mason.com','0','0','0',NULL);
insert into `users` (`Id`, `FirstName`, `SurName`, `Email`, `IsEmailVerified`, `IsEducator`, `IsAdministrator`, `AvatarUrl`) values('21','Olivia','Reid','o-reid.com','0','0','0',NULL);
