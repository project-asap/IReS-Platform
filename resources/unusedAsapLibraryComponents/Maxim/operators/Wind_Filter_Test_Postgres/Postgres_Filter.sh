#!/bin/bash
sudo -u postgres psql $3 -c "SELECT * FROM $1 WHERE $2 ;"