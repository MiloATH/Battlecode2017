package com.battlecode.Robots;

import com.battlecode.Helpers.CreationStatus;

import battlecode.common.Direction;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class BotGardener extends Globals {


	static boolean danger;
	static RobotInfo[] enemyRobots;
	static Direction movement, aggressiveDeploy, defensiveDeploy, plantDir;
	static CreationStatus currentlyMade;
	static boolean plantTree, deploySold, deployTank, deployLumberJack, deployScout;


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
				
				if(currentlyMade == null){
					currentlyMade = CreationStatus.CREATEDNOTHING;
				}
				
				switch(currentlyMade){
					case PLANTEDTREE:
						rc.buildRobot(RobotType.SCOUT, aggressiveDeploy);
						currentlyMade = CreationStatus.MADESCOUT1;
					break;
					
					case MADESCOUT1:
						rc.buildRobot(RobotType.SCOUT, aggressiveDeploy);
						currentlyMade = CreationStatus.MADESCOUT2;
					break;
					case MADESCOUT2:
						rc.buildRobot(RobotType.SOLDIER, aggressiveDeploy);
						currentlyMade = CreationStatus.MADESOLDIER1;
					break;
					case MADESOLDIER1:
						rc.buildRobot(RobotType.SOLDIER, aggressiveDeploy);
						currentlyMade = CreationStatus.MADESOLDIER2;
					break;
					case MADESOLDIER2:
						rc.buildRobot(RobotType.SOLDIER, aggressiveDeploy);
						currentlyMade = CreationStatus.MADESOLDIER3;
					break;
					case MADESOLDIER3:
						rc.buildRobot(RobotType.LUMBERJACK, aggressiveDeploy);
						currentlyMade = CreationStatus.MADELUMBERJACK;
					break;
					case MADELUMBERJACK:
						rc.buildRobot(RobotType.TANK, aggressiveDeploy);
						currentlyMade = CreationStatus.MADETANK;
					break;
					case MADETANK:
						rc.buildRobot(RobotType.GARDENER, defensiveDeploy);
						currentlyMade = CreationStatus.MADEGARDENER;
					break;
					case MADEGARDENER:
						rc.plantTree(plantDir);
						currentlyMade = CreationStatus.PLANTEDTREE;
					break;

				}


			}

		}catch (Exception e) {
			System.out.println("Gardner Exception");
			e.printStackTrace();
		}


	}
}