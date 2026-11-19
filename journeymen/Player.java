import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

public class Player implements Comparable<Player> {
    public final String id, name;
    public final Set<Team> teams;
    private static Map<String, Player> allPlayers = new HashMap<>();

    public static Player getPlayer(String id, String name, String teamStr, int numTeams) {
	Set<Team> _teams = new HashSet<>();
	for(int i = 0; i < teamStr.length(); i += 3) {
	    _teams.add(Team.getTeam(teamStr.substring(i, i+3)));
	}
	if(_teams.size() != numTeams) {
	    System.err.println("Issue with " + name + ": team count mismatch!");
	}
	if(allPlayers.containsKey(id)) {
	    System.err.println(name + " (" + id + ") already exists. Adding previously unknown teams to that entry.");
	    allPlayers.get(id).teams.addAll(_teams);
	    return allPlayers.get(id);
	}
	Player p = new Player(id, name, _teams);
	allPlayers.put(id, p);
	return p;
    }

    private Player(String idIn, String nameIn, Set<Team> teamsIn) {
	id = idIn;
	name = nameIn;
	teams = teamsIn;
    }
    
    private Player(String idIn, String nameIn, String teamStr, int numTeams) {
	id = idIn;
	name = nameIn;
	teams = new HashSet<Team>();
	for(int i = 0; i < teamStr.length(); i += 3) {
	    teams.add(Team.getTeam(teamStr.substring(i, i+3)));
	}
	if(teams.size() != numTeams) {
	    System.err.println("Issue with " + nameIn + ": team count mismatch!");
	}
    }

    public boolean equals(Object other) {
	return other instanceof Player && id.equals(((Player)other).id);
    }

    public int addMoreTeams(String teamStr) {
	int startSize = teams.size();
	for(int i = 0; i < teamStr.length(); i+=3) {
	    teams.add(Team.getTeam(teamStr.substring(i, i+3)));
	}
	return teams.size() - startSize;
    }

    public int compareTo(Player other) {
	return id.compareTo(other.id);
    }

    public String toString() {
	return name + " (" + id + ")";
    }

    public String verboseString() {
	StringBuilder sb = new StringBuilder(name);
	sb.append(" (").append(id).append(")");
	List<String> fullTeamNames = teams.stream().map(t -> t.toString()).collect(Collectors.toList());
	sb.append("(").append(String.join(", ", fullTeamNames)).append(")");
	return sb.toString();
    }

    public static Set<Team> getTeamsByPlayers(Collection<Player> players) {
	Set<Team> teams = new HashSet<>();
	for(Player p : players) {
	    teams.addAll(p.teams);
	}
	return teams;
    }
    public Set<Team> teamsExcept(Collection<Team> teamsToExclude) {
	Set<Team> teams2 = new HashSet<>();
	teams2.addAll(teams);
	teams2.removeAll(teamsToExclude);
	return teams2;
    }
}
