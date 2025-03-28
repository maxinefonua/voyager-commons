-- print a confirming message to see that env var was set properly
\echo uploading from file in env var UPLOAD_SOURCE_FILE set to $UPLOAD_SOURCE_FILE;
-- copy table from local file with options determined by data being uploaded
\copy public.airports_copy FROM $UPLOAD_SOURCE_FILE WITH ENCODING 'UTF8' HEADER DELIMITER ',' QUOTE '"' CSV;