 -- EatSafe 
 -- create tables

    -- drop tables first
DROP TABLE IF EXISTS location CASCADE;
DROP TABLE IF EXISTS inspection CASCADE;
DROP TABLE IF EXISTS violation CASCADE;
DROP TABLE IF EXISTS violation_type CASCADE;


CREATE TABLE location(
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    address TEXT NOT NULL,
    postcode TEXT NOT NULL,
    city TEXT NOT NULL,
    rha TEXT NOT NULL
);

  
CREATE TABLE inspection(
    id SERIAL PRIMARY KEY,
    location_id INT NOT NULL,
    inspection_date DATE NOT NULL,
    inspection_type TEXT NOT NULL,
    reinspection_priority TEXT NOT NULL,
    FOREIGN KEY (location_id)
        REFERENCES location
            ON UPDATE NO ACTION
            ON DELETE NO ACTION
);


CREATE TABLE violation_type(
    id INT NOT NULL PRIMARY KEY,
    description TEXT NOT NULL,
    priority TEXT NOT NULL
);


CREATE TABLE violation(
    inspection_id INT NOT NULL,
    violation_id INT NOT NULL,
    PRIMARY KEY (inspection_id, violation_id),
    FOREIGN KEY (inspection_id)
        REFERENCES inspection
            ON UPDATE NO ACTION
            ON DELETE NO ACTION,
    FOREIGN KEY (violation_Id)
        REFERENCES violation_type
            ON UPDATE NO ACTION
            ON DELETE NO ACTION
);
