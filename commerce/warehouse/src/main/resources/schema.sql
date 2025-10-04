DROP TABLE IF EXISTS warehouse_products, bookings, booking_products;

CREATE TABLE IF NOT EXISTS warehouse_products (
   product_id UUID NOT NULL UNIQUE PRIMARY KEY,
    fragile BOOLEAN,
    width DOUBLE PRECISION NOT NULL,
    height DOUBLE PRECISION NOT NULL,
    depth DOUBLE PRECISION NOT NULL,
    weight DOUBLE PRECISION NOT NULL,
    quantity INTEGER NOT NULL
);

create table if not exists bookings
(
    shopping_cart_id uuid primary key,
    delivery_weight  double precision not null,
    delivery_volume  double precision not null,
    fragile boolean not null,
    order_id uuid
);

create table if not exists booking_products
(
    shopping_cart_id uuid references bookings (shopping_cart_id) on delete cascade primary key,
    product_id uuid not null,
    quantity integer
    )