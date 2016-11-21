user=asap
password=apassword124

echo "create user $user password '$password';" | psql -U postgres
echo "alter user $user with createdb" | psql -U postgres
echo "create database $user" | psql -U $user postgres
