#!/bin/bash
sudo -u postgres psql $4 -c "SELECT *, $2 AS $3 FROM $1 ;"