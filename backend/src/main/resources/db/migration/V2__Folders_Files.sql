CREATE TABLE folders (
  id INTEGER NOT NULL,
  userId VARCHAR(255) NOT NULL,
  name VARCHAR(1024) NOT NULL,
  parentFolder VARCHAR(255),
  PRIMARY KEY(id),
  FOREIGN KEY (userId) REFERENCES users(id),
  FOREIGN KEY (parentFolder) REFERENCES folders(id)
);

CREATE TABLE files (
  id INTEGER NOT NULL,
  userId VARCHAR(255) NOT NULL,
  name VARCHAR(1024) NOT NULL,
  mimeType VARCHAR(255),
  folder INTEGER,
  PRIMARY KEY (id),
  FOREIGN KEY (userId) REFERENCES users(id),
  FOREIGN KEY (folder) REFERENCES folders(id)
);