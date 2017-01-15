package rush;

import battlecode.common.*;

public class BotLumberJack extends RobotPlayer {

    public static void loop() throws GameActionException {
        while (true) {
            try {
                victoryPointsEndgameCheck();
                dodge();
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
                    System.out.println("Just shaked: " + t.getID());
                    if (t.getTeam() != rc.getTeam() && rc.canChop(t.getLocation())) {
                        System.out.println("About to chop: " + t.getLocation().x + ", " + t.getLocation().y);
                        rc.chop(t.getLocation());
                        System.out.println("Chopped");
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
}
