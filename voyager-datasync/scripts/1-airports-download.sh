#!/bin/bash

# If airports data file exists to exit, else downloads to target directory

# marks env vars for export
set -a
source .env
set +a


if [ -e $TARGET_FILE_WITH_PATH ]; then
  echo "WARNING: $TARGET_FILE_WITH_PATH exists - remove file for new download"

else

  # creates target directory if missing
  if ! [ -d $TARGET_DIR ];  then
    mkdir $TARGET_DIR
  fi

  echo "downloading airport data from given source"
  cd $TARGET_DIR
  #  download command, using nonverbose flag
  wget -O $TARGET_FILE_NAME -nv $SOURCE_URL
  cd $SCRIPT_DIR

fi

echo "reached end of upload script"