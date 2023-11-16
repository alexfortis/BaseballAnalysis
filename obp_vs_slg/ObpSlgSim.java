import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

//all stats here are as of the end of the 2023 season
/**
 * This class simulates a 162-game baseball season for two different teams modeled after two different hitters.<br>
These hitters have either the same or very similar OPS, but vastly different OBP and SLG from each other.<br>
The purpose of this is to get a general idea of whether getting on base consistently or hitting for more power but less consistently is more important for scoring runs and winning.<br>
Rafael Devers (active) and Eddie Collins (HOF) both have an .853 career OPS, but Eddie Collins's OBP is .424 while Rafael Devers's is .343, which makes them a perfect pair.<br>
 */
public class ObpSlgSim {

    //the ratio of productive outs to total outs, currently the MLB league average in 2023
    //Sacrifice flies are counted among these, so in this simulation they count against batting average and slugging percentage.
    public static final double productiveOutRatio = 4456.0/16633;
    //the ratio of double plays to double play opportunities, the MLB league average in 2023
    public static final double doublePlayRatio = 3466.0/34097;
    //the ratio of infield singles to outfield singles, the MLB league average in 2023
    public static final double infieldHitRatio = 4480.0/26031;

    private static final boolean LOG_PAS = false;
    private static final boolean LOG_INNINGS = false;
    private static final boolean LOG_GAMES = false;
    
    private static class Player {

	private Random r;
	private int pa, k, oip, bbHbp, singles, doubles, triples, homers;
	public List<Integer> outcomes;
	private int[] thresholds;
	/**
	 * @param paIn The number of plate appearances
	 * @param kIn the number of strikeouts
	 * @param oipIn The number of outs in play: PA - BB - HBP - SO - H
	 * @param bbHbpIn The number of walks + hit-by-pitches
	 * @param singlesIn The number of singles
	 * @param doublesIn The number of doubles
	 * @param triplesIn The number of triples
	 * @param homersIn The number of home runs
	 */
	public Player(int paIn, int kIn, int oipIn, int bbHbpIn, int singlesIn, int doublesIn, int triplesIn, int homersIn) {
	    pa = paIn;
	    k = kIn;
	    oip = oipIn;
	    bbHbp = bbHbpIn;
	    singles = singlesIn;
	    doubles = doublesIn;
	    triples = triplesIn;
	    homers = homersIn;
	    thresholds = new int[7];
	    thresholds[0] = k;
	    thresholds[1] = thresholds[0] + oip;
	    thresholds[2] = thresholds[1] + bbHbp;
	    thresholds[3] = thresholds[2] + singles;
	    thresholds[4] = thresholds[3] + doubles;
	    thresholds[5] = thresholds[4] + triples;
	    thresholds[6] = thresholds[5] + homers;
	    r = new Random();
	    outcomes = new ArrayList<Integer>();
	}
	/**
	 * Copy constructor.
	 * @param other The player to copy
	 */
	public Player(Player other) {
	    pa = other.pa;
	    k = other.k;
	    oip = other.oip;
	    bbHbp = other.bbHbp;
	    singles = other.singles;
	    doubles = other.doubles;
	    triples = other.triples;
	    homers = other.homers;
	    thresholds = other.thresholds;
	    r = new Random();
	    outcomes = new ArrayList<Integer>();
	}

	/** Gets the result of a new plate appearance for this player based on pseudorandom number generation.
	 * @return The number corresponding to the result of a new plate appearance.
	 * 0: strikeout
	 * 1: out in play
	 * 2: walk or hit-by-pitch
	 * 3: single
	 * 4: double
	 * 5: triple
	 * 6: home run
	 */
	public int getPA() {
	    int outcome = r.nextInt(pa);
	    for(int i = 0; i < thresholds.length; i++) {
		if(outcome < thresholds[i]) {
		    outcomes.add(i);
		    return i;
		}
	    }
	    //this should never happen - if it does, ERROR!
	    return -1;
	}

	/**
	 * @return The unrounded on-base percentage of this player
	 */
	private double getRawOBP() {
	    List<Integer> timesOnBase = outcomes.stream().filter(oc -> oc > 1).collect(Collectors.toList());
	    return (double)timesOnBase.size()/outcomes.size();
	}

	/**
	 * @return The unrounded slugging percentage of this player
	 */
	private double getRawSLG() {
	    int tb = 0, ab = 0;
	    for(int oc : outcomes) {
		if(oc != 2) ab++;
		if(oc > 2) tb += oc-2;
	    }
	    return (double)tb/ab;
	}

	/**
	 * @return The on-base percentage of this player, rounded to the nearest thousandth
	 */
	public double getOBP() {
	    return (double)Math.round(1000 * getRawOBP())/1000;
	}

	/**
	 * @return the slugging percentage of this player, rounded to the nearest thousandth
	 */
	public double getSLG() {
	    return (double)Math.round(1000 * getRawSLG())/1000;
	}

	/**
	 * @return the on-base plus slugging percentage of this player, rounded to the nearest thousandth
	 */
	public double getOPS() {
	    return (double)Math.round(1000 * (getRawOBP() + getRawSLG()))/1000;
	}
    }

    private static class Team {
	public Player[] lineup;
	public int runs;
	public final String name;
	public int wins, losses;
	/**
	 * Creates a team with the given array of players and name.
	 * @param players The players in their lineup order.
	 * @param nameIn The name of the team.
	 */
	public Team(Player[] players, String nameIn) {
	    name = nameIn;
	    runs = 0;
	    lineup = players;
	}

	/**
	 * @return The number of total team plate appearances
	 */
	public int getPAs() {
	    int pas = 0;
	    for(Player p : lineup) {
		pas += p.outcomes.size();
	    }
	    return pas;
	}

	/**
	 * @return The number of total team at-bats
	 */
	public int getABs() {
	    int abs = 0;
	    for(Player p : lineup) {
		List<Integer> occs = p.outcomes.stream().filter(oc -> 2 != oc).collect(Collectors.toList());
		abs += occs.size();
	    }
	    return abs;
	}

	/**
	 * Gets the number of times a particular occurence occurred for a team (e.g. single, double, triple, home run)
	 * @return The number of times the given occurrence occurred for this team
	 */
	private int getNumOccurrences(int outcome) {
	    int count = 0;
	    for(Player p : lineup) {
		List<Integer> occs = p.outcomes.stream().filter(oc -> outcome == oc).collect(Collectors.toList());
		count += occs.size();
	    }
	    return count;
	}

	/**
	 * @return The number of collective strikeouts by this team
	 */
	public int getStrikeouts() {
	    return getNumOccurrences(0);
	}
	
	/**
	 * @return The number of collective outs in play by this team
	 */
	public int getOutsInPlay() {
	    return getNumOccurrences(1);
	}

	/**
	 * @return The number of collective walks plus hit-by-pitches by this team
	 */
	public int getBbHbp() {
	    return getNumOccurrences(2);
	}

	/**
	 * @return The number of collective singles by this team
	 */
	public int getSingles() {
	    return getNumOccurrences(3);
	}

	/**
	 * @return The number of collective doubles by this team
	 */
	public int getDoubles() {
	    return getNumOccurrences(4);
	}

	/**
	 * @return The number of collective triples by this team
	 */
	public int getTriples() {
	    return getNumOccurrences(5);
	}

	/**
	 * @return The number of collective home runs by this team
	 */
	public int getHomers() {
	    return getNumOccurrences(6);
	}

	/**
	 * @return The number of collective times this team has reached base safely
	 */
	public int getTimesOnBase() {
	    int count = 0;
	    for(Player p : lineup) {
		List<Integer> occs = p.outcomes.stream().filter(oc -> oc > 1).collect(Collectors.toList());
		count += occs.size();
	    }
	    return count;
	}

	/**
	 * @return The number of collective hits by this team
	 */
	public int getHits() {
	    int count = 0;
	    for(Player p : lineup) {
		List<Integer> occs = p.outcomes.stream().filter(oc -> oc > 2).collect(Collectors.toList());
		count += occs.size();
	    }
	    return count;
	}

	/**
	 * @return The number of collective total bases by this team
	 */
	public int getTotalBases() {
	    int tb = 0;
	    for(Player p : lineup) {
		List<Integer> hits = p.outcomes.stream().filter(oc -> oc > 2).collect(Collectors.toList());
		tb += hits.stream().reduce(0, (acc, val) -> acc + val - 2);
	    }
	    return tb;
	}

	/**
	 * @return The collective batting average of this team
	 */
	public double getBA() {
	    return (double)Math.round(1000 * (double)getHits()/getABs())/1000;
	}

	/**
	 * @return The collective on-base percentage of this team
	 */
	public double getOBP() {
	    return (double)Math.round(1000 * (double)getTimesOnBase()/getPAs())/1000;
	}

	/**
	 * @return The collective slugging percentage of this team
	 */
	public double getSLG() {
	    return (double)Math.round(1000 * (double)getTotalBases()/getABs())/1000;
	}

	/**
	 * @return A String representing the win-loss record of this team
	 */
	public String getWL() {
	    return wins + "-" + losses;
	}

	/**
	 * @return The winning percentage of this team (wins / (wins + losses)), rounded to the nearest thousandth
	 */
	public double getWPCT() {
	    double rawWpct = (double)wins/(wins+losses);
	    return (double) Math.round(rawWpct * 1000)/1000;
	}
    }

    //To represent the result of a simulated inning
    private static class InningResult {
	int runsScored, nextBatter;
	public InningResult(int runsScoredIn, int nextBatterIn) {
	    runsScored = runsScoredIn;
	    nextBatter = nextBatterIn;
	}
    }

    /**
     * Simulates an inning.
     * @param firstBatter The batter leading off this inning.
     * @param team The batting team.
     * @param canWalkOff Tells if there is a chance this inning could end without three outs being recorded. True iff the home team is batting in the 9th inning or later.
     * @param downBy How many runs the batting team is trailing by. Only relevant if canWalkOff is true.
     * @return An InningResult showing the number of runs scored in the inning and the batter leading off next inning.
     */
    private static InningResult simInning(int firstBatter, Player[] team, boolean canWalkOff, int downBy) {
	Random r = new Random();
	int outs = 0;
	int runs = 0;
	int curBatter = firstBatter % team.length;
	boolean[] baserunners = new boolean[]{false, false, false};
	while(outs < 3) {
	    if(LOG_PAS) {
		if(baserunners[0]) {
		    if(baserunners[1]) {
			if(baserunners[2]) {
			    System.out.print("Bases loaded, ");
			}
			else {
			    System.out.print("Runners at first and second, ");
			}
		    }
		    else {
			if(baserunners[2]) {
			    System.out.print("Runners at first and third, ");
			}
			else {
			    System.out.print("Runner at first, ");
			}
		    }
		}
		else {
		    if(baserunners[1]) {
			if(baserunners[2]) {
			    System.out.print("Runners at second and third, ");
			}
			else {
			    System.out.print("Runner at second, ");
			}
		    }
		    else {
			if(baserunners[2]) {
			    System.out.print("Runner at third, ");
			}
			else {
			    System.out.print("Bases empty, ");
			}
		    }
		}
		System.out.println(outs + " out");
		System.out.print("Batter #" + (curBatter+1) + ": ");
	    }
	    int outcome = team[curBatter].getPA();
	    switch(outcome) {
	    case 0: //strikeout
		outs++;
		if(LOG_PAS) {
		    System.out.print("Strikeout.");
		}
		break;
	    case 1: //out in play
		if(outs < 2) {
		    double prodOutRoll = r.nextDouble();
		    if(baserunners[0]) {
			if(prodOutRoll < doublePlayRatio) {
			    if(LOG_PAS) {
				System.out.print("Grounds into double play.");
			    }
			    //GIDP
			    outs++;
			    if(outs < 2) {
				if(baserunners[2]) {
				    if(LOG_PAS) {
					System.out.print(" Runner scores from third.");
				    }
				    runs++;
				}
				baserunners[2] = baserunners[1];
				baserunners[1] = false;
				baserunners[0] = false;
			    }
			}
			else if(prodOutRoll < doublePlayRatio + productiveOutRatio) {
			    if(LOG_PAS) {
				System.out.print("Productive out.");
			    }
			    //productive out; every runner moves up a base
			    if(baserunners[2]) {
				if(LOG_PAS) {
				    System.out.print(" Runner scores from third.");
				}
				runs++;
			    }
			    baserunners[2] = baserunners[1];
			    baserunners[1] = baserunners[0];
			    if(LOG_PAS) {
				if(baserunners[2]) {
				    System.out.print(" Runner advances from second to third.");
				}
				if(baserunners[1]) {
				    System.out.print(" Runner advances from first to second.");
				}
			    }
			    baserunners[0] = false;
			}
		    }
		    else {
			if(prodOutRoll < productiveOutRatio) {
			    if(LOG_PAS) {
				System.out.print("Productive out.");
			    }
			    //productive out; every runner moves up a base
			    if(baserunners[2]) {
				if(LOG_PAS) {
				    System.out.print(" Runner scores from third.");
				}
				runs++;
			    }
			    baserunners[2] = baserunners[1];
			    baserunners[1] = baserunners[0];
			    baserunners[0] = false;
			}
		    }
		}
		else {
		    if(LOG_PAS) {
			System.out.print("Out in play.");
		    }
		}
		outs++;
		if(canWalkOff && downBy < runs) {
		    if(LOG_PAS) {
			System.out.println(" Walk-off RBI!");
		    }
		    return new InningResult(runs, (curBatter + 1) % team.length + 1);
		}
		break;
	    case 2: //walk or hit by pitch
		if(LOG_PAS) {
		    System.out.print("Walk or hit-by-pitch.");
		}
		if(baserunners[0]) {
		    if(baserunners[1]) {
			if(baserunners[2]) {
			    runs++;
			    if(LOG_PAS) {
				System.out.print(" Runner scores from third.");
			    }
			}
			else {
			    baserunners[2] = true;
			}
			if(LOG_PAS) {
			    System.out.print(" Runner advances from second to third.");
			}
		    }
		    else {
			baserunners[1] = true;
		    }
		    if(LOG_PAS) {
			System.out.print(" Runner advances from first to second.");
		    }
		}
		else {
		    baserunners[0] = true;
		}
		if(canWalkOff && downBy < runs) {
		    if(LOG_PAS) {
			System.out.println(" Walk-off RBI!");
		    }
		    return new InningResult(runs, (curBatter + 1) % team.length + 1);
		}
		break;
	    case 3: //single
		if(LOG_PAS) {
		    System.out.print("Single.");
		}
		if(baserunners[2]) {
		    runs++;
		    if(LOG_PAS) {
			System.out.print(" Runner scores from third.");
		    }
		    if(canWalkOff && downBy < runs) {
			if(LOG_PAS) {
			    System.out.println(" Walk-off RBI!");
			}
			return new InningResult(runs, (curBatter + 1) % team.length + 1);
		    }
		}
		baserunners[2] = baserunners[1];
		baserunners[1] = baserunners[0];
		baserunners[0] = true;
		double infieldHitRoll = r.nextDouble();
		if(infieldHitRoll >= infieldHitRatio) {
		    //outfield single: score the runner originally on second if there is one
		    if(baserunners[2]) {
			if(LOG_PAS) {
			    System.out.print(" Runner scores from second.");
			}
			runs++;
			baserunners[2] = false;
			if(canWalkOff && downBy < runs) {
			    return new InningResult(runs, (curBatter + 1) % team.length + 1);
			}
		    }
		    if(baserunners[1] && LOG_PAS) {
			System.out.print(" Runner advances from first to second.");
		    }
		}
		else if(LOG_PAS) {
		    System.out.print(" Infield single.");
		    if(baserunners[2]) {
			System.out.print(" Runner advances from second to third.");
		    }
		    if(baserunners[1]) {
			System.out.print(" Runner advances from first to second.");
		    }
		}
		break;
	    case 4: //double
		if(LOG_PAS) {
		    System.out.print("Double.");
		}
		if(baserunners[2]) {
		    if(LOG_PAS) {
			System.out.print(" Runner scores from third.");
		    }
		    runs++;
		}
		if(baserunners[1]) {
		    if(LOG_PAS) {
			System.out.print(" Runner scores from second.");
		    }
		    runs++;
		}
		baserunners[2] = baserunners[0];
		if(LOG_PAS && baserunners[2]) {
		    System.out.print(" Runner advances from first to third.");
		}
		baserunners[1] = true;
		baserunners[0] = false;
		if(canWalkOff && downBy < runs) {
		    if(LOG_PAS) {
			System.out.println(" Walk-off RBI!");
		    }
		    return new InningResult(runs, (curBatter + 1) % team.length + 1);
		}
		break;
	    case 5: //triple
		if(LOG_PAS) {
		    System.out.print("Triple.");
		}
		if(baserunners[2]) {
		    if(LOG_PAS) {
			System.out.print(" Runner scores from third.");
		    }
		    runs++;
		}
		if(baserunners[1]) {
		    if(LOG_PAS) {
			System.out.print(" Runner scores from second.");
		    }
		    runs++;
		}
		if(baserunners[0]) {
		    if(LOG_PAS) {
			System.out.print(" Runner scores from first.");
		    }
		    runs++;
		}
		baserunners[2] = true;
		baserunners[1] = false;
		baserunners[0] = false;
		if(canWalkOff && downBy < runs) {
		    if(LOG_PAS) {
			System.out.println(" Walk-off RBI!");
		    }
		    return new InningResult(runs, (curBatter + 1) % team.length + 1);
		}
		break;
	    case 6: //home run
		if(LOG_PAS) {
		    System.out.print("Home run.");
		}
		if(baserunners[2]) {
		    if(LOG_PAS) {
			System.out.print(" Runner scores from third.");
		    }
		    runs++;
		}
		if(baserunners[1]) {
		    if(LOG_PAS) {
			System.out.print(" Runner scores from second.");
		    }
		    runs++;
		}
		if(baserunners[0]) {
		    if(LOG_PAS) {
			System.out.print(" Runner scores from first.");
		    }
		    runs++;
		}
		runs++;
		baserunners[2] = false;
		baserunners[1] = false;
		baserunners[0] = false;
		if(canWalkOff && downBy < runs) {
		    if(LOG_PAS) {
			System.out.println(" Walk-off RBI!");
		    }
		    return new InningResult(runs, (curBatter + 1) % team.length + 1);
		}
		break;
	    default: //should never happen
		if(LOG_PAS) {
		    System.out.println("ERROR: Unknown PA outcome " + outcome);
		}
		throw new RuntimeException("ERROR: Unknown PA outcome " + outcome);
	    }
	    curBatter = (curBatter + 1) % team.length;
	    if(LOG_PAS) {
		System.out.println(" Next up: " + (curBatter + 1));
	    }
	}
	if(LOG_INNINGS || LOG_PAS) {
	    System.out.println("End of the inning. " + runs + " run(s) scored.");
	}
	return new InningResult(runs, curBatter);
    }

    /**
     * Simulates a game between two teams.
     * @param awayTeam The away team (batting first).
     * @param homeTeam The home team (batting second).
     * @return An array containing the number of runs scored by the away team, the number of runs scored by the home team, and the number of innings played (if not 9).
     */
    static int[] playGame(Team awayTeam, Team homeTeam) {
	Player[] away = awayTeam.lineup, home = homeTeam.lineup;
	int awayTeamRuns = 0, homeTeamRuns = 0;
	int awayTeamBatter = 0, homeTeamBatter = 0;
	int inning = 1;
	for(; inning < 9; inning++) {
	    if(LOG_INNINGS) {
		System.out.println("Score: " + awayTeamRuns + " - " + homeTeamRuns);
		System.out.println("Top " + inning + ":");
	    }
	    InningResult top = simInning(awayTeamBatter, away, false, homeTeamRuns - awayTeamRuns);
	    awayTeamRuns += top.runsScored;
	    awayTeamBatter = top.nextBatter;
	    if(LOG_INNINGS) {
		System.out.println("Score: " + awayTeamRuns + " - " + homeTeamRuns);
		System.out.println("Bottom " + inning + ":");
	    }
	    InningResult bottom = simInning(homeTeamBatter, home, false, awayTeamRuns - homeTeamRuns);
	    homeTeamRuns += bottom.runsScored;
	    homeTeamBatter = bottom.nextBatter;
	}
	do {
	    if(LOG_INNINGS) {
		System.out.println("Score: " + awayTeamRuns + " - " + homeTeamRuns);
		System.out.println("Top " + inning + ":");
	    }
	    InningResult top = simInning(awayTeamBatter, away, false, homeTeamRuns - awayTeamRuns);
	    awayTeamRuns += top.runsScored;
	    awayTeamBatter = top.nextBatter;
	    if(awayTeamRuns >= homeTeamRuns) {
		if(LOG_INNINGS) {
		    System.out.println("Score: " + awayTeamRuns + " - " + homeTeamRuns);
		    System.out.println("Bottom " + inning + ":");
		}
		InningResult bottom = simInning(homeTeamBatter, home, true, awayTeamRuns - homeTeamRuns);
		homeTeamRuns += bottom.runsScored;
		homeTeamBatter = bottom.nextBatter;
	    }
	    inning++;
	} while(awayTeamRuns == homeTeamRuns);
	if(LOG_INNINGS || LOG_GAMES) {
	    System.out.println("Final score: " + awayTeam.name + " " + awayTeamRuns + " - " + homeTeamRuns + " " + homeTeam.name + ((inning > 10)?(" in " + (inning-1) + " innings"):("")));
	}
	awayTeam.runs += awayTeamRuns;
	homeTeam.runs += homeTeamRuns;
	if(awayTeamRuns > homeTeamRuns) {
	    awayTeam.wins++;
	    homeTeam.losses++;
	}
	else {
	    awayTeam.losses++;
	    homeTeam.wins++;
	}
	if(inning > 10) {
	    return new int[]{awayTeamRuns, homeTeamRuns, inning-1};
	}
	return new int[]{awayTeamRuns, homeTeamRuns};
    }

    public static void main(String[] args) {
	//Eddie Collins, Rafael Devers
	Player p1 = new Player(12087, 467, 6729, 1576, 2643, 438, 187, 47),
	    p2 = new Player(3614, 747, 1626, 322, 519, 221, 7, 172);
	Player[] t1 = new Player[9], t2 = new Player[9];
	for(int i = 0; i < 9; i++) {
	    t1[i] = new Player(p1);
	    t2[i] = new Player(p2);
	}
	Team highObp = new Team(t1, "High OBP"), highSlg = new Team(t2, "High SLG");
	//playGame(highObp, highSlg);
	//alternate which team is at home every 3 games over the 162 game season
	int gameNumber = 1;
	for(int i = 0; i < 54; i++) {
	    for(int j = 0; j < 3; j++) {
		if(LOG_GAMES) {
		    System.out.print("Game #" + gameNumber + ": ");
		}
		if(0 == i%2) {
		    int[] score = playGame(highObp, highSlg);
		}
		else {
		    int[] score = playGame(highSlg, highObp);
		}
		gameNumber++;
	    }
	}
	if(LOG_GAMES) {
	    int seasonLength = gameNumber-1;
	    System.out.println("The " + seasonLength + "-game season has concluded.");
	}
	System.out.println("Records:");
	System.out.println("Team High OBP: " + highObp.getWL() + " (" + highObp.getWPCT() + ")");
	System.out.println("Team High SLG: " + highSlg.getWL() + " (" + highSlg.getWPCT() + ")");

	System.out.println();
	System.out.println("Batting stats:");
	double highObpOBP = highObp.getOBP(), highObpSLG = highObp.getSLG(), highSlgOBP = highSlg.getOBP(), highSlgSLG = highSlg.getSLG();
	System.out.println("Team High OBP: " + highObp.getBA() + "/" + highObpOBP + "/" + highObpSLG + " (" + (highObpOBP + highObpSLG) + " OPS), " + highObp.runs + " runs scored");
	System.out.println("Team High SLG: " + highSlg.getBA() + "/" + highSlgOBP + "/" + highSlgSLG + " (" + (highSlgOBP + highSlgSLG) + " OPS), " + highSlg.runs + " runs scored");
    }
    
}
