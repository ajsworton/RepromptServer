# Users schema

# --- !Ups

ALTER TABLE Users
    ADD AvatarUrl varchar(255) DEFAULT NULL;

# --- !Downs

ALTER TABLE Users
  DROP COLUMN AvatarUrl;