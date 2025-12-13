-- Add title_color and subtitle_color columns to event_banners table

ALTER TABLE event_banners ADD COLUMN title_color TEXT DEFAULT '#FFFFFF';
ALTER TABLE event_banners ADD COLUMN subtitle_color TEXT DEFAULT '#FFFFFF';
