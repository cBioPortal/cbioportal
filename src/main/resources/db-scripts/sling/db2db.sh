# Copies entire source database contents (i.e: all tables) to target database by utilizing sling
# Visit https://docs.slingdata.io/sling-cli/getting-started#installation for sling installation instructions 

# example script execution: 
#    bash db2db.sh --src-conn MYSQL_GENIE --tgt-conn CLICKHOUSE_GENIE --mode truncate --src-db db_2024_04_16 --tgt-db sling_db_2024_04_16
# example source mysql connection:
#    sling conns set MYSQL_GENIE type=mysql host=rfc80db.cbioportal.org user=cbio_user database=db_2024_04_16 password=somepassword port=3306  
# example target clickhouse connection:
#    sling conns set CLICKHOUSE_GENIE type=clickhouse http_url="https://default:somepassword@iurb556cp4.us-east-1.aws.clickhouse.cloud:8443/sling_db_2024_04_16?secure=true"

# parsing the arguments to get --src-db, --tgt-db, and --src-conn
# this is a simple parser assuming that the arguments are followed by an actual value
# we do not actually check the arguments for error  
opts=($(echo $@));
readyToReceiveArgument='_READY_' # we need this workaround since the value of the argument is right after the name of the argument
for opt in "${opts[@]}"
do
  [ "$sourceConn" == "$readyToReceiveArgument" ] && sourceConn=$opt
  [ "$opt" == "--src-conn" ] && sourceConn=$readyToReceiveArgument
  [ "$sourceDb" == "$readyToReceiveArgument" ] && sourceDb=$opt
  [ "$opt" == "--src-db" ] && sourceDb=$readyToReceiveArgument
  [ "$targetDb" == "$readyToReceiveArgument" ] && targetDb=$opt
  [ "$opt" == "--tgt-db" ] && targetDb=$readyToReceiveArgument
done

#TODO exit if the sourceConn, sourceDb, or targetDb is empty?

# get the table names from the source database and create an array
query_result=$(sling run --src-conn $sourceConn --src-stream "SELECT table_name FROM information_schema.tables WHERE table_schema='$sourceDb'" --stdout)
tables=($(echo ${query_result#"table_name"}))

# for each table execute the sling command to copy data from source to target 
for tableName in "${tables[@]}"
do
  # sling run --src-conn $sourceConn --src-stream $sourceDb.$tableName --tgt-conn $targetConn --tgt-object $targetDb.$tableName --mode $mode
  # pass rest of the arguments to sling
  sling run --src-stream $sourceDb.$tableName --tgt-object $targetDb.$tableName "$@"
done
