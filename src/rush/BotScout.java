package rush;
import battlecode.common.*;

public class BotScout extends RobotPlayer {

    public static void loop() throws GameActionException {
        while (true) {
            try {
                RobotPlayer.victoryPointsEndgameCheck();
                RobotPlayer.dodge();
                //TODO optimise to not have to use rc.hasMoved()
                //Attack other team's gardeners only
                RobotInfo[] bots = rc.senseNearbyRobots();


                for (RobotInfo b : bots) {
                    if (b.getTeam() != rc.getTeam() && b.getType() == RobotType.GARDENER && rc.canFireSingleShot()) {
                        Direction opponent = rc.getLocation().directionTo(b.getLocation());
                        rc.fireSingleShot(opponent);
                        if (rc.readBroadcast(ENEMY_GARDENER_SEEN_CHANNEL) == 0) {
                            rc.broadcast(ENEMY_GARDENER_SEEN_CHANNEL, encodeBroadcastLoc(b.getLocation()));
                        }
                        if (!rc.hasMoved() && rc.canMove(opponent)) {
                            rc.move(opponent, (float) (b.getLocation().distanceTo(b.getLocation()) - 0.25));
                        }
                        break;
                    }
                }
                //Didn't move
                if (!rc.hasMoved()) {
                    MapLocation loc = decodeBroadcastLoc(rc.readBroadcast(ENEMY_GARDENER_SEEN_CHANNEL));
                    MapLocation me = rc.getLocation();
                    if (loc != null && loc.x - me.x < .5 && loc.y - me.y < .5 && !rc.hasAttacked()) {//Enemy was probably destroyed or escaped
                        rc.broadcast(ENEMY_GARDENER_SEEN_CHANNEL, 0);
                        if (rc.getRoundNum() < 200) {

                        }
                    }
                    if (loc != null && rc.canMove(loc)) {//Go to seen gardener
                        rc.move(loc);
                    } else {//wander
                        wander();
                    }
                }
                Clock.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
