package com.battlecode.Robots;

import java.util.HashMap;

import com.battlecode.Helpers.RobotPersonality;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;

public class BotSoldier extends Globals {
	static boolean danger, onMission;
	static boolean canFirePentad, canFireSingle, canFireTriad;
	static boolean isFighter, isDefender;
	static RobotInfo[] enemyRobots;
	static MapLocation currentLocation;
	static Direction movement;
	public static RobotPersonality myPersonality;

	public static void loop() {

		try{
			
			switch(myPersonality){
			case ATTACKER:
				actAsAttacker();
				break;
			case DEFENDER:
				actAsDefender();
				break;
			default:
				actAsAttacker();
				break;
			
			}
		}
		
		catch (Exception e) {
			System.out.println("LumberJack Exception");
			e.printStackTrace();
		}

	}

	private static void actAsAttacker() throws GameActionException {

		updateLocalEnvironment();

		//TODO we should put in logic to determine if the soldier we create should be a fighter or a defender. 

		if(danger){
			//TODO Fight logic 
			if(!rc.hasAttacked()){
				attackClosestEnemy();

			}
		}
		else{	
			if(!Globals.initialEnemyArchonLocatonsChecked & !onMission){
				scoutInitialEnemyArchonLocations();
			}
			
			footSoldierMovement();
		
		}
	}	

	
	private static void actAsDefender() throws GameActionException {

		updateLocalEnvironment();

		//TODO we should put in logic to determine if the soldier we create should be a fighter or a defender. 

		if(danger){
			//TODO Fight logic 
			if(!rc.hasAttacked()){
				attackClosestEnemy();

			}
		}
		else{	
			if(!Globals.initialEnemyArchonLocatonsChecked & !onMission){
				scoutInitialEnemyArchonLocations();
			}
			
			footSoldierMovement();
		
		}
	}	


	
	private static void footSoldierMovement() {
		// TODO Auto-generated method stub

	}
	
	
	

	/**
	 * This figures out an initial Enemy Archon Location to go to if they haven't been checked yet. If it finds one,
	 * it sets onMission to be true, and sets the movement to that area. 
	 * @throws GameActionException
	 */
	private static void scoutInitialEnemyArchonLocations() throws GameActionException {
		
		HashMap<MapLocation, Boolean> test = Globals.InitialEnemyArchonLocationStatus;
		MapLocation[] locations = (MapLocation[]) test.keySet().toArray();
		for(int i = 0; i <= test.size(); i++){
			MapLocation toCheck = locations[i];
			if(!test.get(toCheck).booleanValue()){
				movement = currentLocation.directionTo(toCheck);
				onMission = true;
				break;
			}
		}
	}

	private static void determineIfFighterOrDefender() {
		// TODO Auto-generated method stub

	}

	private static void attackClosestEnemy() throws GameActionException {
		//TODO We have to make sure that it doesn't pull the trigger if there's a friendly in that direction. 
		//TODO need to make sure that the closest robot is in fact the 0 pointer in the array.

		MapLocation closestEnemy = enemyRobots[0].location;
		Direction enemyDirection = currentLocation.directionTo(closestEnemy);
		if (!rc.hasAttacked()){
			if(rc.canFirePentadShot() && enemyRobots.length > 3){
				rc.firePentadShot(enemyDirection);
			}
			else if(rc.canFireTriadShot() && enemyRobots.length > 1){
				rc.fireTriadShot(enemyDirection);
			}
			else if(rc.canFireSingleShot()){
				rc.fireSingleShot(enemyDirection);
			}
		}
	}


	private static void updateLocalEnvironment(){
		enemyRobots = rc.senseNearbyRobots(15, friendly.opponent());
		currentLocation = rc.getLocation();

		if(enemyRobots.length > 0){
			danger = true; //TODO could be cost inefficient to be doing this. 
		}

		else if (enemyRobots.length == 0){
			danger = false;
		}

	}
}