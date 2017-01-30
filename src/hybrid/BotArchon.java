package hybrid;

import battlecode.common.*;

import java.util.Arrays;

public class BotArchon extends RobotPlayer {


    static void loop() throws GameActionException {
        //Initial rally point
        //MapLocation me = rc.getLocation();
        //rc.broadcast(RALLY_LOCATION_CHANNEL, encodeBroadcastLoc(new MapLocation(me.x + 5, me.y + 5)));
        while (true) {
            try {
                //Initiate enemy archon locations
                if(rc.getRoundNum()<=1 && rc.readBroadcast(ENEMY_ARCHON_LOCATIONS_CHANNELS) == 0){
                    MapLocation[] enemyArchons = rc.getInitialArchonLocations(rc.getTeam().opponent());
                    Arrays.sort(enemyArchons, (a, b) -> -compareBotsForInitialSorting(a,b));//Sort by decreasing distance. furthest first
                    debug_println("Setting enemy archon locations: " + enemyArchons.length);
                    for(int i = enemyArchons.length; --i>=0;){//Store them in reverse order.
                        debug_println("" + (i+ENEMY_ARCHON_LOCATIONS_CHANNELS) + " has location " + enemyArchons[i].toString());
                        rc.broadcast(ENEMY_ARCHON_LOCATIONS_CHANNELS + i,
                                encodeBroadcastLoc(enemyArchons[i]));
                    }
                }


                victoryPointsEndgameCheck();
                dodge();
                MapLocation base = decodeBroadcastLoc(rc.readBroadcast(BASE_LOCATION_CHANNEL));
                if (base != null) {
                    navigateTo(base);
                }
                if (rc.getRoundNum() < VERY_EARLY_GAME) {
                    GARDENER_MAX = 1;
                } else {
                    GARDENER_MAX = 30;
                }

                if (rc.getRoundNum() < EARLY_GAME) {
                    MAX_NUMBER_OF_GARDENER_LOOKING = 2;
                }
                if (rc.getRoundNum() >= EARLY_GAME && rc.getRoundNum() < MID_GAME) {
                    MAX_NUMBER_OF_GARDENER_LOOKING = 3;

                }
                if (rc.getRoundNum() >= MID_GAME) {
                    MAX_NUMBER_OF_GARDENER_LOOKING = 5;
                }
                //TODO: make it better or remove middleman
                //Set rally point if enemy seen and above a round #
                int enemySeen = rc.readBroadcast(ENEMY_SEEN_CHANNEL);
                int gardenerUnderAttack = rc.readBroadcast(GARDENER_UNDER_ATTACK);
                if (rc.getRoundNum() == ROUND_TO_BROADCAST_TREE_DENSITY + 1) {
                    int treeDensity = rc.readBroadcast(TREE_DENSITY_CHANNEL);
                    if (treeDensity > 30) {
                        ATTACK_ROUND = 950;
                    }
                    if (treeDensity > 60) {
                        ATTACK_ROUND = 1250;
                    }
                }
                //System.out.println("GARDENER UNDER ATTACK INPUT: " + gardenerUnderAttack);
                /*if (gardenerUnderAttack != 0) {
                    //System.out.println("Rally at gardener under attack");
                    rc.broadcast(RALLY_LOCATION_CHANNEL, gardenerUnderAttack);
                } else {
                    rc.broadcast(RALLY_LOCATION_CHANNEL, 0);
                }*/
                    /*else if (enemySeen != 0) {
                    if (rc.getRoundNum() > ATTACK_ROUND) {//ATTACK
                        rc.broadcast(RALLY_LOCATION_CHANNEL, enemySeen);
                    } else if (rc.getRoundNum() > ATTACK_ROUND - 200) {
                        MapLocation me = rc.getLocation();
                        MapLocation enemy = decodeBroadcastLoc(enemySeen);
                        MapLocation quarterOfTheWay = new MapLocation(me.x + (enemy.x - me.x) / 4, me.y + (enemy.y - me.y) / 4);
                        rc.broadcast(RALLY_LOCATION_CHANNEL, encodeBroadcastLoc(quarterOfTheWay));
                    }
                }*/

                //Build gardener if less than max
                int prevNumGard = rc.readBroadcast(GARDENER_CHANNEL);

                //Read the current one
                int numberOfGardenerLooking = rc.readBroadcast(GARDENER_LOOKING_FOR_PLANTING + (rc.getRoundNum() - 1) % 3);
                if (prevNumGard < GARDENER_MAX && numberOfGardenerLooking < MAX_NUMBER_OF_GARDENER_LOOKING) {
                    int numberOfGardenersMade = prevNumGard + fineDeltaTryToBuild(RobotType.GARDENER, RobotType.GARDENER.bulletCost);
                    //update number of gardeners made
                    if (numberOfGardenersMade != prevNumGard) {
                        rc.broadcast(GARDENER_CHANNEL, numberOfGardenersMade);
                    }

                }
                //Reset the one from a few rounds ago.
                rc.broadcast(GARDENER_LOOKING_FOR_PLANTING + (rc.getRoundNum() - 2) % 3, 0);
                Clock.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
