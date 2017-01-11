package com.battlecode.Robots;

import com.battlecode.Helpers.RobotPersonality;

import battlecode.common.Direction;
import battlecode.common.RobotInfo;

public class BotTank extends Globals {
	static boolean danger;
	static RobotInfo[] enemyRobots;
	static Direction movement;
	public static RobotPersonality myPersonality;
    
	public static void loop() {
     

		try{
			
			switch(myPersonality){
			case PLOWER:
				actAsPlower();
				break;
			case KILLER:
				actAsKiller();
			default:
				actAsKiller();
				break;
			}


		}catch (Exception e) {
			System.out.println("LumberJack Exception");
			e.printStackTrace();
		}
		
	}

	private static void actAsKiller() {
		
		detectNearbyEnemies();
		if(danger){
			//TODO Fight logic 
		}

		else{
			//TODO MotionLogic to ffind fight if fighter, defend if defender.
		}		
	}

	private static void actAsPlower() {
		
		detectNearbyEnemies();

		if(danger){
			//TODO Fight logic 
		}

		else{
			//TODO MotionLogic to ffind fight if fighter, defend if defender.
		}	
	}
	
	private static void detectNearbyEnemies(){
		enemyRobots = rc.senseNearbyRobots(-1, friendly.opponent());

		if(enemyRobots.length > 0){
			danger = true; //TODO could be cost inefficient to be doing this. 
		}
		else if (enemyRobots.length == 0){
			danger = false;
		}

	}
}