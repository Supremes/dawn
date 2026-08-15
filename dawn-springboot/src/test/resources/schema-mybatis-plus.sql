DROP TABLE IF EXISTS t_mp_feature_record;

CREATE TABLE t_mp_feature_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    status INT NOT NULL DEFAULT 0,
    version INT NOT NULL DEFAULT 0,
    deleted TINYINT NOT NULL DEFAULT 0,
    create_time TIMESTAMP NOT NULL,
    update_time TIMESTAMP NULL
);

