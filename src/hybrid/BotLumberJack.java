package hybrid;

import battlecode.common.*;

public class BotLumberJack extends RobotPlayer {


    //Initiate at start of turn
    public static TreeInfo[] nearbyTrees;
    public static RobotInfo[] nearbyRobots;
    public static MapLocation base;
    public static float LUMBERJACK_ATTACK_RADIUS = 1f;//SEE SPECS FOR RADIUS. THIS IS THE RADIUS FROM THE EDGE OF THE LUMBERJACK

    public static void loop() throws GameActionException {
        while (true) {
            try {
                /*
                * NOTE: The order of theses methods matters a lot!!!
                * */
                victoryPointsEndgameCheck();
                nearbyTrees = rc.senseNearbyTrees();
                nearbyRobots = rc.senseNearbyRobots();
                base = decodeBroadcastLoc(rc.readBroadcast(BASE_LOCATION_CHANNEL));
                //dodge();
                RobotInfo[] bots = rc.senseNearbyRobots();
                for (RobotInfo b : bots) {
                    if (b.getTeam() != rc.getTeam()){
                        if(rc.canStrike() && rc.getLocation().distanceTo(b.getLocation()) - rc.getType().bodyRadius - b.getType().bodyRadius < LUMBERJACK_ATTACK_RADIUS)
                        {
                            rc.strike();
                        }
                        Direction chase = rc.getLocation().directionTo(b.getLocation());
                        if (rc.canMove(chase) && !rc.hasMoved()) {
                            rc.move(chase);
                        }
                        break;
                    }
                }
                boolean choppingTree = false;
                for (TreeInfo t : nearbyTrees) {
                    tryToShake(t);
                    if (t.getTeam() != rc.getTeam() && rc.canChop(t.getLocation())) {
                        rc.chop(t.getLocation());
                        choppingTree = true;
                        if (!rc.canInteractWithTree(t.getID())) {
                            navigateTo(t.getLocation());//Maybe??
                        }
                        break;
                    }
                }
                /*if (!choppingTree) {
                    moveAwayFromAllies();
                }*/
                rally();
                lumberjackNeededRally();
                if (!choppingTree) {
                    lumberjackWander();
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
            debug_println("Lumberjack requested: " + location.toString());
            //Help gardener only if I can see the tree and am closer to the base then the tree
            if (location.distanceTo(rc.getLocation()) <= rc.getType().sensorRadius
                    && (base == null || location.distanceTo(rc.getLocation()) < base.distanceTo(rc.getLocation()))) {
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
        MapLocation me = rc.getLocation();
        //Move away from base
        /*for (RobotInfo bot : nearbyRobots) {
            if (bot.getTeam() == rc.getTeam()) {
                if ((bot.getType() == RobotType.GARDENER || bot.getType() == RobotType.ARCHON)
                        && bot.getLocation().distanceTo(base) > me.distanceTo(base)) {
                    if (repel(base) != null) {
                        break;
                    }
                }
            }
        }*/

        moveTowardsEnemyArchonInitial();

        //If all archons killed, then swarm without scrunching
        moveAwayFromAllies();

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


    public static void moveAwayFromAllies() throws GameActionException {
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

}
