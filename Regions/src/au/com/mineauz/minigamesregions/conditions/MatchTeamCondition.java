package au.com.mineauz.minigamesregions.conditions;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import au.com.mineauz.minigames.MinigamePlayer;
import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemList;
import au.com.mineauz.minigames.minigame.TeamColor;
import au.com.mineauz.minigames.properties.types.StringProperty;
import au.com.mineauz.minigamesregions.TriggerArea;

public class MatchTeamCondition extends ConditionInterface {
	
	private final StringProperty team = new StringProperty("RED", "team"); // TODO: Use EnumProperty of TeamSelection
	
	public MatchTeamCondition() {
		properties.addProperty(team);
	}

	@Override
	public String getName() {
		return "MATCH_TEAM";
	}
	
	@Override
	public String getCategory(){
		return "Team Conditions";
	}

	@Override
	public boolean useInRegions() {
		return true;
	}

	@Override
	public boolean useInNodes() {
		return true;
	}

	@Override
	public boolean checkCondition(MinigamePlayer player, TriggerArea area) {
		if(player.getTeam() != null && player.getTeam().getColor().toString().equals(team.getValue())){
			return true;
		}
		return false;
	}

	@Override
	public boolean displayMenu(MinigamePlayer player, Menu prev) {
		Menu m = new Menu(3, "Match Team");
		List<String> teams = new ArrayList<String>();
		for(TeamColor t : TeamColor.values())
			teams.add(MinigameUtils.capitalize(t.toString().replace("_", " ")));
		m.addItem(new MenuItemList("Team Color", Material.WOOL, team, teams));
		addInvertMenuItem(m);
		m.displayMenu(player);
		return true;
	}

}
