package onegardenerrush;

import battlecode.common.*;

public class BotSoldier extends RobotPlayer {

    static void loop() throws GameActionException {
        Team enemy = rc.getTeam().opponent();

        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                victoryPointsEndgameCheck();
                dodge();
                MapLocation base = decodeBroadcastLoc(rc.readBroadcast(BASE_LOCATION_CHANNEL));
                if (rc.getRoundNum() < EARLY_GAME) {//stay near gardeners at start

                } else {
                    //If no robot seen, stay next to base gardener
                    if (base != null) {
                        navigateTo(base);
                    } else {
                        debug_println("Wadnering");
                        // Move randomly
                        wander();
                    }
                }
                debug_println("Trying tree destruction");
                if (base != null && rc.getLocation().distanceTo(base) <= 6 * RobotType.TANK.bodyRadius) {//TODO: clear nearby trees
                    TreeInfo[] trees = rc.senseNearbyTrees();
                    for (TreeInfo t : trees) {
                        if (t.getTeam() != rc.getTeam() && rc.canFirePentadShot()) {
                            rc.firePentadShot(rc.getLocation().directionTo(t.getLocation()));
                            break;
                        }
                    }
                }
                debug_println("Trying rally");
                rally();
                debug_println("Checking Gardener");
                //Support gardener under attack
                int input = rc.readBroadcast(GARDENER_UNDER_ATTACK);//TODO: COULD BE REMOVED LATER
                //System.out.println("INPUT: "+ input + " GARDENER LOCATION: ");
                if (input != 0) {
                    MapLocation gardenerInNeed = decodeBroadcastLoc(input);
                    //System.out.println(gardenerInNeed.toString());
                    if (gardenerInNeed.distanceTo(rc.getLocation()) < 0.5) {
                        rc.broadcast(GARDENER_UNDER_ATTACK, 0);
                    } else {
                        navigateTo(gardenerInNeed);
                    }
                }
                debug_println("Shaking trees");
                shakeNeighbors();
                MapLocation myLocation = rc.getLocation();
                // See if there are any nearby enemy robots
                RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);
                debug_println("Looking for nearby enemies");
                // If there are some...
                if (robots.length > 0) {
                    // And we have enough bullets, and haven't attacked yet this turn...
                    if (rc.canFireSingleShot() && !rc.hasAttacked()) {
                        // ...Then fire a bullet in the direction of the enemy.
                        rc.fireSingleShot(rc.getLocation().directionTo(robots[0].location));
                    }
                }
                debug_println("end");
                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}