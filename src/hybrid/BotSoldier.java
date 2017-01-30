package hybrid;

import battlecode.common.*;

public class BotSoldier extends RobotPlayer{

    static void loop() throws GameActionException {
        Team enemy = rc.getTeam().opponent();

        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                Direction moved = null;
                victoryPointsEndgameCheck();
                dodge();
                if(rc.getRoundNum()<EARLY_GAME){//stay near gardeners at start
                    RobotInfo[] bots = rc.senseNearbyRobots();
                    for(RobotInfo b: bots){
                        if(b.getTeam()!=rc.getTeam()){
                            if(b.getLocation().distanceTo(rc.getLocation()) > 10){
                                moved = navigateTo(b.getLocation());
                                //break;
                            }
                            else{
                                MapLocation meNow = repel(b.getLocation());
                                moved = rc.getLocation().directionTo(meNow);
                                debug_println("REPELED: " + moved);
                                //break;
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
                        Direction tempMoved = navigateTo(gardenerInNeed);
                        moved = tempMoved!=null ? tempMoved : moved;
                    }
                }

                shakeNeighbors();
                MapLocation myLocation = rc.getLocation();
                // See if there are any nearby enemy robots
                RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, enemy);

                for(RobotInfo b: enemyRobots){
                    Direction fireAngle = rc.getLocation().directionTo(b.getLocation());
                    if(rc.canFirePentadShot() && (moved==null || Math.abs(moved.degreesBetween(fireAngle)) > 30)){
                        rc.firePentadShot(fireAngle);
                    }
                }

                // Move towards Enemy archons
                if(enemyRobots.length == 0) {
                    moveTowardsEnemyArchonInitial();
                    //Else
                    wander();
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}