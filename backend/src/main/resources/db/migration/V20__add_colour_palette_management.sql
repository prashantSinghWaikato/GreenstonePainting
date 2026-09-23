CREATE TABLE colour_palette_entries (
    id UUID PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    name VARCHAR(100) NOT NULL UNIQUE,
    hex_value VARCHAR(7) NOT NULL,
    resene_url VARCHAR(1000),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INTEGER NOT NULL,
    default_interior BOOLEAN NOT NULL DEFAULT FALSE,
    default_exterior BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_colour_display_order CHECK (display_order BETWEEN 1 AND 1000),
    CONSTRAINT chk_colour_hex_length CHECK (CHAR_LENGTH(hex_value) = 7)
);

CREATE INDEX idx_colour_palette_active_order ON colour_palette_entries (active, display_order);

INSERT INTO colour_palette_entries
    (id, version, name, hex_value, resene_url, active, display_order, default_interior, default_exterior, created_at, updated_at)
VALUES
    ('10000000-0000-0000-0000-000000000001', 0, 'Sea Fog', '#e9e7e3', 'https://www.resene.co.nz/swatches/preview.php?brand=Resene&chart=Resene%20special%20palette%20-%20Arrowtown&name=Sea%20Fog', TRUE, 1, TRUE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('10000000-0000-0000-0000-000000000002', 0, 'Thorndon Cream', '#dcd7c6', 'https://www.resene.co.nz/swatches/preview.php?chart=Resene%20Whites%20%26%20neutrals%20range%20%282010%29&brand=Resene&name=Thorndon%20Cream', TRUE, 2, FALSE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('10000000-0000-0000-0000-000000000003', 0, 'White Pointer', '#e1ddd7', 'https://www.resene.co.nz/swatches/preview.php?chart=Resene%20Whites%20%26%20neutrals%20range%20%282016%29&brand=Resene&name=White%20Pointer', TRUE, 3, FALSE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('10000000-0000-0000-0000-000000000004', 0, 'Black White', '#ebe9e5', 'https://www.resene.co.nz/swatches/preview.php?chart=Resene%20Whites%20%26%20neutrals%20range%20%282010%29&brand=Resene&name=Black%20White', TRUE, 4, FALSE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('10000000-0000-0000-0000-000000000005', 0, 'Lemon Grass', '#999a86', 'https://www.resene.co.nz/swatches/preview.php?chart=Resene%20special%20palette%20-%20Waikato%20Coastal&brand=Resene&name=Lemon%20Grass', TRUE, 5, FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('10000000-0000-0000-0000-000000000006', 0, 'Xanadu', '#75876e', NULL, TRUE, 6, FALSE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('10000000-0000-0000-0000-000000000007', 0, 'Patina', '#639283', NULL, TRUE, 7, FALSE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('10000000-0000-0000-0000-000000000008', 0, 'Stonewall', '#807661', NULL, TRUE, 8, FALSE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('10000000-0000-0000-0000-000000000009', 0, 'West Coast', '#5c512f', NULL, TRUE, 9, FALSE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('10000000-0000-0000-0000-000000000010', 0, 'Green Leaf', '#526b2d', NULL, TRUE, 10, FALSE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
