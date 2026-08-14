-- Admin seed account (per Section 1, F1: admin assigned via seed, no signup flow)
-- Password is 'AdminPass123' hashed with bcrypt
INSERT INTO users (name, email, password_hash, role) VALUES
('Admin', 'admin@manishamart.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN');

-- Sample seller
INSERT INTO users (name, email, password_hash, role) VALUES
('Manisha Seller', 'seller@manishamart.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'SELLER');

-- Sample buyer
INSERT INTO users (name, email, password_hash, role) VALUES
('Test Buyer', 'buyer@manishamart.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'BUYER');

-- Sample products (seller_id = 2, the seller above)
INSERT INTO products (seller_id, name, description, price, stock_qty, category, image_url) VALUES
(2, 'Wireless Mouse', 'Ergonomic wireless mouse with USB receiver', 599.00, 50, 'Electronics', 'https://placehold.co/300x300?text=Mouse'),
(2, 'Cotton T-Shirt', 'Comfortable 100% cotton t-shirt, size M', 349.00, 100, 'Clothing', 'https://placehold.co/300x300?text=Tshirt'),
(2, 'Notebook Set', 'Pack of 3 ruled notebooks, 200 pages each', 199.00, 200, 'Stationery', 'https://placehold.co/300x300?text=Notebook');
