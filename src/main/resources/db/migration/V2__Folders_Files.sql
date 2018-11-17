CREATE TABLE folders (
  id VARCHAR(255) NOT NULL,
  name VARCHAR(1024) NOT NULL,
  parentFolder VARCHAR(255),
  PRIMARY KEY(id),
  FOREIGN KEY (parentFolder) REFERENCES folders(id)
);

CREATE TABLE files (
  id VARCHAR(255) NOT NULL,
  name VARCHAR(1024),
  mimeType VARCHAR(255),
  folder VARCHAR(255),
  PRIMARY KEY (id),
  FOREIGN KEY (folder) REFERENCES folders(id)
);