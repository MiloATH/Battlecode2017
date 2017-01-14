package rush;

import battlecode.common.*;

public class BotScout extends RobotPlayer {
    public static Boolean nearbyGardener;
    public static void loop() throws GameActionException {
        while (true) {
            try {
                victoryPointsEndgameCheck();
                dodge();
                shakeNeighbors();
                //TODO optimise to not have to use rc.hasMoved()
                //Attack other team's gardeners only
                RobotInfo[] bots = rc.senseNearbyRobots();
                nearbyGardener = false;
                for (RobotInfo b : bots) {
                    if (b.getTeam() != rc.getTeam() && b.getType() == RobotType.GARDENER ) {
                        nearbyGardener = true;
                        if (rc.canFireSingleShot()) {
                            Direction opponent = rc.getLocation().directionTo(b.getLocation());
                            rc.fireSingleShot(opponent);
                            if (rc.readBroadcast(ENEMY_GARDENER_SEEN_CHANNEL) == 0) {
                                rc.broadcast(ENEMY_GARDENER_SEEN_CHANNEL, encodeBroadcastLoc(b.getLocation()));
                            }
                            //TODO: Don't run over your own bullets
                            if (!rc.hasMoved() && rc.canMove(opponent)) {
                                rc.move(opponent, (float) (b.getLocation().distanceTo(b.getLocation()) - 0.25));
                            }
                            break;
                        }
                    }
                }
                //Didn't move
                if (!rc.hasMoved()) {
                    MapLocation loc = decodeBroadcastLoc(rc.readBroadcast(ENEMY_GARDENER_SEEN_CHANNEL));
                    MapLocation me = rc.getLocation();
                    //If within sight (real sight is 10) of location, there are no nearby enemy gardeners and hasn't attacked
                    if (loc != null && loc.x-me.x< 7 && loc.y-me.y < 7 && !nearbyGardener && !rc.hasAttacked()) {//Enemy was probably destroyed or escaped
                        rc.broadcast(ENEMY_GARDENER_SEEN_CHANNEL, 0);
                        if (rc.getRoundNum() < 200) {

                        }
                    }
                    if (loc != null && rc.canMove(loc)) {//Go to seen gardener
                        rc.move(loc);
                    } else {//wander
                        scoutWander();
                    }
                }
                Clock.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void scoutWander() throws GameActionException {

        //TODO: early game check location of opponent archons. get location of opponent archons with symmetry of our archons
        try {
            if (!rc.hasMoved()) {
                while (Clock.getBytecodesLeft() > 100) {
                    if (rc.canMove(goingDir) && !rc.hasMoved()) {
                        rc.move(goingDir);
                        return;
                    }
                    goingDir = randomDirection();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
