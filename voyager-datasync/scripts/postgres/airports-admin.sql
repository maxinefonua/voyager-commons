-- Table: public.airports_copy drop and create statement to be run by pg superuser ONLY
DROP TABLE IF EXISTS public.airports_copy;
CREATE TABLE IF NOT EXISTS public.airports_copy
(
    icao character(4) COLLATE pg_catalog."default" NOT NULL,
    iata character(3) COLLATE pg_catalog."default",
    name text COLLATE pg_catalog."default",
    city character varying(50) COLLATE pg_catalog."default",
    subd character varying(50) COLLATE pg_catalog."default",
    country character(2) COLLATE pg_catalog."default",
    elevation real,
    lat real,
    lon real,
    tz character varying(50) COLLATE pg_catalog."default",
    lid character varying(5) COLLATE pg_catalog."default",
    CONSTRAINT airports_copy_pkey PRIMARY KEY (icao)
)
TABLESPACE pg_default;
ALTER TABLE IF EXISTS public.airports_copy
    OWNER to $PGADMIN;
GRANT ALL ON TABLE public.airports_copy TO $AIRPORT_DATASYNC_USER;
GRANT ALL ON TABLE public.airports_copy TO $PGADMIN;