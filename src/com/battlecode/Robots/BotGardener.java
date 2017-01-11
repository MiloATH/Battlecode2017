package com.battlecode.Robots;

import battlecode.common.Direction;
import battlecode.common.RobotInfo;

public class BotGardener extends Globals {
	

	static boolean danger;
	static RobotInfo[] enemyRobots;
	static Direction movement;
	
	public static void runBotGardener() {
		try{

			enemyRobots = rc.senseNearbyRobots(15, friendly.opponent());

			if(enemyRobots.length > 0){
				danger = true; //TODO could be cost inefficient to be doing this. 
			}
			else if (enemyRobots.length == 0){
				danger = false;
			}

			if(danger){
				//TODO Flee Logic
			}

			else{
				//TODO MotionLogic 
				//TODO Logic to create something. 
			}

		}catch (Exception e) {
			System.out.println("Gardner Exception");
			e.printStackTrace();
		}
		
		
	}
}