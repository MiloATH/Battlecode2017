package onegardenerrush;

import battlecode.common.*;


public class BotGardener extends RobotPlayer {

    public static RobotInfo[] bots;

    static void loop() throws GameActionException {
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
                //Initiate bots
                bots = rc.senseNearbyRobots();


                Boolean nearAllyTrees = false;

                //Set base location
                MapLocation base = decodeBroadcastLoc(rc.readBroadcast((BASE_LOCATION_CHANNEL)));
                if (base == null) {
                    rc.broadcast(BASE_LOCATION_CHANNEL, encodeBroadcastLoc(rc.getLocation()));
                    base = rc.getLocation();
                } else if (!startedPlanting && base.distanceTo(rc.getLocation()) > RobotType.GARDENER.bodyRadius) {
                    //Find good place to plant trees
                    //Move away from ally trees
                    TreeInfo[] nearbyTrees = rc.senseNearbyTrees();
                    for (TreeInfo tree : nearbyTrees) {
                        if (tree.getTeam() == rc.getTeam() && tree.getLocation().distanceTo(rc.getLocation()) < MIN_GARDENER_SPACING) {//NOTE TREE TO GARDENER spacing
                            nearAllyTrees = true;
                            break;
                        }
                    }
                    if (nearAllyTrees) {
                        gardenerMoveAway();
                    }
                }

                //Not near anyone, just wander.
                int moveUntilRound = 10;//5 * (GARDENER_MAX * rc.getRoundNum() / rc.getRoundLimit() + 1);
                //gardenerWander();
                if (numberOfRoundsAlive < moveUntilRound) {
                    gardenerWander();
                }

                //Check if under attack
                //If not under attack and broadcast is my location, set broadcast to 0
                boolean enemyFound = false;
                for (RobotInfo bot : bots) {
                    if (bot.getTeam() != rc.getTeam()) {
                        rc.broadcast(GARDENER_UNDER_ATTACK, encodeBroadcastLoc(bot.getLocation()));
                        //debug_println("ENEMY SEEN");
                        enemyFound = true;
                    }
                }
                if (!enemyFound) {
                    MapLocation broadcastLocation = decodeBroadcastLoc(rc.readBroadcast(GARDENER_UNDER_ATTACK));
                    //debug_println("NO ENEMY SEEN");
                    if (broadcastLocation != null
                            && rc.getLocation().distanceTo(broadcastLocation) <= rc.getType().sensorRadius) {
                        rc.broadcast(GARDENER_UNDER_ATTACK, 0);
                        debug_println("RALLY CALLED OFF");
                    }
                }
                int prevLum = rc.readBroadcast(LUMBERJACK_CHANNEL);
                int prevScouts = rc.readBroadcast(SCOUTS_CHANNEL);
                int prevTree = rc.readBroadcast(TREE_CHANNEL);
                int prevSold = rc.readBroadcast(SOLDIER_CHANNEL);
                int buildNum = (prevLum + prevScouts + prevTree + prevSold) % build.length;
                if (prevScouts == 0) {
                    if (tryToBuild(RobotType.SCOUT) == 1) {
                        rc.broadcast(SCOUTS_CHANNEL, prevScouts + 1);
                    }
                }
                if (buildNum < build.length) {
                    switch (build[buildNum]) {
                        case 0://Tree
                            //debug_println("Try to plant");
                            /*debug_println("Started Planting?: " + startedPlanting + "\nNear Ally Gardeners: " + nearAllyTrees +
                                    "\n #rounds Alive more than/equal moveUntilRound?: " + (numberOfRoundsAlive >= moveUntilRound));*/
                            //TODO: Only build if close to base or allies that are buildings
                            if ((startedPlanting || !nearAllyTrees) &&
                                    numberOfRoundsAlive >= moveUntilRound &&
                                    (tryToPlant() || rc.getTeamBullets() > 2 * SURPLUS_BULLETS)) {//Short circut evaluation. Only plants after moveUntilRound
                                rc.broadcast(TREE_CHANNEL, prevTree + 1);
                            }
                            break;
                        case 1://Scout
                            rc.broadcast(SCOUTS_CHANNEL, prevScouts + tryToBuild(RobotType.SCOUT));
                            break;
                        case 2://Lumberjack
                            if (tryToBuild(RobotType.LUMBERJACK) == 1) {
                                rc.broadcast(LUMBERJACK_CHANNEL, prevLum + 1);
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
                if (!startedPlanting) {
                    stillLookingForPlanting();
                }


                Clock.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void gardenerWander() throws GameActionException {
        MapLocation base = decodeBroadcastLoc(rc.readBroadcast(BASE_LOCATION_CHANNEL));
        //Wander if near ally gardeners
        for (RobotInfo b : bots) {
            if (b.getType() == RobotType.GARDENER && b.getTeam() == rc.getTeam()) {
                wander();
                return;
            }
        }
        //If not near ally gardeners, go home.
        if (base != null) {
            //debug_println("NAVING TO BASE");
            navigateTo(base);
        } else {
            wander();
        }
    }

    public static void gardenerMoveAway() throws GameActionException {
        //Move away from ally units
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
        for (RobotInfo bot : nearbyRobots) {
            if (bot.getTeam() == rc.getTeam()) {
                if ((bot.getType() == RobotType.GARDENER || bot.getType() == RobotType.ARCHON)
                        && rc.getLocation().distanceTo(bot.getLocation()) < MIN_RADIUS_FROM_GARDENERS) {
                    if (repel(bot.getLocation()) != null) {
                        break;
                    }
                } else if (bot.getType() == RobotType.LUMBERJACK
                        && rc.getLocation().distanceTo(bot.getLocation()) < MIN_RADIUS_FROM_LUMBERJACKS) {
                    if (repel(bot.getLocation()) != null) {
                        break;
                    }
                }
            }
        }
    }
}
