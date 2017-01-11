package com.battlecode.Robots;

import battlecode.common.*;
import examplefuncsplayer.RobotPlayer;

public class BotArchon extends Globals {

	static boolean setUp;
	static MapLocation archonLoc;
	static Direction movement;
	static boolean danger;
	static RobotInfo[] enemyRobots;

	public static void runArchon() {

		// The code you want your robot to perform every round should be in this loop
		while (true) {

			// Try/catch blocks stop unhandled exceptions, which cause your robot to explode
			try {

				//Testing if it's in Danger

				enemyRobots = rc.senseNearbyRobots(10, friendly.opponent());

				if(enemyRobots.length > 0){
					danger = true; //TODO could be cost inefficient to be doing this. 
				}
				else if (enemyRobots.length == 0){
					danger = false;
				}

				if(danger){
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

				else{

					// Move randomly
					if(setUp != true){
						//TODO setup algorithm, right now we'll just set setup to be true.
						setUp = true;
					}
					if(setUp == true){
						movement = RobotPlayer.randomDirection();

						if(rc.canHireGardener(movement) && Math.random() < .01){
							rc.hireGardener(movement);
						}

					}

					RobotPlayer.tryMove(movement);



					Clock.yield();

				}

			}
				catch (Exception e) {
					System.out.println("Archon Exception");
					e.printStackTrace();
				}
		}
	}

		private static Direction getEvadeDir() {
			MapLocation nearestEnemyLocation = enemyRobots[0].location; 
			return (nearestEnemyLocation.directionTo(archonLoc)).opposite();

			//TODO directionTo is rather expensive, figure out an algorithm that may be cheaper. 

		}

		private static void broadcast() throws GameActionException{
			MapLocation myLocation = rc.getLocation();
			rc.broadcast(0,(int)myLocation.x);
			rc.broadcast(1,(int)myLocation.y);
		}
	}