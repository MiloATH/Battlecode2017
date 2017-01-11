package com.battlecode.Robots;

import battlecode.common.*;

public class BotScout extends Globals {

	static boolean danger;
	static RobotInfo[] enemyRobots;
	static Direction movement;

	public static void loop() {

		try{

			enemyRobots = rc.senseNearbyRobots(15, friendly.opponent());

			if(enemyRobots.length > 0){
				danger = true; //TODO could be cost inefficient to be doing this. 
			}
			else if (enemyRobots.length == 0){
				danger = false;
			}

			if(danger){
				//TODO Fight logic for lumberJacks
			}

			else{
				//TODO MotionLogic
			}

		}catch (Exception e) {
			System.out.println("LumberJack Exception");
			e.printStackTrace();
		}
	}
}