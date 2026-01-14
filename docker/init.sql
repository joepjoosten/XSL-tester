-- XSL-Tester Database Initialization

USE fiddles;

-- Create fiddle table
CREATE TABLE IF NOT EXISTS fiddle (
    id BIGINT AUTO_INCREMENT PRIMARY KEY
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create fiddle_revision table
CREATE TABLE IF NOT EXISTS fiddle_revision (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fiddle_id BIGINT NOT NULL,
    revision INT NOT NULL,
    engine VARCHAR(50),
    xml TEXT,
    xsl TEXT,
    FOREIGN KEY (fiddle_id) REFERENCES fiddle(id) ON DELETE CASCADE,
    INDEX idx_fiddle_revision (fiddle_id, revision)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Grant permissions
GRANT ALL PRIVILEGES ON fiddles.* TO 'xsltester'@'%';
FLUSH PRIVILEGES;
