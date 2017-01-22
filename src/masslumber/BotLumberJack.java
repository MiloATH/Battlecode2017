package masslumber;

import battlecode.common.*;

import java.awt.*;

public class BotLumberJack extends RobotPlayer {

    //Lumberjack constants
    public static float MIN_RADIUS_FROM_GARDENERS =
            2 * GameConstants.BULLET_TREE_RADIUS + RobotType.GARDENER.bodyRadius + RobotType.LUMBERJACK.bodyRadius + 2f;

    public static float MIN_RADIUS_FROM_LUMBERJACKS =
            2 * RobotType.LUMBERJACK.bodyRadius + GameConstants.LUMBERJACK_STRIKE_RADIUS + 0.01f;

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
            navigateTo(location);
        }
    }

    public static void lumberjackWander() throws GameActionException {
        //Step slightly away from gardeners
        debug_println("Lumberjack wandering");
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


}
