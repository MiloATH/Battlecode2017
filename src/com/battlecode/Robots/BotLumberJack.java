package com.battlecode.Robots;

import com.battlecode.Helpers.RobotPersonality;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;


public class BotLumberJack extends Globals {


	static boolean danger, hasAttacked;
	static boolean kamikazee;

	static MapLocation myLocation;

	static RobotInfo[] enemyRobots;
	static Direction movement;
	public static RobotPersonality myPersonality;


	public void runLumberJack() {
		try{

			switch (myPersonality){

			case CHOPPER:
				behaveAsChopper();
				break;
			case KAMIKAZEE:
				behaveAsKamikazee();
				break;
			case NORMALLUMBERJACK:
				behaveAsNormalLumberJack();
			default:
				behaveAsNormalLumberJack();
				break;
			}

		}catch (Exception e) {
			System.out.println("LumberJack Exception");
			e.printStackTrace();
		}
	}




	private void behaveAsKamikazee() throws GameActionException {
		setDangerStatus();
		if(danger){
			
			if(rc.canStrike()){
				kamikazeeHasAttacked();
			}
			if(hasAttacked){
				rc.move(movement);
			}
			else{
				rc.move(determineNewDirectionToMove());
			}
		}

		else{
			//TODO MotionLogic
		}

	}




	private void behaveAsChopper() {
		setDangerStatus();

		if(danger){

		}
		else{
			//TODO MotionLogic
		}

	}




	private void behaveAsNormalLumberJack() {
		setDangerStatus();

		if(danger){

		}
		else{
			//TODO MotionLogic
		}
	}



	private void setDangerStatus(){
		enemyRobots = rc.senseNearbyRobots(-1, friendly.opponent());

		if(enemyRobots.length > 0){
			danger = true; //TODO could be cost inefficient to be doing this. 
		}
		else if (enemyRobots.length == 0){
			danger = false;
		}
	}

	private static void kamikazeeHasAttacked() throws GameActionException {
		RobotInfo[] nearbyRobots = rc.senseNearbyRobots(1);
		int nearbyEnemies = 0;
		int nearbyFriendlies = 0;
		for(RobotInfo x: nearbyRobots){
			if(x.getTeam().equals(Globals.friendly)){
				nearbyFriendlies += 1;
			}
			else{
				nearbyEnemies += 1;
			}
		}
		if(nearbyEnemies <= nearbyFriendlies){
			hasAttacked = false;
		}
		else{
			rc.strike();
			hasAttacked = true;
		}

	}

	private static Direction determineNewDirectionToMove(){
		Direction toReturn = movement;
		for(RobotInfo x: enemyRobots){
			Direction toCheck  = myLocation.directionTo(x.getLocation());
			if(toCheck != movement){
				movement = toCheck;
				return movement;
			}
		}
		return toReturn;
	}

	public static RobotPersonality getMyPersonality() {
		return myPersonality;
	}

	public static void setMyPersonality(RobotPersonality myPersonality) {
		BotLumberJack.myPersonality = myPersonality;
	}
}