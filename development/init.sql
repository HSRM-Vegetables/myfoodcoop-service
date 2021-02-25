CREATE
    USER vegetables WITH PASSWORD 'insecure-password-for-local-development' CREATEDB;
CREATE
    DATABASE vegetables_data
    WITH
    OWNER = vegetables
    ENCODING = 'UTF8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;
