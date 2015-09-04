package au.com.mineauz.minigamesregions;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import au.com.mineauz.minigames.MinigamePlayer;
import au.com.mineauz.minigames.properties.Properties;
import au.com.mineauz.minigames.properties.Property;
import au.com.mineauz.minigamesregions.actions.ActionInterface;
import au.com.mineauz.minigamesregions.actions.Actions;
import au.com.mineauz.minigamesregions.conditions.ConditionInterface;
import au.com.mineauz.minigamesregions.conditions.Conditions;
import au.com.mineauz.minigamesregions.triggers.Trigger;

public class TriggerExecutor {
	private static final UUID GLOBAL_TRIGGER = UUID.fromString("00000000-0000-0000-0000-000000000000");
	private final Trigger trigger;
	private final TriggerArea owner;
	
	private final Property<Boolean> triggerPerPlayer;
	private final Property<Integer> triggerCount;
	
	private final List<ConditionInterface> conditions;
	private final List<ActionInterface> actions;
	
	// Current game state
	private Map<UUID, Integer> triggers;
	
	public TriggerExecutor(Trigger trigger, TriggerArea owner) {
		this.trigger = trigger;
		this.owner = owner;
		
		triggerPerPlayer = Properties.create(false);
		triggerCount = Properties.create(0);
		
		conditions = Lists.newArrayList();
		actions = Lists.newArrayList();
		
		triggers = Maps.newHashMap();
	}
	
	public Trigger getTrigger() {
		return trigger;
	}
	
	public TriggerArea getOwner() {
		return owner;
	}
	
	public List<ConditionInterface> getConditions() {
		return conditions;
	}
	
	public List<ActionInterface> getActions() {
		return actions;
	}
	
	public int getTriggerCount() {
		return triggerCount.getValue();
	}
	
	public void setTriggerCount(int count) {
		triggerCount.setValue(count);
	}
	
	public Property<Integer> triggerCount() {
		return triggerCount;
	}
		
	public boolean isTriggerPerPlayer() {
		return triggerPerPlayer.getValue();
	}
	
	public void setTriggerPerPlayer(boolean perPlayer) {
		triggerPerPlayer.setValue(perPlayer);
	}
	
	public Property<Boolean> triggerPerPlayer() {
		return triggerPerPlayer;
	}
	
	public boolean canBeTriggered(MinigamePlayer player) {
		if (triggerCount.getValue() != 0) {
			if (!triggerPerPlayer.getValue()) {
				if (triggers.get(GLOBAL_TRIGGER) != null && triggers.get(GLOBAL_TRIGGER) >= triggerCount.getValue()) {
					return false;
				}
			} else {
				if (triggers.get(player.getUUID()) != null && triggers.get(player.getUUID()) >= triggerCount.getValue()) {
					return false;
				}
			}
		}
		return true;
	}
	
	public void clearTriggers() {
		triggers.clear();
	}
	
	public void removeTrigger(MinigamePlayer player) {
		triggers.remove(player.getUUID());
	}
	
	public boolean canTrigger(MinigamePlayer player, TriggerArea area) {
		for (ConditionInterface condition : conditions) {
			if (condition.requiresPlayer() && player == null) {
				continue;
			}
			
			boolean matches = condition.checkCondition(player, area);
			if (condition.isInverted()) {
				matches = !matches;
			}
			
			if (!matches) {
				return false;
			}
		}
		
		return (canBeTriggered(player));
	}
	
	public void execute(MinigamePlayer player, TriggerArea area) {
		for (ActionInterface action : actions) {
			// Allow the area to be enabled still
			if(!area.isEnabled() && !action.getName().equalsIgnoreCase("SET_ENABLED")) {
				continue;
			}
			
			if (action.requiresPlayer() && player == null) {
				continue;
			}
			
			action.executeAction(player, area);
			
			if (isTriggerPerPlayer()) {
				addPlayerTrigger(player);
			} else {
				addPublicTrigger();
			}
		}
	}
	
	private void addPublicTrigger() {
		if(!triggers.containsKey(GLOBAL_TRIGGER)) {
			triggers.put(GLOBAL_TRIGGER, 0);
		}
		triggers.put(GLOBAL_TRIGGER, triggers.get(GLOBAL_TRIGGER) + 1);
	}
	
	private void addPlayerTrigger(MinigamePlayer player) {
		if (!triggers.containsKey(player.getUUID())) {
			triggers.put(player.getUUID(), 1);
		} else {
			triggers.put(player.getUUID(), triggers.get(player.getUUID()) + 1);
		}
	}
	
	public void save(ConfigurationSection section) {
		// Save actions
		int index = 0;
		for (ActionInterface action : actions) {
			ConfigurationSection actionSection = section.createSection("actions." + index);
			actionSection.set("type", action.getName());
			action.save(actionSection);
			++index;
		}
		
		// Save conditions
		index = 0;
		for (ConditionInterface condition : conditions) {
			ConfigurationSection actionSection = section.createSection("conditions." + index);
			actionSection.set("type", condition.getName());
			condition.save(actionSection);
			++index;
		}
		
		// Save additional arguments
		if (isTriggerPerPlayer()) {
			section.set("isTriggeredPerPlayer", isTriggerPerPlayer());
		}
		if (getTriggerCount() != 0) {
			section.set("triggerCount", getTriggerCount());
		}
	}
	
	public void load(ConfigurationSection section) {
		// Load actions
		ConfigurationSection actionsSection = section.getConfigurationSection("actions");
		actions.clear();
		
		for (String key : actionsSection.getKeys(false)) {
			ConfigurationSection actionSection = actionsSection.getConfigurationSection(key);
			ActionInterface action = Actions.getActionByName(actionSection.getString("type"));
			
			action.load(actionSection);
			actions.add(action);
		}
		
		// Load conditions
		ConfigurationSection conditionsSection = section.getConfigurationSection("conditions");
		actions.clear();
		
		for (String key : conditionsSection.getKeys(false)) {
			ConfigurationSection conditionSection = conditionsSection.getConfigurationSection(key);
			ConditionInterface condition = Conditions.getConditionByName(conditionSection.getString("type"));
			
			condition.load(conditionSection);
			conditions.add(condition);
		}
		
		// Load additional arguments
		if (section.contains("isTriggeredPerPlayer")) {
			setTriggerPerPlayer(section.getBoolean("isTriggeredPerPlayer"));
		}
		if (section.contains("triggerCount")) {
			setTriggerCount(section.getInt("triggerCount"));
		}
	}
}
