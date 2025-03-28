#!/bin/bash

# Check psql installed, required upload file exists, then upload.

# exits if psql not installed
if ! command -v psql > /dev/null ; then
  echo "FAILURE: script requires psql to be installed and on your PATH. Exiting"
  exit 1
fi

# marks env vars for export
set -a
source .env
set +a

# check for required file
if ! test -f $UPLOAD_SOURCE_FILE; then
  echo "FAILURE: required file '$UPLOAD_SOURCE_FILE' does not exist. Cannot complete upload without upload data file. Exiting"
  exit 1
fi

# load database connection info
set -o allexport
set +o allexport

# expands generic upload command with env vars, saves to temporary file
envsubst < postgres/airports-upload.sql | cat > tmp-expanded-upload.sql

# connect to the database, run the expanded file, then disconnect
psql $PGDATABASE -U $AIRPORT_DATASYNC_USER -f tmp-expanded-upload.sql

# removes temporary file
rm tmp-expanded-upload.sql

echo "reached end of upload script"