import java.util.Map;
import java.util.HashMap;

//Only considering teams since 1901 (the modern era).
public class Team {
    private final String modernAbbreviation;
    private static Map<String, Team> allTeams = new HashMap<>();
    private static Map<String, String> teamAbbrevToFullName = new HashMap<>();
    static {
	//AL East
	teamAbbrevToFullName.put("BAL", "St. Louis Browns/Baltimore Orioles");
	teamAbbrevToFullName.put("BOS", "Boston Red Sox");
	teamAbbrevToFullName.put("NYY", "New York Highlanders/Yankees");
	teamAbbrevToFullName.put("TBR", "Tampa Bay Devil Rays/Rays");
	teamAbbrevToFullName.put("TOR", "Toronto Blue Jays");
	//AL Central
	teamAbbrevToFullName.put("CHW", "Chicago White Sox");
	teamAbbrevToFullName.put("CLE", "Cleveland Naps/Indians/Guardians");
	teamAbbrevToFullName.put("DET", "Detroit Tigers");
	teamAbbrevToFullName.put("KCR", "Kansas City Royals");
	teamAbbrevToFullName.put("MIN", "Washington Senators/Minnesota Twins");
	//AL West
	teamAbbrevToFullName.put("ATH", "Philadelphia/Kansas City/Oakland/Homeless Athletics");
	teamAbbrevToFullName.put("LAA", "Los Angeles/California/Anaheim Angels");
	teamAbbrevToFullName.put("HOU", "Houston Colt .45s/Astros");
	teamAbbrevToFullName.put("SEA", "Seattle Mariners");
	teamAbbrevToFullName.put("TEX", "Washington Senators/Texas Rangers");
	//NL East
	teamAbbrevToFullName.put("ATL", "Boston/Milwaukee/Atlanta Braves");
	teamAbbrevToFullName.put("MIA", "Florida/Miami Marlins");
	teamAbbrevToFullName.put("NYM", "New York Mets");
	teamAbbrevToFullName.put("PHI", "Philadelphia Phillies");
	teamAbbrevToFullName.put("WSN", "Montreal Expos/Washington Nationals");
	//NL Central
	teamAbbrevToFullName.put("CHC", "Chicago Cubs");
	teamAbbrevToFullName.put("CIN", "Cincinnati Reds");
	teamAbbrevToFullName.put("MIL", "Seattle Pilots/Milwaukee Brewers");
	teamAbbrevToFullName.put("PIT", "Pittsburgh Pirates");
	teamAbbrevToFullName.put("STL", "St. Louis Cardinals");
	//NL West
	teamAbbrevToFullName.put("ARI", "Arizona Diamondbacks");
	teamAbbrevToFullName.put("COL", "Colorado Rockies");
	teamAbbrevToFullName.put("LAD", "Brooklyn/Los Angeles Dodgers");
	teamAbbrevToFullName.put("SDP", "San Diego Padres");
	teamAbbrevToFullName.put("SFG", "New York/San Francisco Giants");
    }

    public static int getNumFranchises() {
	return teamAbbrevToFullName.size();
    }

    public static String getCurrentAbbrev(String teamAbbr) {
        switch(teamAbbr) {
	case "TBD": //Tampa Bay Devil Rays (1998-2007)
	    return "TBR";
	case "MON": //Montreal Expos (1969-2004)
	    return "WSN";
	case "PHA": //Philadelphia Athletics (1901-1954)
	case "KCA": //Kansas City Athletics (1955-1967)
	case "OAK": //Oakland Athletics (1968-2024)
	    return "ATH";
	case "BSN": //Boston Braves (1912-1935, 1941-1952), Boston Bees (1936-1940), Boston Rustlers (1911), Boston Doves (1907-1910), Boston Beaneaters (1901-1906)
	case "MLN": //Milwaukee Braves (1953-1965)
	    return "ATL";
	case "SEP": //Seattle Pilots (1969)
	    return "MIL";
	case "BRO": //Brooklyn Dodgers (1901-1957)
	    return "LAD";
	case "NYG": //New York Giants (1901-1957)
	    return "SFG";
	case "SLB": //St. Louis Browns (1902-1954)
	    return "BAL";
	case "WSH": //Washington Senators/Nationals (1901-1960)
	    return "MIN";
	case "WSA": //Washington Senators (1961-1971)
	    return "TEX";
	case "ANA": //Anaheim Angels (1997-2004)
	case "CAL": //California Angels (1965-1996)
	    return "LAA";
	case "FLA": //Florida Marlins (1993-2011)
	    return "MIA";
	default:
	    return teamAbbr;
	}
    }

    public static Team getTeam(String abbrev) {
	String modern_abbrev = getCurrentAbbrev(abbrev);
	if(allTeams.containsKey(modern_abbrev)) {
	    return allTeams.get(modern_abbrev);
	}
	Team t = new Team(modern_abbrev);
	allTeams.put(modern_abbrev, t);
	return t;
    }

    private Team(String abbrevIn) {
	modernAbbreviation = abbrevIn;
    }

    public boolean equals(Object other) {
	return other instanceof Team && ((Team)other).modernAbbreviation.equals(modernAbbreviation);
    }

    public String toString() {
	return teamAbbrevToFullName.get(modernAbbreviation);
    }
}
