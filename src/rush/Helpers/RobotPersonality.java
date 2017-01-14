package rush.Helpers;

import battlecode.common.RobotType;

public enum RobotPersonality {
    
	
	//LumberJack Types
	KAMIKAZEE("Goal is to go deep into enemy territory and swing with little regard for friendlies", RobotType.LUMBERJACK),
	CHOPPER("Goal is to chop trees", RobotType.LUMBERJACK),
	NORMALLUMBERJACK("Behaves in our normal fashion", RobotType.LUMBERJACK),
	
	//Scout Types
	RECON("Scouts the initial Archon Locations", RobotType.SCOUT),
	MAPDETAILER("Scouts the map", RobotType.SCOUT),
	SPY("Scouts areas of interest", RobotType.SCOUT),
	
	//Soldier types
	
	ATTACKER("Attacking Soldier", RobotType.SOLDIER),
	DEFENDER("Defending Soldier", RobotType.SOLDIER),
	
	//Gardener Types
	PLANTER("Planter", RobotType.GARDENER),
	DEPLOYER("Deploys battlebots", RobotType.GARDENER),
	
	//Tank Types
	PLOWER("Task is to destory trees", RobotType.TANK),
	KILLER("Task is to run over other enemies", RobotType.TANK);
	
	
	
    private final String description; 
	private final RobotType type;
	
	
	RobotPersonality(String desc, RobotType x) {
        this.description = desc;
        this.type = x;
        
    }
}
