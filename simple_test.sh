#!/bin/bash

DATA="{\"params\": [1,2], \"optimizationId\": $2}"
SECONDS=0

for (( i=1; i<=$1; i++ ))
do  
   curl -H "Content-Type: application/json" -X POST -d "$DATA" http://localhost:8080/run && echo "Done $i" &
done
wait

echo $SECONDS
