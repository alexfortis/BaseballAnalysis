import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Comparator;
import java.util.Collection;
import java.util.stream.Collectors;

public class GetSmallestCoverageOfAllTeams {
    private static String hitterFileName = "journeyman_hitters.csv",
	pitcherFileName = "journeyman_pitchers.csv";

    private static class PlayerComparator implements Comparator<Player> {
	private Collection<Team> teamsToExclude;
	private boolean reverse;
	public PlayerComparator(Collection<Team> teamsToExcludeIn) {
	    this(teamsToExcludeIn, false);
	}
	public PlayerComparator(Collection<Team> teamsToExcludeIn, boolean reverseIn) {
	    teamsToExclude = teamsToExcludeIn;
	    reverse = reverseIn;
	}
	public int compare(Player p1, Player p2) {
	    int teamCountDiff = p1.teamsExcept(teamsToExclude).size() - p2.teamsExcept(teamsToExclude).size();
	    if(0 == teamCountDiff) {
		return reverse ? p2.compareTo(p1) : p1.compareTo(p2);
	    }
	    return reverse ? -teamCountDiff : teamCountDiff;
	}
    }

    private static final PlayerComparator REVERSE_TEAM_COUNT = new PlayerComparator(new ArrayList<Team>(), true);

    public static List<SortedSet<Player>> getSmallestSets(SortedSet<Player> using, SortedSet<Player> available, int minSizeKnown) {
	if(available.size()>0 && using.size() < minSizeKnown) {
	    System.err.print("Setting player #" + (using.size()+1) + " to " + available.last() + " (" + available.last().teams.size() + " teams)");
	}
	Set<Team> coveredTeams = Player.getTeamsByPlayers(using);
	//if there's no chance of at least tying the smallest full coverage group, backtrack
	//condition 1: already using minSizeKnown players, still haven't covered all 30 teams
	if((using.size() >= minSizeKnown && coveredTeams.size() < Team.getNumFranchises()) ||
	   //condition 2: no player remaining has played for any uncovered teams
	   available.last().teams.size() == 0 ||
	   //condition 3: the number of remaining player slots times the number of teams of the max available player is less than the number of teams remaining to cover
	   ((minSizeKnown - using.size()) * available.last().teamsExcept(coveredTeams).size() + coveredTeams.size() < Team.getNumFranchises())) {
	    return new ArrayList<SortedSet<Player>>();
	}
	if(coveredTeams.size() > Team.getNumFranchises()) {
	    System.err.println("Impossible! You covered " + coveredTeams.size() + " teams, but there are only " + Team.getNumFranchises() + "!");
	    System.err.println(using);
	    System.exit(1);
	}
	//if this combination covers all 30 teams, then this is either tied for the smallest full coverage set of players, or a new smallest!
	if(coveredTeams.size() == Team.getNumFranchises()) {
	    List<SortedSet<Player>> ret = new ArrayList<>();
	    ret.add(new TreeSet<Player>(using));
	    return ret;
	}
	//if there are no players left to evaluate, it's done, and this track is finished!
	if(0 == available.size()) {
	    return new ArrayList<SortedSet<Player>>();
	}
	List<SortedSet<Player>> playerses = new ArrayList<>();
	Player p = available.last();
	SortedSet<Player> newAvailableExcludingP = available.headSet(p);
	using.add(p);
	Set<Team> newTeams = Player.getTeamsByPlayers(using);
	PlayerComparator pc = new PlayerComparator(newTeams);
	SortedSet<Player> newAvailableIncludingP = new TreeSet<>(pc);
	newAvailableIncludingP.addAll(newAvailableExcludingP);
	List<SortedSet<Player>> smallestIncludingP = getSmallestSets(using, newAvailableIncludingP, minSizeKnown);
	using.remove(p);
	if(smallestIncludingP.size() > 0) {
	    int candidate = smallestIncludingP.get(0).size();
	    if(candidate <= minSizeKnown) {
		playerses = smallestIncludingP;
		if(candidate == minSizeKnown) {
		    System.err.println("Tied the smallest known possible size: " + candidate);
		    for(int i = 0; i < playerses.size(); i++) {
			System.err.println(playerses.get(i));
		    }
		}
		else {
		    System.err.println("New smallest full coverage size: " + candidate);
		    for(int i = 0; i < playerses.size(); i++) {
			System.err.println(playerses.get(i));
		    }
		    minSizeKnown = candidate;
		}
	    }
	}
	List<SortedSet<Player>> smallestExcludingP = getSmallestSets(using, newAvailableExcludingP, minSizeKnown);
	if(smallestExcludingP.size() > 0) {
	    int candidate = smallestExcludingP.get(0).size();
	    if(candidate == minSizeKnown) {
		playerses.addAll(smallestExcludingP);
		System.err.println("Tied the smallest known possible size: " + candidate);
		for(int i = 0; i < playerses.size(); i++) {
		    System.err.println(playerses.get(i));
		}
	    }
	    else if(candidate < minSizeKnown) {
		System.err.println("New smallest full coverage size: " + candidate);
		for(int i = 0; i < playerses.size(); i++) {
		    System.err.println(playerses.get(i));
		}
		minSizeKnown = candidate;
		playerses = smallestExcludingP;
	    }
	}
	return playerses;
    }
    
    public static void main(String[] args) {
	File hitterFile = new File(hitterFileName),
	    pitcherFile = new File(pitcherFileName);
	//I know it's possible to cover all 30 teams with 4 players. Is 3 possible?
	final int MAX_PLAYERS = 4;
	try {
	    Scanner hitterScanner = new Scanner(hitterFile), pitcherScanner = new Scanner(pitcherFile);
	    //get the hitters
	    int teamCol = -1, nameCol = -1, idCol = -1, teamCountCol = -1;
	    String line = hitterScanner.nextLine();
	    String[] header = line.split(",");
	    for(int i = 0; i < header.length; i++) {
		if(header[i].equals("Team")) {
		    teamCol = i;
		}
		else if(header[i].equals("Player-additional")) {
		    idCol = i;
		}
		else if(header[i].equals("Player")) {
		    nameCol = i;
		}
		else if(header[i].equals("Fr.")) {
		    teamCountCol = i;
		}
		if(-1 != teamCol && -1 != idCol && -1 != nameCol && -1 != teamCountCol) {
		    break;
		}
	    }
	    if(-1 == teamCol || -1 == idCol || -1 == nameCol || -1 == teamCountCol) {
		System.err.println("Couldn't find the 4 required columns for hitters (Team, Player, Player-additional, Fr.). Try again.");
		System.exit(1);
	    }
	    List<Player> hitters = new ArrayList<>();
	    while(hitterScanner.hasNextLine()) {
		line = hitterScanner.nextLine();
		String[] vals = line.split(",");
		Player p = Player.getPlayer(vals[idCol], vals[nameCol], vals[teamCol], Integer.parseInt(vals[teamCountCol]));
		hitters.add(p);
	    }
	    System.err.println(hitters.size() + " hitters found");

	    //get the pitchers
	     List<Player> pitchers = new ArrayList<>();
	    line = pitcherScanner.nextLine();
	    header = line.split(",");
	    teamCol = -1;
	    idCol = -1;
	    nameCol = -1;
	    teamCountCol = -1;
	    for(int i = 0; i < header.length; i++) {
		if(header[i].equals("Team")) {
		    teamCol = i;
		}
		else if(header[i].equals("Player-additional")) {
		    idCol = i;
		}
		else if(header[i].equals("Player")) {
		    nameCol = i;
		}
		else if(header[i].equals("Fr.")) {
		    teamCountCol = i;
		}
		if(-1 != teamCol && -1 != idCol && -1 != nameCol && -1 != teamCountCol) {
		    break;
		}
	    }
	    if(-1 == teamCol || -1 == idCol || -1 == nameCol || -1 == teamCountCol) {
		System.err.println("Couldn't find the 4 required columns for pitchers (Team, Player, Player-additional, Fr.). Try again.");
		System.exit(1);
	    }
	    while(pitcherScanner.hasNextLine()) {
		line = pitcherScanner.nextLine();
		String[] vals = line.split(",");
		Player p = Player.getPlayer(vals[idCol], vals[nameCol], vals[teamCol], Integer.parseInt(vals[teamCountCol]));
		pitchers.add(p);
	    }
	    System.err.println(pitchers.size() + " pitchers found");

	    //put both lists together and sort them
	    PlayerComparator playerComp = new PlayerComparator(new ArrayList<Team>());
	    SortedSet<Player> allPlayers = new TreeSet<>(playerComp);
	    allPlayers.addAll(hitters);
	    allPlayers.addAll(pitchers);
	    System.err.println(allPlayers.size() + " total players");
	    List<SortedSet<Player>> smallestCoverages = getSmallestSets(new TreeSet<Player>(REVERSE_TEAM_COUNT), allPlayers, MAX_PLAYERS);
	    if(smallestCoverages.size() == 0) {
		System.err.println("Cannot cover all 30 teams with " + MAX_PLAYERS + " players. Try a bigger number.");
	    }
	    else {
		PlayerComparator reverseTeamCountOrder = new PlayerComparator(new ArrayList<Team>(), true);
		int smallestSize = smallestCoverages.get(0).size();
		//print the header
		List<String> cols = new ArrayList<>(smallestSize);
		for(int i = 1; i <= smallestSize; i++) {
		    cols.add("Player " + i);
		}
		System.out.println(String.join("\t", cols));
		//print each set of players
		for(SortedSet<Player> coverage : smallestCoverages) {
		    List<String> verbose = coverage.stream().map(p -> p.verboseString()).collect(Collectors.toList());
		    System.out.println(String.join("\t", verbose));
		}
	    }
	}
	catch(FileNotFoundException e) {
	    e.printStackTrace();
	}
    }
}
