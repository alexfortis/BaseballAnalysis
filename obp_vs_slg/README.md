# OBP vs. SLG Simulator

## The Goal

To get an idea of whether hitters should be valued for getting on base more often or hitting for more power. Obviously the very best can do both, but not everyone can, which is why those who can are the best. Players can't all hit like Ted Williams, coaches can't get all their hitters to hit like Ted Williams, and GMs can't put together teams of Ted Williamses to get on base and hit for power consistently. So they have to prioritize: should they care more about getting on base or hitting for power?

## Usage

If you just want a summary of the data, see [the Results section](#results).

If you want the actual raw data, look at the files results.txt and results.csv. The TXT file uses English and is in a more human readable format, but the CSV is easier to process programmatically and analyze the data for a large number of simulations.

If you want to run it for yourself, you can download the Java file and compile and run it with a recent version of the JDK (anything Java 8 or later should work, but I haven't tested it extensively). The bash scripts provided make running it multiple times and gathering/analyzing data more convenient, but they are not necessary to run the simulator.

The bash scripts read from stdin and write to stdout. When running them in a *nix terminal, use `<` and `>` to redirect input from and output to a file respectively, or you can use `|` to pipe the output of one command to another. What I did to get matching results.txt and results.csv was the following:
```
./run_sim.sh 5000 > results.txt
./make_csv.sh < results.txt > results.csv
```
But I could have gotten just the CSV with this:
```
./run_sim.sh 5000 | ./make_csv.sh > results.csv
```
If you don't specify a number of times to run the simulation, it will run 100 times.

## Methods

### Algorithm

This program simulates each game, play by play, over the course of a whole season. For simplicity, assume no errors, since hitters shouldn't be trying to reach on errors anyway, but I might look into adding it later if it's requested. The same goes for fielder's choices - they're more complicated to program/simulate in this fashion than the basic outcomes I put in. I used Java to write the main code and Bash scripts to run it many times in quick succession and gather all the data.

The simulator takes two predetermined players ([more on them later](#choosing-the-players)), creates lineups full of them, has them play each other 162 times, simulating each individual play of a baseball game. Most outcomes that are a direct result of batting are covered: strikeout, out in play (which is sometimes a productive out but sometimes a double play), walk/HBP, infield single, outfield single, double, triple, home run. For the sake of simplicity, sacrifice flies are counted as "productive outs" and thus decrease batting average and slugging percentage, unlike in real life. I may go back and change this if other people request it, but personally I don't think it makes that much of a difference.

There are a few constants in the code: the productive out ratio, double play ratio, and the infield hit ratio. These are the MLB team averages in the 2023 season, from baseball-reference ([here](https://www.baseball-reference.com/leagues/majors/2023.shtml) and [here](https://www.baseball-reference.com/leagues/majors/2023-situational-batting.shtml)).

### Choosing the players

Note: All stats are as of the end of the 2023 MLB season.

A good pair of players meets the following criteria:
- Both have at least 3000 PA in MLB
- Both have the same OPS (at least rounded to the nearest thousandth, following tradition)
- The OPS they share is pretty good (ideally .850 or better)
- They have drastically different OBPs and SLGs

To find a good pair, I went on Stathead. First, I found [the players with an OPS at least .850 and an OBP at most .350](https://stathead.com/tiny/SDwpT):
- Nelson Cruz .274/.343/.513 (.856 OPS)
- Nolan Arenado .286/.343/.527 (.871 OPS)
- Juan González .295/.343/.561 (.904 OPS)
- Rafael Devers .280/.343/.510 (.853 OPS)
- Ryan Howard .258/.343/.515 (.859 OPS)
- Richie Sexson .273/.344/.507 (.851 OPS)
- Sammy Sosa .273/.344/.534 (.878 OPS)
- Giancarlo Stanton .259/.349/.529 (.878 OPS)
- Josh Hamilton .290/.349/.516 (.865 OPS)
- J.D. Martinez .287/.350/.524 (.874 OPS)

Then I tried to match those OPSes with players who had much higher OBPs (and, as a result, much lower SLGs) than the aforementioned players. Each player had his own set of matches:
- [Nelson Cruz .856](https://stathead.com/tiny/QaXjp) - best match is Branch Russell (.309/.391/.465)
- [Nolan Arenado .871](https://stathead.com/tiny/yapqh) - best match is Bobby Abreu (.291/.395/.475), even though the OPS is off by .001
- [Juan González .904](https://stathead.com/tiny/zeNUL) - only match is Jim Edmonds (.284/.376/.527), so no good match
- [Rafael Devers .853](https://stathead.com/tiny/qYstU) - best match by far is Eddie Collins (.333/.424/.429)
- [Ryan Howard .859](https://stathead.com/tiny/Nd1XX) - best match is Wade Boggs (.328/.415/.443), despite the OPS being .001 off, or Arky Vaughan (.318/.406/.453)
- [Richie Sexson .851](https://stathead.com/tiny/14zPW) - best match by far is Denny Lyons (.310/.407/.442)
- [Sammy Sosa and Giancarlo Stanton .878](https://stathead.com/tiny/ST81v) - best match is Paul Waner (.333/.404/.473)
- [Josh Hamilton .865](https://stathead.com/tiny/eMNkf) - best match is Ed Morgan (.313/.398/.467) even though the OPS is off by .001
- [J.D. Martinez .874](https://stathead.com/tiny/RFPFn) - best match is Jack Fournier (.313/.392/.483)

Luckily, there are a few here with an OBP of at least .400: Eddie Collins (.424), Wade Boggs (.415), Arky Vaughan (.406), Denny Lyons (.407), and Paul Waner (.404). Based on the sheer difference between the two, I determined that Devers and Collins make the best pairing. So the simulated matchup is between a team of Rafael Deverses and a team of Eddie Collinses.

## Results

In 5000 seasons, the following results were observed:
- 222 resulted in each team winning 81 games.
- 841 resulted in Team Devers winning at least 82 games.
- The other 3937 resulted in Team Collins winning at least 82 games.
- The fewest runs scored by either team in a single season was 790.
- Team Devers won at least 90 games 67 times, at least 92 games 26 times, at least 95 games 9 times, and at least 100 games 1 time (max 101).
- Team Collins won at least 90 games 1657 times, at least 92 games 1103 times, at least 95 games 567 gimes, at least 100 games 115 times, at least 102 games 48 times, and had a maximum of 108 wins.
- The median season had Team Collins winning 87 games and Team Devers winning 75.
- The average season had Team Collins winning 86.662 games and Team Devers winning 75.338, which round to 87 and 75, the same as the median.
- Team Devers scored under 800 runs 4 times, under 850 runs 165 times, under 900 runs 1187 times, at least 950 runs 1720 times, at least 1000 runs 311 times, at least 1050 runs 27 times, and had a maximum of 1097 runs in one season.
- Team Collins had a minimum of 843 runs scored, scored under 850 runs 3 times, under 900 runs 55 times, under 950 runs 517 times, under 1000 runs 2076 times, at least 1050 runs 1028 times, more than 1097 runs 210 times, at least 1100 runs 187 times, at least 1150 runs 15 times, and a maximum of 1187 runs in one season.
- The median season for Team Collins had them scoring 1010 runs.
- The median season for Team Devers had them scoring 932 runs.
- On average, Team Collins scored 1010.3726 runs per season or 6.24 per game, while Team Devers scored 931.8834 runs per season or 5.75 per game. Rounded to the nearest integer, these are both the same as their teams' respective medians.

The results here seem to overwhelmingly imply that consistently getting on base is more important than hitting for power every now and then.

