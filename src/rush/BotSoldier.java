package rush;

import battlecode.common.*;

public class BotSoldier extends RobotPlayer{

    static void loop() throws GameActionException {
        Team enemy = rc.getTeam().opponent();

        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                RobotPlayer.victoryPointsEndgameCheck();
                RobotPlayer.dodge();
                shakeNeighbors();

                MapLocation myLocation = rc.getLocation();

                // See if there are any nearby enemy robots
                RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);
                Direction enemyDirection = rc.getLocation().directionTo(robots[0].location);

                // If there are some...
                if (!rc.hasAttacked()){
                    if(rc.canFirePentadShot() && robots.length > 3){
                        rc.firePentadShot(enemyDirection);
                    }
                    else if(rc.canFireTriadShot() && robots.length > 1){
                        rc.fireTriadShot(enemyDirection);
                    }
                    else if(rc.canFireSingleShot()){
                        rc.fireSingleShot(enemyDirection);
                    }
                }
                // Move randomly
                RobotPlayer.wander();

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}