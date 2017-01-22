package masslumber;

import battlecode.common.*;

public class BotSoldier extends RobotPlayer{

    static void loop() throws GameActionException {
        Team enemy = rc.getTeam().opponent();

        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                victoryPointsEndgameCheck();
                dodge();
                if(rc.getRoundNum()<EARLY_GAME){//stay near gardeners at start
                    RobotInfo[] bots = rc.senseNearbyRobots();
                    for(RobotInfo b: bots){
                        if(b.getTeam()==rc.getTeam() && b.getType()==RobotType.GARDENER){
                            if(b.getLocation().distanceTo(rc.getLocation()) > 10){
                                navigateTo(b.getLocation());
                            }
                        }
                    }
                }
                rally();

                //Support gardener under attack
                int input = rc.readBroadcast(GARDENER_UNDER_ATTACK);//TODO: COULD BE REMOVED LATER
                //System.out.println("INPUT: "+ input + " GARDENER LOCATION: ");
                if(input!=0){
                    MapLocation gardenerInNeed = decodeBroadcastLoc(input);
                    //System.out.println(gardenerInNeed.toString());
                    if(gardenerInNeed.distanceTo(rc.getLocation())<0.5){
                        rc.broadcast(GARDENER_UNDER_ATTACK,0);
                    }
                    else {
                        navigateTo(gardenerInNeed);
                    }
                }

                shakeNeighbors();
                MapLocation myLocation = rc.getLocation();
                // See if there are any nearby enemy robots
                RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);

                // If there are some...
                if (robots.length > 0) {
                    // And we have enough bullets, and haven't attacked yet this turn...
                    if (rc.canFireSingleShot()) {
                        // ...Then fire a bullet in the direction of the enemy.
                        rc.fireSingleShot(rc.getLocation().directionTo(robots[0].location));
                    }
                }

                // Move randomly
                wander();

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}