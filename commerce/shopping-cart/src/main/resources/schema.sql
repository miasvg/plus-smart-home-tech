DROP TABLE IF EXISTS shopping_carts, shopping_carts_items;

CREATE TABLE IF NOT EXISTS shopping_carts (
    shopping_cart_id UUID default gen_random_uuid() PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    cart_state VARCHAR(100)
    );

CREATE TABLE IF NOT EXISTS shopping_carts_items (
    product_id UUID NOT NULL,
    quantity INTEGER,
    cart_id UUID REFERENCES shopping_carts (shopping_cart_id) ON DELETE CASCADE
    );