#!/bin/bash
sudo -u postgres psql $5 -c "DROP TABLE $3; "
sudo -u postgres psql $5 -c "CREATE TABLE $3 AS SELECT * FROM $1 LEFT JOIN $2 ON $4 ;"