#!/bin/bash

# Get input from stdin. Can use "<" operator in terminal to get input from file.
# Write to stdout. Can use ">" operator in terminal to redirect output.
echo "Season,High OBP Wins,High SLG Wins,High OBP BA,High OBP OBP,High OBP SLG,High OBP OPS,High OBP R,High SLG BA,High SLG OBP,High SLG SLG,High SLG OPS,High SLG R"
while read -d ":" line season
do
    read line
    read line
    read -d ":" team
    read -d "-" obp_w
    read line
    read -d ":" team
    read -d "-" slg_w
    read line
    read line
    read line
    read -d ":"
    read -d "/" obp_ba
    read -d "/" obp_obp
    read -d " " obp_slg
    read -d "("
    read -d " " -n 5 obp_ops
    read -d ","
    read obp_runs line
    read -d ":"
    read -d "/" slg_ba
    read -d "/" slg_obp
    read -d " " slg_slg
    read -d "("
    read -d " " -n 5 slg_ops
    read -d ","
    read slg_runs line
    echo "${season},${obp_w},${slg_w},${obp_ba},${obp_obp},${obp_slg},${obp_ops},${obp_runs},${slg_ba},${slg_obp},${slg_slg},${slg_ops},${slg_runs}"
done
