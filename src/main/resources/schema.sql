CREATE TABLE customer (
    id BIGINT PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);
CREATE TABLE purchase_transaction (
    id BIGINT PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customer(id),
    amount DECIMAL(12,2) NOT NULL CHECK (amount >= 0),
    transaction_date DATE NOT NULL
);
CREATE INDEX idx_purchase_date_customer ON purchase_transaction(transaction_date, customer_id);
