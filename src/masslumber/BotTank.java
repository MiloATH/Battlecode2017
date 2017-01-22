package masslumber;

import battlecode.common.*;

public class BotTank extends RobotPlayer {

    public static void loop() throws GameActionException {//TODO: EVERYTHING
        while (true) {
            try {
                victoryPointsEndgameCheck();
                RobotInfo[] bots = rc.senseNearbyRobots();

                for (RobotInfo b : bots) {
                    if (b.getTeam() != rc.getTeam()) {
                        Direction towardsEnemy = rc.getLocation().directionTo(b.getLocation());
                        if (rc.canMove(towardsEnemy) && !rc.hasMoved()) {
                            rc.move(towardsEnemy);
                        }
                        if (rc.canFireSingleShot()) {
                            rc.fireSingleShot(towardsEnemy);
                        }
                        break;
                    }
                }
                if (!rc.hasAttacked()) {

                }
                Clock.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
