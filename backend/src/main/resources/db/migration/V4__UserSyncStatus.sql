CREATE TABLE userSyncStatus (
  userId VARCHAR(255),
  nextLink VARCHAR(255),
  PRIMARY KEY(userId),
  FOREIGN KEY (userId) REFERENCES users(id)
);