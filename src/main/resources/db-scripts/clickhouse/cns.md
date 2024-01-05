# Load CNS Data

This to be done after the tables have been created in ClickHouse.

## Download from MySQL database into file called mutation.csv
```

```


## Split File

```
cat cns.csv | parallel --header : --pipe -N99999 'cat > cns_{#}.csv' 
```

## Create Load Script

Edit and run as needed.

```
# Where 95 is the number of splits
for number in range(0, 95):
   print(f"./clickhouse client -h localhost --port 19000 -q \"INSERT INTO cbioportal.copy_number_seg FORMAT CSV\" < sql_query/cns/cns_{number}.csv")
```

## Run Script

```
python3 load_data.py > load_cns.sh
chmod u+x load_mutation.sh
./load_mutation.sh
```

Notes: This took less than 10 minutes to run with 9.7 million entries in the CSV
