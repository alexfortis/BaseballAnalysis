#!/bin/bash

# First determine whether it needs to be recompiled

needs_compiling=0
if [ -f ObpSlgSim.class ]; then
    # when the source code was last edited
    source_date=$(date -r ObpSlgSim.java +%Y%j%H%M%S)
    # when it was last compiled
    compile_date=$(date -r ObpSlgSim.class +%Y%j%H%M%S)
    # now do the comparison
    if [[ $compile_date < $source_date ]]; then
	needs_compiling=1
    fi
else
    needs_compiling=1
fi
# now do the compiling if necessary
if [ $needs_compiling -eq 1 ]; then
    javac ObpSlgSim.java
fi

# now run it however many times the user wants
# if not specified, do 100
num_seasons=100
if [[ $# -gt 0 ]]; then
    num_seasons=$1
fi
for n in $(seq 1 $num_seasons)
do
    echo "Season ${n}:"
    java ObpSlgSim
    echo ""
done
