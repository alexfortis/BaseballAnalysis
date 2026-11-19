import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Set;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;
import java.util.stream.Collectors;

public class FindBestTuples {
    private static String hitterFileName = "journeyman_hitters.csv",
	pitcherFileName = "journeyman_pitchers.csv";
    
    private static Set<String> getModernFranchisesFromStr(String team_str) {
	Set<String> franchises = new HashSet<>();
	for(int i = 0; i < team_str.length(); i += 3) {
	    String abbrev = team_str.substring(i, i+3);
	    String currentAbbrev = Team.getCurrentAbbrev(abbrev);
	    franchises.add(currentAbbrev);
	}
	return franchises;
    }

    private static class PlayerListComparator implements Comparator<List<String>> {
	Map<String, Set<String>> playerMap;
	public PlayerListComparator(Map<String, Set<String>> playerMapIn) {
	    playerMap = playerMapIn;
	}
	@Override
	public int compare(List<String> ids1, List<String> ids2) {
	    Set<String> teams1 = new HashSet<>(), teams2 = new HashSet<>();
	    for(String id : ids1) {
		teams1.addAll(playerMap.get(id));
	    }
	    for(String id : ids2) {
		teams2.addAll(playerMap.get(id));
	    }
	    if(teams1.size() == teams2.size()) {
		if(ids1.size() == ids2.size()) {
		    for(int i = 0; i < ids1.size(); i++) {
			if(!ids1.get(i).equals(ids2.get(i))) {
			    return ids1.get(i).compareTo(ids2.get(i));
			}
		    }
		}
		return ids1.size() - ids2.size();
	    }
	    return teams1.size() - teams2.size();
	}
    }

    public static void main(String[] args) {
	final int TUPLE_SIZE = args.length == 0 ? 2 : Math.max(2, Integer.parseInt(args[0]));
	//for pairs, the goal is to surpass the combined total of Edwin Jackson and Rich Hill, which is 22 teams. Each of them played for a record 14 teams, sp the fewest teams a player could have played for to pair with one of them for more combined teams is 23-14 or 9.
	//For everyone else, I don't know yet.
	final int JOURNEYMAN_THRESHOLD = 2==TUPLE_SIZE ? 9 : 1;
	Map<String, Set<String>> franchisesByPlayer = new HashMap<>();
	File hitterFile = new File(hitterFileName), pitcherFile = new File(pitcherFileName);
	try {
	    Scanner hitterScanner = new Scanner(hitterFile), pitcherScanner = new Scanner(pitcherFile);
	    //get the hitters
	    //hard coding the column numbers now because I'm lazy
	    String line = hitterScanner.nextLine();
	    //String[] header = line.split(",");
	    while(hitterScanner.hasNextLine()) {
		line = hitterScanner.nextLine();
		String[] vals = line.split(",");
		String teamStr = vals[vals.length-2], id = vals[vals.length-1];
		Set<String> teams = getModernFranchisesFromStr(teamStr);
		if(teams.size()>=JOURNEYMAN_THRESHOLD) {
		    franchisesByPlayer.put(id, teams);
		}
		else break;
	    }
	    //get the pitchers
	    line = pitcherScanner.nextLine();
	    while(pitcherScanner.hasNextLine()) {
		line = pitcherScanner.nextLine();
		String[] vals = line.split(",");
		String teamStr = vals[vals.length-2], id = vals[vals.length-1];
		Set<String> teams = getModernFranchisesFromStr(teamStr);
		if(teams.size()>=JOURNEYMAN_THRESHOLD) {
		    franchisesByPlayer.put(id, teams);
		}
		else break;
	    }
	    System.out.println(franchisesByPlayer.size() + " players found");
	    PlayerListComparator plc = new PlayerListComparator(franchisesByPlayer);
	    SortedSet<List<String>> allPairsSorted = new TreeSet<>(plc);
	    List<String> allIds = new ArrayList<>();
	    allIds.addAll(franchisesByPlayer.keySet());
	    for(int i = 0; i < allIds.size()-1; i++) {
		for(int j = i+1; j < allIds.size(); j++) {
		    List<String> pair = new ArrayList<>();
		    pair.add(allIds.get(i));
		    pair.add(allIds.get(j));
		    allPairsSorted.add(pair);
		}
	    }
	    for(List<String> pair : allPairsSorted) {
		List<String> forPrinting = pair.stream().map(p -> p + " (" + String.join(", ", franchisesByPlayer.get(p)) + ")").collect(Collectors.toList());
		System.out.println(forPrinting);
	    }
	}
	catch(FileNotFoundException e) {
	    e.printStackTrace();
	}
    }
}
