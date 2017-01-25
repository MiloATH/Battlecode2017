package masslumber;

import battlecode.common.*;

public class BotTank extends RobotPlayer {

    public static void loop() throws GameActionException {
        while (true) {
            try {
                victoryPointsEndgameCheck();
                RobotInfo[] bots = rc.senseNearbyRobots();
                MapLocation base = decodeBroadcastLoc(rc.readBroadcast(BASE_LOCATION_CHANNEL));
                MapLocation enemyArchons = decodeBroadcastLoc(rc.readBroadcast(ENEMY_ARCHON_LOCATIONS_CHANNELS));
                for (RobotInfo b : bots) {
                    if (b.getTeam() != rc.getTeam()) {
                        Direction towardsEnemy = rc.getLocation().directionTo(b.getLocation());
                        if (rc.canMove(towardsEnemy) && b.getType() != RobotType.LUMBERJACK && !rc.hasMoved()) {
                            rc.move(towardsEnemy);
                        }
                        if (b.getType() == RobotType.LUMBERJACK) {
                            Direction away = closestMovableDirection(towardsEnemy.opposite());
                            if (rc.canMove(away) && !rc.hasMoved()) {
                                rc.move(away);
                                rc.setIndicatorLine(rc.getLocation(), rc.getLocation().add(away), 0, 0, 255);
                            }
                        }
                        if (rc.canFirePentadShot()) {
                            //TODO: could be optimized, only fire penta if there are more than x enemies or somwthing
                            rc.firePentadShot(towardsEnemy);
                        }
                        break;
                    }
                }

                MapLocation nextArchon = getNextInitialArchonLocation();

                if (nextArchon != null) {
                    tankNavigateTo(nextArchon);
                }
                wander();
                Clock.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public static boolean shouldShoot(Direction dir){//TODO !!!!!!
        return true;
    }

    public static boolean tankShouldMove(MapLocation loc) throws GameActionException {
        TreeInfo[] trees = rc.senseNearbyTrees(rc.getType().strideRadius + rc.getType().bodyRadius);

        float distanceToLoc = rc.getLocation().distanceTo(loc);
        for (TreeInfo t : trees) {
            Direction treeToLoc = t.getLocation().directionTo(loc);
            //debug_println("TEAM: " + t.getTeam());
            //debug_println("DISTANCE: " + t.getLocation().add(treeToLoc, t.getRadius()).distanceTo(loc));
            if (t.getTeam() == rc.getTeam() && t.getLocation().add(treeToLoc, t.getRadius()).distanceTo(loc) <= rc.getType().bodyRadius) {
                return false;
            }
        }
        return true;
    }

    public static boolean tankShouldMove(Direction dir) throws GameActionException {
        MapLocation movedLoc = rc.getLocation().add(dir, rc.getType().strideRadius);
        return tankShouldMove(movedLoc);
    }

    public static void tankNavigateTo(MapLocation loc) throws GameActionException {
        goingDir = rc.getLocation().directionTo(loc);
        if (!rc.hasMoved()) {
            while (Clock.getBytecodesLeft() > 100) {
                int leftOrRight = goRight ? -1 : 1;
                for (int i = 0; i < 72; i++) {
                    Direction offset = new Direction(goingDir.radians + (float) (leftOrRight * 2 * Math.PI * ((float) i) / 72));
                    if (rc.canMove(offset) && !rc.hasMoved() && tankShouldMove(offset)) {
                        if (i > 0) {
                            patienceLeft--;
                            //If lumberjack just stay at it and it will through
                            if (rc.getType() == RobotType.LUMBERJACK && patienceLeft <= 0) {
                                goRight = !goRight;
                                patienceLeft = MAX_LUMBERJACK_PATIENCE;
                            } else if (patienceLeft <= 0) {
                                goRight = !goRight;
                                patienceLeft = MAX_PATIENCE;
                            }
                        }
                        rc.setIndicatorLine(rc.getLocation(), rc.getLocation().add(offset, 10 * rc.getType().strideRadius), 255, 0, 0);
                        rc.move(offset);
                        goingDir = offset;
                        return;
                    }
                }
                //Blocked off, just try to get out
                //  |----> TODO: make it better?
                goingDir = randomDirection();
            }
        }
    }
}
