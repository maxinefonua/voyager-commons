#!/bin/bash

# Populates generic admin command file with env vars and saves output to target file.

# marks env vars for export
set -a
source .env
set +a

# creates directories if file doesn't yet exist
if ! [ -e ../target/upload/full-admin-sql.txt ]; then
  mkDir ../target
  mkDir ../target/upload
fi

# prints expanded admin command into target file substituting in env vars
# full sql statement with need env vars will be printed in target destination
# statement must be run by psql superuser to drop and create new table for airports upload
envsubst < postgres/airports-admin.sql | cat > ../target/upload/full-admin-sql.txt

echo "reached end of generate admin commands"