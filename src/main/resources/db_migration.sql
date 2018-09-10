ALTER TABLE transactions
ADD is_matched boolean;

ALTER TABLE transactions
ADD reason varchar(200);