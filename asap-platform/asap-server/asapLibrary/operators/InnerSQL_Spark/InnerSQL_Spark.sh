#!/bin/bash

SQL_QUERY=$1
DATABASE=$2

sudo -u postgres psql -c $DATABASE -f $SQL_QUERY
