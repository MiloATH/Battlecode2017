package com.battlecode.Robots;

import java.awt.Robot;

import battlecode.common.*;
import examplefuncsplayer.RobotPlayer;

public class BotArchon extends Globals {

	static boolean setUp;
	static MapLocation archonLoc;
	static Direction movement;
	static boolean danger;
	static RobotInfo[] enemyRobots;

	public static void runArchon() {

		while (true) {
			// Try/catch blocks stop unhandled exceptions, which cause your robot to explode
			try {
				//Early Game Logic

				if(!Globals.getSetUpInitialGlobalInfo()){
					setUpInitialGlobals();	
					Globals.setSetUpInitialGlobalInfo(true);
				}
				else{

					//Testing if it's in Danger
					updateIfInDanger();

					//Acting accordingly
					if(danger){	
						inDangerBehavior();

					}
					else{
						safeBehavior();

					}
				}


				Clock.yield();

			}

			catch (Exception e) {
				System.out.println("Archon Exception");
				e.printStackTrace();
			}
		}
	}


	private static void safeBehavior() throws GameActionException {
		if(setUp != true){
			//TODO setup algorithm, right now we'll just set setup to be true.
			setUp = true;
		}
		if(setUp){
			// Move random;
			movement = RobotPlayer.randomDirection();
			RobotPlayer.tryMove(movement);
			if(rc.canHireGardener(movement.opposite()) && Math.random() < .01){
				rc.hireGardener(movement.opposite());
			}
		}
	}

	private static void inDangerBehavior() throws GameActionException {

		archonLoc = rc.getLocation();
		broadcast();
		Direction dir = getEvadeDir();
		if(rc.canMove(dir)){
			rc.move(dir);
		}
		else{
			//TODO Pathfind if obstruction.
		}
	}

	private static void updateIfInDanger() {
		visibleEnemies = rc.senseNearbyRobots(10, enemy);

		if(visibleEnemies.length > 0){
			danger = true; //TODO could be cost inefficient to be doing this. 
		}
		else if (visibleEnemies.length == 0){
			danger = false;
		}
	}

	//TODO directionTo is rather expensive, figure out an algorithm that may be cheaper. 
	private static Direction getEvadeDir() {
		MapLocation nearestEnemyLocation = visibleEnemies[0].location; 
		return (nearestEnemyLocation.directionTo(archonLoc));
		//TODO directionTo is rather expensive, figure out an algorithm that may be cheaper. 
	}

	/**
	 * Used to place location of the Archon into our broadcasting system.
	 *
	 * @throws GameActionException
	 */
	private static void broadcast() throws GameActionException{
		MapLocation myLocation = rc.getLocation();
		rc.broadcast(0,(int)myLocation.x);
		rc.broadcast(1,(int)myLocation.y);
	}

	/**
	 * This method should be called at the very beginning of the game by the archon to set up all of our initial information. 
	 */

	private static void setUpInitialGlobals(){
		Globals.numberOfInitialArchon = rc.getInitialArchonLocations(Globals.friendly).length;

		MapLocation[] initialLocations = rc.getInitialArchonLocations(Globals.enemy);

		for(MapLocation x : initialLocations){
			Globals.InitialEnemyArchonLocationStatus.put(x, false);
		}
		Globals.initialFriendlyArchonLocations = rc.getInitialArchonLocations(Globals.friendly);
	}
}