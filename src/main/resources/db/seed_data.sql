-- Seed data for cosplay rental application
-- This file can be safely committed to version control
-- Run this to populate your database with sample/test data
-- Note: Admin user is auto-created by Database.init(), customer accounts are device-specific

-- Sample Cosplays
INSERT OR IGNORE INTO cosplays (name, category, series_name, size, description, image_path, rent_rate_1day, rent_rate_2days, rent_rate_3days) VALUES
('Naruto Uzumaki Outfit', 'Anime', 'Naruto', 'M', 'Complete Naruto costume with headband', '/images/naruto.jpg', 500.00, 900.00, 1200.00),
('Sailor Moon Costume', 'Anime', 'Sailor Moon', 'S', 'Classic Sailor Moon transformation outfit', '/images/sailormoon.jpg', 600.00, 1000.00, 1400.00),
('Spider-Man Suit', 'Marvel', 'Spider-Man', 'L', 'Authentic-looking Spider-Man costume', '/images/spiderman.jpg', 800.00, 1400.00, 1900.00);

-- Sample Featured Images
INSERT OR IGNORE INTO featured_images (slot, image_url, title) VALUES
(1, '/images/featured1.jpg', 'Most Popular'),
(2, '/images/featured2.jpg', 'New Arrivals'),
(3, '/images/featured3.jpg', 'Best Value');

-- Sample Event Banner
INSERT OR IGNORE INTO event_banners (title, message, is_active, background_color, text_color, subtitle, image_path, event_name, venue, onsite_rent_date) VALUES
('Comic Con 2025', 'Special event rental rates available!', 1, '#fff4ed', '#d47f47', 'December Special', '/images/comiccon.jpg', 'Comic Con Manila', 'SMX Convention Center', 'December 20-22, 2025');
