package com.battlecode.Robots;

import java.util.HashMap;

import com.battlecode.Helpers.RobotPersonality;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;


public class BotScout extends Globals {

	static boolean danger;
	static RobotInfo[] enemyRobots;
	static Direction movement;
	static float foundEast;
	static float mapSize;


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

			/*else{
				if(mapSize == -1) {
					mapSize = 0;
					do {
						do {
							rc.move(Direction.getEast());
						} while(rc.canMove(Direction.getEast()));
						foundEast = rc.getLocation().x;
						do {
							rc.move(Direction.getWest());
						} while (rc.canMove(Direction.getWest()));
						mapSize = foundEast - rc.getLocation().x;
					} while (mapSize <= 0);
				}
			}*/

		}catch (Exception e) {
			System.out.println("Scout Exception");
			e.printStackTrace();
		}
	}
}