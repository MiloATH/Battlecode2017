package com.battlecode.Robots;

import battlecode.common.*;
import examplefuncsplayer.RobotPlayer;

public class BotArchon extends Globals {

	static boolean setUp;
	static MapLocation archonLoc;
	static Direction movement;
	static boolean danger;

	public static void runArchon() {
		System.out.println("I'm an archon!");

		// The code you want your robot to perform every round should be in this loop
		while (true) {

			// Try/catch blocks stop unhandled exceptions, which cause your robot to explode
			try {

				//Testing if it's in Danger

				visibleEnemies = rc.senseNearbyRobots(10, enemy);

				if(visibleEnemies.length > 0){
					danger = true; //TODO could be cost inefficient to be doing this. 
				}
				else if (visibleEnemies.length == 0){
					danger = false;
				}

				if(danger = true){
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
					if(setUp){
						movement = RobotPlayer.randomDirection();
                        RobotPlayer.tryMove(movement);
                        if(rc.canHireGardener(movement.opposite()) && Math.random() < .01){
							rc.hireGardener(movement.opposite());
						}

					}





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
			MapLocation nearestEnemyLocation = visibleEnemies[0].location; 
			return (nearestEnemyLocation.directionTo(archonLoc)).opposite();

			//TODO directionTo is rather expensive, figure out an algorithm that may be cheaper. 

		}

		private static void broadcast() throws GameActionException{
			MapLocation myLocation = rc.getLocation();
			rc.broadcast(0,(int)myLocation.x);
			rc.broadcast(1,(int)myLocation.y);
		}
	}