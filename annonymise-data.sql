-- Useful sql commands for annonymising the production db.

-- select name, user_id, birthdate from rapidsms_contact LIMIT 10;

-- annonymise the contacts...
update rapidsms_contact set name = md5(name), birthdate = now() where id < 1000;
