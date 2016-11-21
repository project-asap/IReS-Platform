pg_location="/etc/postgresql/9.3/main"
admin_password=mypassword

sudo apt-get install postgresql-9.3 pgadmin3
pg_createcluster 9.3 main --start



#trust local connections
sed -i "s/local   all             all                                     peer/local   all         all                               trust/g" -i "$pg_location/pg_hba.conf"

sed -i "s/local   all             postgres                                peer/local   all             postgres                                trust/g" -i "$pg_location/pg_hba.conf"
#listen to global connections
sed -i "s/.*listen_addresses = .*/listen_addresses = \'*'/g" "$pg_location/postgresql.conf"

#allow remote IPv4 connections
echo "host    all             all             ::/0                 md5" >>"$pg_location/pg_hba.conf"
echo "host    all             all             0.0.0.0/0            md5" >>"$pg_location/pg_hba.conf"


#change the admin admin_password
echo "alter user postgres password '$admin_password';" | psql -U postgres
