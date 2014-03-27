-- This file contains the SQL for creating the necessary tables

-- Table avlReport. 
-- Contains position reports for a transit vehicle
CREATE  TABLE avlReport (
  -- GPS time when report was generate. Need to explicity state that
  -- should use 3 digits after the decimal point so that miliseconds
  -- stored when using MySQL. Not null because part of primary key.
  time TIMESTAMP(3) NOT NULL,
  
  -- Time when GPS report actually processed. There will always be a 
  -- delay between the time the GPS report is generated on the vehicle
  -- and the time it is actually processed.
  timeProcessed TIMESTAMP(3) NOT NULL,
  
  -- Identifier for vehicle. Part of primary key so must not be null.
  -- Usually the identifier is just a few characters but made up to
  -- 45 chars long so can handle special names.
  vehicleId VARCHAR(45) NOT NULL ,
  
  -- latitude of vehicle. Will always be set so must not be null.
  lat DOUBLE NOT NULL ,

  -- longitude of vehicle. Will always be set so must not be null.
  lon DOUBLE NOT NULL ,
  
  -- Block assignment of vehicle. Often this will not be provided
  -- as part of the AVL feed so can be null.
  block VARCHAR(45) NULL DEFAULT NULL ,
  
  -- Speed in m/s of vehicle. Sometimes not provided in AVL feed so
  -- can be null.
  speed FLOAT NULL DEFAULT NULL ,
  
  -- Heading of vehicle in degrees from north. Sometimes not provided
  -- in AVL feed so can be null.
  heading FLOAT NULL DEFAULT NULL ,
  
  -- Two vehicles might report at the same time. Therefore time alone
  -- is not a sufficient primary key. Also need to use vehicleId.
  -- Wanted to also use block as part of the key because in theory a
  -- vehicle might send two simultaneous reports, one indicating the
  -- old, possibly null, assignment and the other indicating the new
  -- assignment. But often block will not be specified in feed so can
  -- be null, and nulls cannot be used in primary key. But hopefully
  -- block won't actualy be needed in the primary key.
  PRIMARY KEY (time, vehicleId) 
);

-- The index automaticaly created as part of the primary key simply
-- needs to make sure each object is unique. But since for avlReport
-- the primary key is one two columns, time & vehiceId, the index
-- will likely simply be a concatination of the two column values.
-- But that won't be sufficient for quickly finding data from a 
-- particular time range. For that need to create a separate key
-- just on the time column. 
--
-- In future might want an additional key (vehicleId, time) so that
-- can look at AVL reports for a particuar vehicle really quickly.
-- But until such a need is demonstrated we are not adding a
-- possibly superfluous and expensive index.
CREATE INDEX avlReport_index ON avlReport(time);