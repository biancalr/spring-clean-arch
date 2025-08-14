DROP SCHEMA IF EXISTS customer CASCADE;

CREATE SCHEMA customer;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DROP TYPE IF EXISTS customer_status;
CREATE TYPE customer_status AS ENUM ('CREATED');

CREATE TABLE "customer".customers
(
    id uuid NOT NULL,
    username character varying COLLATE pg_catalog."default" NOT NULL,
    first_name character varying COLLATE pg_catalog."default" NOT NULL,
    last_name character varying COLLATE pg_catalog."default" NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    customer_status customer_status NOT NULL,
    CONSTRAINT customers_pkey PRIMARY KEY (id)
);

DROP TYPE IF EXISTS outbox_status;
CREATE TYPE outbox_status AS ENUM ('STARTED', 'COMPLETED', 'FAILED');

DROP TABLE IF EXISTS "customer".customer_outbox CASCADE;

CREATE TABLE "customer".customer_outbox
(
    id uuid NOT NULL,
    saga_id uuid NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE,
    type character varying COLLATE pg_catalog."default" NOT NULL,
    payload jsonb NOT NULL,
    customer_status customer_status NOT NULL,
    outbox_status outbox_status NOT NULL,
    version integer NOT NULL,
    CONSTRAINT order_outbox_pkey PRIMARY KEY (id)
);

CREATE UNIQUE INDEX "customer_order_outbox_saga_id_customer_status_outbox_status"
    ON "customer".order_outbox
    (type, saga_id, customer_status, outbox_status);

CREATE UNIQUE INDEX "customer_status"
    ON "customer".order_outbox
    (customer_status);

DROP MATERIALIZED VIEW IF EXISTS customer.order_customer_m_view;

CREATE MATERIALIZED VIEW customer.order_customer_m_view
TABLESPACE pg_default
AS
 SELECT id,
    username,
    first_name,
    last_name
   FROM customer.customers
WITH DATA;

refresh materialized VIEW customer.order_customer_m_view;

DROP function IF EXISTS customer.refresh_order_customer_m_view;

CREATE OR replace function customer.refresh_order_customer_m_view()
returns trigger
AS '
BEGIN
    refresh materialized VIEW customer.order_customer_m_view;
    return null;
END;
'  LANGUAGE plpgsql;

DROP trigger IF EXISTS refresh_order_customer_m_view ON customer.customers;

CREATE trigger refresh_order_customer_m_view
after INSERT OR UPDATE OR DELETE OR truncate
ON customer.customers FOR each statement
EXECUTE PROCEDURE customer.refresh_order_customer_m_view();