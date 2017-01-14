package com.battlecode.Robots;

import battlecode.common.*;
import com.battlecode.Helpers.CreationStatus;
import com.battlecode.Helpers.RobotPersonality;

public class BotGardener extends Globals {


	static boolean danger, treesAreNearby;
	static boolean planter;
	static int ticker;
	static RobotInfo[] enemyRobots;
	static TreeInfo[] nearbyTrees;
	static Direction movement, aggressiveDeploy, defensiveDeploy, plantDir;
	static CreationStatus currentlyMade;
	static boolean plantTree, deploySold, deployTank, deployLumberJack, deployScout;
	public static RobotPersonality myPersonality;
	static boolean canMoveAway = true;
	static boolean hasFarm;

	static double tempTreeHolder = 0.4;
	static float treeMultiplier = (float) tempTreeHolder;

    static int movesFromEdge = 0;

	public static void loop() {
		try{
			switch(myPersonality){
			case PLANTER:
				actAsPlanter();
				break;
			case DEPLOYER:
				actAsDeployer();
			default:
				getRole();
				break;
			}

		}catch (Exception e) {
			System.out.println("Gardener Exception");
			e.printStackTrace();
		}
	}

    private static void getRole() throws GameActionException {
	   int i; //TODO Fix this inefficient hack of a coding job
	    for(i=0; i<=rc.senseNearbyRobots().length; i++) {
            if(rc.getType() == RobotType.GARDENER) {
                myPersonality = RobotPersonality.PLANTER;
            } else {
                myPersonality = RobotPersonality.DEPLOYER;
            }
        }

    }

	private static void actAsDeployer() throws GameActionException {
		updateLocalEnvironment();

		if(danger){
			evasiveMovement();
		}
		else{
			normalMovement();
			if(!planter){
				createBots();
			}
		}
	}


	private static void actAsPlanter() throws GameActionException{
		updateLocalEnvironment();

		if(danger){
			evasiveMovement();
		}
		else{
			normalMovement();

			if(!treesAreNearby){
				plantTrees();
			}
			else{
				maintainTrees();

			}
		}
	}


	/**
	 * Method to take care of trees so they don't die on us. 
	 */

	private static void maintainTrees() {
		// TODO Auto-generated method stub

	}


	private static void updateLocalEnvironment() {


		enemyRobots = rc.senseNearbyRobots(7, friendly.opponent());
		nearbyTrees = rc.senseNearbyTrees();


		if(enemyRobots.length > 0){
			danger = true; //TODO could be cost inefficient to be doing this. 
		}
		else if (enemyRobots.length == 0){
			danger = false;
		}

		if(nearbyTrees.length > 0){
			treesAreNearby = true;
		}
		else{
			treesAreNearby = false;
		}


	}



	/**
	 * Evasive movement that the gardener should take.
	 */

	public static void evasiveMovement(){

	}

	/**
	 * Normal movement that the gardener should take.
	 */

	public static void normalMovement(){

	}

	/**
	 * What the gardener should do in the early game.
	 */

	public static void earlyGameLogic(){

	}

	/**
	 * Thought process for a gardener to plant trees.
	 */

	public static void planterMovement() throws GameActionException {
        if(!hasFarm) {
            if(rc.canMove(awayFromEnemy) && canMoveAway){
                rc.move(awayFromEnemy);
            } else {
                canMoveAway = false;

            }
        }

    }

	public static void plantTrees() throws GameActionException {

	}



	/**
	 * Thought process for a gardener to create Robots.
	 * @throws GameActionException
	 */
	public static void createBots() throws GameActionException{
		if(currentlyMade == null){
			currentlyMade = CreationStatus.CREATEDNOTHING;
		}
		boolean canBuildAggressively = rc.canMove(aggressiveDeploy);
		boolean canBuildDefensively = rc.canMove(defensiveDeploy);
		switch(currentlyMade){
		case PLANTEDTREE:
			if(canBuildAggressively){
				rc.buildRobot(RobotType.SCOUT, aggressiveDeploy);
				currentlyMade = CreationStatus.MADESCOUT1;
			}
			break;

		case MADESCOUT1:
			if(canBuildAggressively){
				rc.buildRobot(RobotType.SCOUT, aggressiveDeploy);
				currentlyMade = CreationStatus.MADESCOUT2;
			}
			break;
		case MADESCOUT2:
			if(canBuildAggressively){
				rc.buildRobot(RobotType.SOLDIER, aggressiveDeploy);
				currentlyMade = CreationStatus.MADESOLDIER1;
			}
			break;
		case MADESOLDIER1:
			if(canBuildAggressively){
				rc.buildRobot(RobotType.SOLDIER, aggressiveDeploy);
				currentlyMade = CreationStatus.MADESOLDIER2;
			}
			break;
		case MADESOLDIER2:
			if(canBuildAggressively){
				rc.buildRobot(RobotType.SOLDIER, aggressiveDeploy);
				currentlyMade = CreationStatus.MADESOLDIER3;
			}
			break;
		case MADESOLDIER3:
			if(canBuildAggressively){
				rc.buildRobot(RobotType.LUMBERJACK, aggressiveDeploy);
				currentlyMade = CreationStatus.MADELUMBERJACK;
			}
			break;
		case MADELUMBERJACK:
			if(canBuildAggressively){
				rc.buildRobot(RobotType.TANK, aggressiveDeploy);
				currentlyMade = CreationStatus.MADETANK;
			}
			break;
		case MADETANK:
			if(canBuildDefensively){
				rc.buildRobot(RobotType.GARDENER, defensiveDeploy);
				currentlyMade = CreationStatus.MADEGARDENER;
			}
			break;
		case MADEGARDENER:
			if(canBuildDefensively){
				rc.plantTree(plantDir);
				currentlyMade = CreationStatus.PLANTEDTREE;
			}
			break;
		}
	}
}