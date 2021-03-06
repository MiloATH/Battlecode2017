package rush;

import battlecode.common.*;

public class BotLumberJack extends RobotPlayer {

    public static void loop() throws GameActionException {
        while (true) {
            try {
                victoryPointsEndgameCheck();
                dodge();
                rally();
                lumberjackNeededRally();
                RobotInfo[] bots = rc.senseNearbyRobots();
                for (RobotInfo b : bots) {
                    if (b.getTeam() != rc.getTeam() && rc.canStrike()) {
                        rc.strike();
                        Direction chase = rc.getLocation().directionTo(b.getLocation());
                        if (rc.canMove(chase) && !rc.hasMoved()) {
                            rc.move(chase);
                        }
                        break;
                    }
                }
                TreeInfo[] trees = rc.senseNearbyTrees();
                for (TreeInfo t : trees) {
                    tryToShake(t);
                    if (t.getTeam() != rc.getTeam() && rc.canChop(t.getLocation())) {
                        rc.chop(t.getLocation());
                        break;
                    }
                }
                if (!rc.hasAttacked()) {
                    wander();
                }
                Clock.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void lumberjackNeededRally() throws GameActionException {
        MapLocation location = decodeBroadcastLoc(rc.readBroadcast(NEED_LUMBERJACK_FOR_CLEARING));
        if (location != null) {
            navigateTo(location);
        }
    }
}
