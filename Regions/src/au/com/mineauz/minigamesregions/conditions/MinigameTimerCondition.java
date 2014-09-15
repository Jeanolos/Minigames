package au.com.mineauz.minigamesregions.conditions;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Event;

import au.com.mineauz.minigames.MinigamePlayer;
import au.com.mineauz.minigames.config.IntegerFlag;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemPage;
import au.com.mineauz.minigames.menu.MenuItemTime;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;

public class MinigameTimerCondition extends ConditionInterface{
	
	private IntegerFlag minTime = new IntegerFlag(5, "minTime");
	private IntegerFlag maxTime = new IntegerFlag(10, "maxTime");

	@Override
	public String getName() {
		return "MINIGAME_TIMER";
	}

	@Override
	public String getCategory() {
		return "Minigame Conditions";
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
	public boolean checkRegionCondition(MinigamePlayer player, Region region,
			Event event) {
		return check(player.getMinigame());
	}

	@Override
	public boolean checkNodeCondition(MinigamePlayer player, Node node,
			Event event) {
		return check(player.getMinigame());
	}
	
	private boolean check(Minigame mg){
		if(mg.getMinigameTimer().getTimeLeft() >= minTime.getFlag() &&
				mg.getMinigameTimer().getTimeLeft() <= maxTime.getFlag()){
			return true;
		}
		return false;
	}

	@Override
	public void saveArguments(FileConfiguration config, String path) {
		minTime.saveValue(path, config);
		maxTime.saveValue(path, config);
	}

	@Override
	public void loadArguments(FileConfiguration config, String path) {
		minTime.loadValue(path, config);
		maxTime.loadValue(path, config);
	}

	@Override
	public boolean displayMenu(MinigamePlayer player, Menu prev) {
		Menu m = new Menu(3, "Minigame Timer", player);
		
		m.addItem(new MenuItemTime("Min Time", Material.WATCH, minTime.getCallback(), 0, null));
		m.addItem(new MenuItemTime("Max Time", Material.WATCH, maxTime.getCallback(), 0, null));
		
		m.addItem(new MenuItemPage("Back", Material.REDSTONE_TORCH_ON, prev), m.getSize() - 9);
		m.displayMenu(player);
		return true;
	}

}