package masslumber;

import battlecode.common.*;

import java.awt.*;

public class BotLumberJack extends RobotPlayer {



    //Initiate at start of turn
    public static TreeInfo[] nearbyTrees;
    public static RobotInfo[] nearbyRobots;

    public static void loop() throws GameActionException {
        while (true) {
            try {
                victoryPointsEndgameCheck();
                nearbyTrees = rc.senseNearbyTrees();
                nearbyRobots = rc.senseNearbyRobots();
                //dodge();
                moveAwayFromAllies();
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
                for (TreeInfo t : nearbyTrees) {
                    tryToShake(t);
                    if (t.getTeam() != rc.getTeam() && rc.canChop(t.getLocation())) {
                        rc.chop(t.getLocation());
                        break;
                    }
                }
                rally();
                lumberjackNeededRally();
                lumberjackWander();
                Clock.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void lumberjackNeededRally() throws GameActionException {
        MapLocation location = decodeBroadcastLoc(rc.readBroadcast(NEED_LUMBERJACK_FOR_CLEARING));
        if (location != null) {
            debug_println("Lumberjack requested: " + location.toString());
            if (location.distanceTo(rc.getLocation()) <= rc.getType().sensorRadius) {
                //If nearby check there is a tree near the location
                boolean treeWorthChopping = false;
                int treeID = rc.readBroadcast(NEED_LUMBERJACK_FOR_CLEARING_TREE_ID);
                for (TreeInfo t : nearbyTrees) {
                    if (t.getID() == treeID) {
                        treeWorthChopping = true;
                        navigateTo(t.getLocation());
                        return;
                    }
                }
                if (!treeWorthChopping) {//No tree worth chopping at location
                    rc.broadcast(NEED_LUMBERJACK_FOR_CLEARING, 0);
                }
            }


        }
    }

    public static void lumberjackWander() throws GameActionException {
        //Step slightly away from gardeners
        //debug_println("Lumberjack wandering");
        MapLocation base = decodeBroadcastLoc(rc.readBroadcast(BASE_LOCATION_CHANNEL));
        MapLocation me = rc.getLocation();
        //Move away from base
        for (RobotInfo bot : nearbyRobots) {
            if (bot.getTeam() == rc.getTeam()) {
                if ((bot.getType() == RobotType.GARDENER || bot.getType() == RobotType.ARCHON)
                        && bot.getLocation().distanceTo(base) > me.distanceTo(base)) {
                    if (repel(base) != null) {
                        break;
                    }
                }
            }
        }

        //If you aren't repeling the base, then you are to far. Go towards the base
        if (base != null) {
            //debug_println("NAVING TO BASE");
            navigateTo(base);
            if (base.distanceTo(rc.getLocation()) < RobotType.GARDENER.bodyRadius) {
                rc.broadcast(BASE_LOCATION_CHANNEL, 0);
            }
        }
        //wander();
    }


    public static void moveAwayFromAllies() throws GameActionException{
        //Move away from ally units
        for (RobotInfo bot : nearbyRobots) {
            if (bot.getTeam() == rc.getTeam()) {
                if ((bot.getType() == RobotType.GARDENER || bot.getType() == RobotType.ARCHON)
                        && rc.getLocation().distanceTo(bot.getLocation()) < MIN_RADIUS_FROM_GARDENERS) {
                    if (repel(bot.getLocation()) != null) {
                        break;
                    }
                } else if (bot.getType() == RobotType.LUMBERJACK
                        && rc.getLocation().distanceTo(bot.getLocation()) < MIN_RADIUS_FROM_LUMBERJACKS) {
                    if (repel(bot.getLocation()) != null) {
                        break;
                    }
                }
            }
        }
    }

    public static void lumberjackNavigateTo(MapLocation loc) throws GameActionException {
        //moveAwayFromAllies();
        goingDir = rc.getLocation().directionTo(loc);
        if (!rc.hasMoved()) {
            int leftOrRight = goRight ? -1 : 1;
            for (int i = 0; i < 72; i++) {
                Direction offset = new Direction(goingDir.radians + (float) (leftOrRight * 2 * Math.PI * ((float) i) / 72));
                if (rc.canMove(offset) && !rc.hasMoved()) {
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
