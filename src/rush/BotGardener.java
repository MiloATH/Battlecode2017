package rush;

import battlecode.common.*;

import java.util.ArrayList;

public class BotGardener extends RobotPlayer {


    static void loop() throws GameActionException {
        if (rc.getRoundNum() == 1) {
            hasHome = false;
        }

        trees = new ArrayList<MapLocation>();
        if (rc.getRoundNum() >= ROUND_TO_BROADCAST_TREE_DENSITY + 1) {
            int treeDensity = rc.readBroadcast(TREE_DENSITY_CHANNEL);
            if (treeDensity > 25) {
                INITIAL_MOVES_BASE = 15;
            }
            if (treeDensity > 80) {
                INITIAL_MOVES_BASE = 20;
            }
        }
        while (true) {
            try {
                victoryPointsEndgameCheck();
                if (!startedPlanting) {
                    dodge();
                }
                RobotInfo[] bots = rc.senseNearbyRobots();
                //TODO: Make tree density more general
                if (rc.getRoundNum() == ROUND_TO_BROADCAST_TREE_DENSITY + 1) {
                    int treeDensity = rc.readBroadcast(TREE_DENSITY_CHANNEL);
                    if (treeDensity > 0 && treeDensity <= 5) {//Few trees
                        //For now, be the same as default build
                    } else if (treeDensity <= 25) {//Medium amount of trees
                        build = manyLumberjackBuild;
                    } else {//THATS ALOT OF TREES!
                        build = almostAllLumberjackBuild;
                    }
                }

                //Find good place to plant trees
                int moveUntilRound = 10;//5 * (GARDENER_MAX * rc.getRoundNum() / rc.getRoundLimit() + 1);
                if (numberOfRoundsAlive < moveUntilRound) {
                    wander();
                }
                Boolean nearAllyTrees = false;
                TreeInfo[] nearbyTrees = rc.senseNearbyTrees();
                for (TreeInfo tree : nearbyTrees) {
                    if (tree.getTeam() == rc.getTeam() && tree.getLocation().distanceTo(rc.getLocation()) < MIN_GARDENER_SPACING) {//NOTE TREE TO GARDENER spacing
                        nearAllyTrees = true;
                    }
                }
                if (nearAllyTrees && !startedPlanting) {
                    wander();
                }

                //Check if under attack
                //If not under attack and broadcast is my location, set broadcast to 0
                boolean enemyFound = false;
                for (RobotInfo bot : bots) {
                    if (bot.getTeam() != rc.getTeam()) {
                        rc.broadcast(GARDENER_UNDER_ATTACK, encodeBroadcastLoc(rc.getLocation()));
                        //System.out.println("BROADCASTED I AM UNDER ATTACK");
                        enemyFound = true;
                    }
                }
                if(!enemyFound){
                    MapLocation broadcastLocation = decodeBroadcastLoc(rc.readBroadcast(GARDENER_UNDER_ATTACK));
                    //System.out.println("NO ENEMY SEEN. Broadcast loc: " + (broadcastLocation!=null?broadcastLocation.toString():"NULL"));
                    if(broadcastLocation!=null){
                        //System.out.println("Delta x: " + Math.abs(broadcastLocation.x - rc.getLocation().x));
                        //System.out.println("Delta y: " + Math.abs(broadcastLocation.y - rc.getLocation().y));
                    }

                    if(broadcastLocation!=null &&
                            Math.abs(broadcastLocation.x - rc.getLocation().x) < 2 &&
                            Math.abs(broadcastLocation.y - rc.getLocation().y) < 2){
                        rc.broadcast(GARDENER_UNDER_ATTACK,0);
                    }
                }

                int prevLum = rc.readBroadcast(LUMBERJACK_CHANNEL);
                int prevScouts = rc.readBroadcast(SCOUTS_CHANNEL);
                int prevTree = rc.readBroadcast(TREE_CHANNEL);
                int prevSold = rc.readBroadcast(SOLDIER_CHANNEL);
                int buildNum = (prevLum + prevScouts + prevTree + prevSold) % build.length;
                if (buildNum < build.length) {
                    switch (build[buildNum]) {
                        case 0://Tree
                            ////System.out.println("Try to plant");
                            /*System.out.println("Started Planting?: " + startedPlanting + "\nNear Ally Gardeners: " + nearAllyTrees +
                                    "\n #rounds Alive more than/equal moveUntilRound?: " + (numberOfRoundsAlive >= moveUntilRound));*/
                            if ((startedPlanting || !nearAllyTrees) &&
                                    numberOfRoundsAlive >= moveUntilRound &&
                                    (tryToPlant() || rc.getTeamBullets() > 2 * SURPLUS_BULLETS)) {//Short circut evaluation. Only plants after moveUntilRound
                                rc.broadcast(TREE_CHANNEL, prevTree + 1);
                            }
                            break;
                        case 1://Scout
                            if (prevScouts <= SCOUT_MAX) {
                                rc.broadcast(SCOUTS_CHANNEL, prevScouts + tryToBuild(RobotType.SCOUT));
                            }
                            break;
                        case 2://Lumberjack
                            if (prevLum <= LUMBERJACK_MAX) {
                                rc.broadcast(LUMBERJACK_CHANNEL, prevLum + tryToBuild(RobotType.LUMBERJACK));
                            }
                            break;
                        case 3://Soldiers
                            rc.broadcast(SOLDIER_CHANNEL, prevSold + tryToBuild(RobotType.SOLDIER));
                            break;
                    }
                }

                //now try to water trees
                //DONT MOVE
                tryToWater();
                numberOfRoundsAlive++;
                if(!startedPlanting){
                    stillLookingForPlanting();
                }
                Clock.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
