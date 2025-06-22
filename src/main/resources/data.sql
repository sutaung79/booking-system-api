-- This file will be executed automatically by Spring Boot on startup.
-- It's useful for seeding initial data for development and testing.

-- Credit Packages
-- Note: Using INSERT IGNORE to prevent errors on subsequent application restarts if the data already exists.
INSERT IGNORE INTO credit_package (id, name, credits, price, country, validity_in_days, created_at, updated_at) VALUES
(1, 'Basic Package SG', 5, 49.99, 'SINGAPORE', 30, NOW(), NOW()),
(2, 'Standard Package SG', 10, 89.99, 'SINGAPORE', 60, NOW(), NOW()),
(3, 'Premium Package SG', 20, 159.99, 'SINGAPORE', 90, NOW(), NOW()),
(4, 'Basic Package MY', 5, 39.99, 'MYANMAR', 30, NOW(), NOW());

-- Class Schedules
INSERT IGNORE INTO class_schedule (id, class_name, start_time, end_time, capacity, country, required_credits, created_at, updated_at) VALUES
(1, '1hr Yoga Class (SG)', DATE_ADD(NOW(), INTERVAL 15 MINUTE), DATE_ADD(NOW(), INTERVAL 75 MINUTE), 10, 'SINGAPORE', 1, NOW(), NOW()),
(2, 'HIIT Session (SG)', '2025-12-01 18:00:00', '2025-12-01 19:00:00', 5, 'SINGAPORE', 2, NOW(), NOW()),
(3, 'Zumba Fitness (SG)', '2025-12-02 19:00:00', '2025-12-02 20:00:00', 15, 'SINGAPORE', 1, NOW(), NOW()),
(4, 'Muay Thai Basics (MY)', '2025-12-05 17:00:00', '2025-12-05 18:00:00', 8, 'MYANMAR', 1, NOW(), NOW());