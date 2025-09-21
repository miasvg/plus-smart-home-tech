DROP TABLE IF EXISTS products;

CREATE TABLE IF NOT EXISTS products (
    product_id UUID default gen_random_uuid() PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    description VARCHAR(5000) NOT NULL,
    image_src VARCHAR(255),
    quantity_state VARCHAR(20) NOT NULL,
    product_state VARCHAR(20) NOT NULL,
    product_category VARCHAR(20),
    price DECIMAL(19,2) NOT NULL
    );