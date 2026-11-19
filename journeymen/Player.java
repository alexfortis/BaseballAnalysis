import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

public class Player implements Comparable<Player> {
    public final String id, name;
    public final Set<Team> teams;
    public Player(String idIn, String nameIn, String teamStr, int numTeams) {
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
}
