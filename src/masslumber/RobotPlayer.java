package masslumber;

import battlecode.common.*;

import java.util.*;

public strictfp class RobotPlayer {
    static RobotController rc;
    static Random myRand;
    // Keep broadcast channels
    static int GARDENER_CHANNEL = 50;
    static int LUMBERJACK_CHANNEL = 60;
    static int SCOUTS_CHANNEL = 70;
    static int TREE_CHANNEL = 80;
    static int SOLDIER_CHANNEL = 90;
    static int ENEMY_GARDENER_SEEN_CHANNEL = 100;
    static int RALLY_LOCATION_CHANNEL = 110;
    static int TREE_DENSITY_CHANNEL = 120;
    static int GARDENER_UNDER_ATTACK = 130;
    static int NEED_LUMBERJACK_FOR_CLEARING = 140;
    static int NEED_LUMBERJACK_FOR_CLEARING_TREE_ID = 141;
    static int BASE_LOCATION_CHANNEL = 150;
    static int ENEMY_ARCHON_LOCATIONS_CHANNELS = 850;//Needs at least GameConstants.NUMBER_OF_ARCHONS_MAX above
    static int ENEMY_SEEN_CHANNEL = 900;
    static int GARDENER_LOOKING_FOR_PLANTING = 950;//Needs 3 above

    // Keep important numbers here
    static int GARDENER_MAX = 11;//Subject to change by archon run
    static int MAX_NUMBER_OF_GARDENER_LOOKING = 5;//Changes by in archon run during early game
    static int VERY_EARLY_GAME = 100;
    static int EARLY_GAME = 200;
    static int MID_GAME = 500;
    static int SURPLUS_BULLETS = 160;
    static int MAX_PATIENCE = 50;
    static int ROUND_TO_BROADCAST_TREE_DENSITY = 100;
    static int ATTACK_ROUND = 750;
    static int INITIAL_MOVES_BASE = 6;
    static float MIN_GARDENER_SPACING = 4 * GameConstants.BULLET_TREE_RADIUS + 8 * RobotType.GARDENER.bodyRadius;//10;//12;
    static float MIN_GARDENER_CLEARING = 2.75f; //Should be at least GameConstants.BULLET_TREE_RADIUS
    static int MAX_LUMBERJACK_PATIENCE = 100;

    static Direction[] dirList = new Direction[6];
    static Direction goingDir;
    static int openDirFromList;
    static int roundNum;
    static int numberOfRoundsAlive;
    static Boolean goRight = true;//True means go right. False means go left.
    static int patienceLeft = MAX_PATIENCE;
    static Boolean startedPlanting = false;


    static TreeInfo[] senseNearbyTrees;
    static boolean hasHome;

    static Random rand = new Random();
    //0:Tree
    //1:Scout
    //2:Lumberjack
    //3:Soldier
    static int[] build = { 2, 2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0};


    //Mass Lumberjack flooding method constants
    public static float MIN_RADIUS_FROM_GARDENERS =
            2 * GameConstants.BULLET_TREE_RADIUS + RobotType.GARDENER.bodyRadius + RobotType.LUMBERJACK.bodyRadius + 2f;

    public static float MIN_RADIUS_FROM_LUMBERJACKS =
            2 * RobotType.LUMBERJACK.bodyRadius + GameConstants.LUMBERJACK_STRIKE_RADIUS + 0.01f;



    public static void run(RobotController rc) throws GameActionException {
        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;
        initDirList();
        numberOfRoundsAlive = 0;
        openDirFromList = rc.getID() % 6;
        roundNum = rc.getRoundNum();
        rand = new Random(rc.getID());
        myRand = new Random(rc.getID());
        goingDir = randomDir();
        // Here, we've separated the controls into a different method for each RobotType.
        // You can add the missing ones or rewrite this into your own control structure.
        switch (rc.getType()) {
            case ARCHON:
                BotArchon.loop();
                break;
            case GARDENER:
                BotGardener.loop();
                break;
            case SOLDIER:
                BotSoldier.loop();
                break;
            case LUMBERJACK:
                BotLumberJack.loop();
                break;
            case SCOUT:
                BotScout.loop();
                break;
            case TANK:
                BotTank.loop();
        }
    }

    public static void shakeNeighbors() throws GameActionException {
        senseNearbyTrees = rc.senseNearbyTrees();
        for (TreeInfo t : senseNearbyTrees) {
            tryToShake(t);
        }
    }

    public static void tryToShake(TreeInfo t) throws GameActionException {
        MapLocation tree = t.getLocation();
        if (rc.canShake(tree)) {
            rc.shake(tree);
        }
    }


    public static void wander() throws GameActionException {
        try {//TODO. make it better
            if (!rc.hasMoved()) {
                while (Clock.getBytecodesLeft() > 100) {
                    int leftOrRight = rand.nextBoolean() ? -1 : 1;
                    for (int i = 0; i < 72; i++) {
                        Direction offset = new Direction(goingDir.radians + (float) (leftOrRight * 2 * Math.PI * ((float) i) / 72));
                        if (rc.canMove(offset) && !rc.hasMoved()) {
                            rc.move(offset);
                            goingDir = offset;
                            return;
                        }
                    }
                    goingDir = randomDirection();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static int encodeBroadcastLoc(MapLocation location) {
        //debug_println("ENCODED: " + location.toString());
        return ((int) (location.x*10 + .5)) * 100000 + (int) (location.y *10 + .5);//Plus .5 to each so they round tenths either up or down
    }

    public static MapLocation decodeBroadcastLoc(int input) {
        if (input == 0) {
            return null;
        }
        return new MapLocation((int) input / 1000000, (input % 100000)/10);
    }


    public static Direction randomDirection() {
        return (new Direction(myRand.nextFloat() * 2 * (float) Math.PI));
    }

    public static Direction randomDir() {
        return dirList[rand.nextInt(6)];
    }

    public static void initDirList() {
        for (int i = 0; i < 6; i++) {
            float radians = (float) (-Math.PI + 2 * Math.PI * ((float) i) / 6);
            dirList[i] = new Direction(radians);
        }
    }

    public static void tryToWater() throws GameActionException {
        if (rc.canWater()) {
            TreeInfo[] nearbyTrees = rc.senseNearbyTrees();
            for (int i = 0; i < nearbyTrees.length; i++)
                if (nearbyTrees[i].getHealth() < GameConstants.BULLET_TREE_MAX_HEALTH - GameConstants.WATER_HEALTH_REGEN_RATE) {
                    if (nearbyTrees[i].getTeam() == rc.getTeam() && rc.canWater(nearbyTrees[i].getID())) {
                        rc.water(nearbyTrees[i].getID());
                        break;
                    }
                }
        }
    }

    public static int tryToBuild(RobotType t) throws GameActionException {
        return tryToBuild(t, t.bulletCost);
    }

    public static int tryToBuild(RobotType type, int moneyNeeded) throws GameActionException {
        //try to build gardeners
        //can you build a gardener?
        if (rc.getTeamBullets() > moneyNeeded) {//have enough bullets. assuming we haven't built already.
            for (int i = 0; i < 6; i++) {
                if (rc.canBuildRobot(type, dirList[i])) {
                    rc.buildRobot(type, dirList[i]);
                    return 1;
                }
            }
        }
        return 0;
    }

    /*
    Same as tryToBuild, but tests more directions for building
    */
    public static int fineDeltaTryToBuild(RobotType type, int moneyNeeded) throws GameActionException {
        //try to build gardeners
        //can you build a gardener?
        if (rc.getTeamBullets() > moneyNeeded) {//have enough bullets. assuming we haven't built already.
            for (int i = 0; i < 72; i++) {
                Direction d = new Direction((float) (Math.PI * i / 72));
                if (rc.canBuildRobot(type, d)) {
                    rc.buildRobot(type, d);
                    return 1;
                }
            }
        }
        return 0;
    }

    public static Boolean tryToPlant() throws GameActionException {
        //try to build gardeners
        //can you build a gardener?
        //System.out.println("PLANTING");
        if (rc.getTeamBullets() > GameConstants.BULLET_TREE_COST) {//have enough bullets. assuming we haven't built already.
            for (int i = 0; i < 6; i++) {
                if (i != openDirFromList && rc.canPlantTree(dirList[i])) {
                    rc.plantTree(dirList[i]);
                    startedPlanting = true;
                    //If lumberjacks were called to clear a tree in the way. Let them know it was cleared.
                    MapLocation plantedTree = rc.getLocation().add(dirList[i], MIN_GARDENER_CLEARING);
                    MapLocation lumberjackNeededLocation = decodeBroadcastLoc(rc.readBroadcast(NEED_LUMBERJACK_FOR_CLEARING));
                    if (lumberjackNeededLocation != null && lumberjackNeededLocation.distanceTo(plantedTree) <= MIN_GARDENER_CLEARING) {
                        rc.broadcast(NEED_LUMBERJACK_FOR_CLEARING, 0);
                    }
                    return true;
                } else if (treeInWay(rc.senseNearbyTrees(), rc.getLocation().add(dirList[i], MIN_GARDENER_CLEARING))) {
                    rc.broadcast(NEED_LUMBERJACK_FOR_CLEARING, encodeBroadcastLoc(rc.getLocation().add(dirList[i], MIN_GARDENER_CLEARING)));
                } else {
                    //TURN BACK ON//rc.setIndicatorDot(rc.getLocation().add(dirList[i]) , 255,0, 0   );
                }
            }
        }
        //System.out.println("RET False");
        return false;
    }

    public static Boolean treeInWay(TreeInfo[] trees, MapLocation location) throws GameActionException{
        for (TreeInfo t : trees) {
            if (t.getTeam() != rc.getTeam() && t.getLocation().distanceTo(location) < MIN_GARDENER_CLEARING) {
                debug_println("REQUEST CLEARING");
                rc.setIndicatorDot(location, 0, 255, 0);
                rc.broadcast(NEED_LUMBERJACK_FOR_CLEARING_TREE_ID, t.getID());
                return true;
            }
        }
        return false;
    }

    static boolean willCollideWithMe(BulletInfo bullet) {
        MapLocation myLocation = rc.getLocation();

        // Get relevant bullet information
        Direction propagationDirection = bullet.dir;
        MapLocation bulletLocation = bullet.location;

        // Calculate bullet relations to this robot
        Direction directionToRobot = bulletLocation.directionTo(myLocation);
        float distToRobot = bulletLocation.distanceTo(myLocation);
        float theta = propagationDirection.radiansBetween(directionToRobot);

        // If theta > 90 degrees, then the bullet is traveling away from us and we can break early
        if (Math.abs(theta) > Math.PI / 2) {
            return false;
        }

        // distToRobot is our hypotenuse, theta is our angle, and we want to know this length of the opposite leg.
        // This is the distance of a line that goes from myLocation and intersects perpendicularly with propagationDirection.
        // This corresponds to the smallest radius circle centered at our location that would intersect with the
        // line that is the path of the bullet.
        float perpendicularDist = (float) Math.abs(distToRobot * Math.sin(theta)); // soh cah toa :)

        return (perpendicularDist <= rc.getType().bodyRadius);
    }

    /**
     * Attempts to move in a given direction, while avoiding small obstacles directly in the path.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir) throws GameActionException {
        return tryMove(dir, 20, 3);
    }


    /**
     * Attempts to move in a given direction, while avoiding small obstacles direction in the path.
     *
     * @param dir           The intended direction of movement
     * @param degreeOffset  Spacing between checked directions (degrees)
     * @param checksPerSide Number of extra directions checked on each side, if intended direction was unavailable
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir, float degreeOffset, int checksPerSide) throws GameActionException {
        if (!hasHome) {
            // First, try intended direction
            if (!rc.hasMoved() && rc.canMove(dir)) {
                rc.move(dir);
                return true;
            }

            // Now try a bunch of similar angles
            //boolean moved = rc.hasMoved();
            int currentCheck = 1;

            while (currentCheck <= checksPerSide) {
                // Try the offset of the left side
                if (!rc.hasMoved() && rc.canMove(dir.rotateLeftDegrees(degreeOffset * currentCheck))) {
                    rc.move(dir.rotateLeftDegrees(degreeOffset * currentCheck));
                    return true;
                }
                // Try the offset on the right side
                if (!rc.hasMoved() && rc.canMove(dir.rotateRightDegrees(degreeOffset * currentCheck))) {
                    rc.move(dir.rotateRightDegrees(degreeOffset * currentCheck));
                    return true;
                }
                // No move performed, try slightly further
                currentCheck++;
            }

            // A move never happened, so return false.

        }
        return false;
    }

    static boolean trySidestep(BulletInfo bullet) throws GameActionException {

        Direction towards = bullet.getDir();
        MapLocation leftGoal = rc.getLocation().add(towards.rotateLeftDegrees(90), rc.getType().bodyRadius);
        MapLocation rightGoal = rc.getLocation().add(towards.rotateRightDegrees(90), rc.getType().bodyRadius);

        return (tryMove(towards.rotateRightDegrees(90)) || tryMove(towards.rotateLeftDegrees(90)));
    }


    public static void dodge() throws GameActionException {
        BulletInfo[] bullets = rc.senseNearbyBullets();
        for (BulletInfo bi : bullets) {
            if (willCollideWithMe(bi)) {
                trySidestep(bi);
            }
        }

    }

    /*
        Navigates to location loc.
     */
    //TODO: Have it also only move if there isn't a bullet. Or maybe minimize number of bullets it has to take.
    public static void navigateTo(MapLocation loc) throws GameActionException {
        goingDir = rc.getLocation().directionTo(loc);
        if (!rc.hasMoved()) {
            while (Clock.getBytecodesLeft() > 100) {
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

    public static void rally() throws GameActionException {
        MapLocation rallyPoint = decodeBroadcastLoc(rc.readBroadcast(RALLY_LOCATION_CHANNEL));
        if (rallyPoint != null) {
            debug_println("RALLY AT: " + rallyPoint.toString());
            navigateTo(rallyPoint);
        } else {
            //System.out.println("RALLY NULL");
        }
    }

    public static float getVictoryPointCost() {
        return (float) (7.5 + (rc.getRoundNum()) * 12.5 / 3000);
    }

    public static void victoryPointsEndgameCheck() throws GameActionException {
        //If we have 10000 bullets, end the game.
        if (rc.getTeamBullets() >= 1000 * getVictoryPointCost() || (rc.getRoundLimit() - rc.getRoundNum() <= 1)) {
            rc.donate(rc.getTeamBullets());
        }
    }


    public static void stillLookingForPlanting() throws GameActionException {// PROBLEM: LATENCE BETWEEN TURNS. TODO
        int input = rc.readBroadcast(GARDENER_LOOKING_FOR_PLANTING + rc.getRoundNum() % 3);
        rc.broadcast(GARDENER_LOOKING_FOR_PLANTING + rc.getRoundNum() % 3, input + 1);
        //System.out.println("NUMBER OF GARDNERS LOOKING: " + (input+1));
    }

    /**
     * Find closest direction to the ideal direction which the player can move.
     *
     * @param ideal The ideal direction to move.
     * @return closest movable direction. If can't move in any direction based on a check with deltas of pi/36, then returns null.
     * @throws GameActionException
     */
    public static Direction closestMovableDirection(Direction ideal) {
        if (!rc.hasMoved()) {
            //Only turn one way first
            for (int i = 0; i <= 36; i++) {
                Direction offset = new Direction(ideal.radians + (float) (Math.PI * ((float) i) / 36));
                if (rc.canMove(offset)) {
                    return offset;
                }
            }
            //If you can't turn that way, try the other way
            for (int i = 0; i <= 36; i++) {
                Direction offset = new Direction(ideal.radians + (float) (-Math.PI * ((float) i) / 36));
                if (rc.canMove(offset)) {
                    return offset;
                }
            }
        }
        return null;
    }

    /**
     * Attempts to repel and move away from the otherRobot.
     *
     * @param awayFromLocation The MapLocation to move away from.
     * @return new location of robot. Null if didn't move.
     * @throws GameActionException
     */
    public static MapLocation repel(MapLocation awayFromLocation) throws GameActionException {
        Direction away = rc.getLocation().directionTo(awayFromLocation).opposite();
        Direction closestToAway = closestMovableDirection(away);
        if (closestToAway != null && !rc.hasMoved()) {
            MapLocation repelledLocation = rc.getLocation().add(closestToAway, rc.getType().strideRadius);
            //debug_println("TRYING TO REPEL: " + otherRobot.toString());
            if (rc.canMove(repelledLocation) && !rc.hasMoved()) {
                rc.move(repelledLocation);
                return repelledLocation;
            }
        }
        //debug_println("COULDN'T REPEL: " + otherRobot.toString());
        return null;
    }

    /*
    For sorting. Will sort robots by distance away from player.
     */
    public static int compareBotsForInitialSorting(RobotInfo a, RobotInfo b){
        MapLocation myLocation = rc.getLocation();
        return (int) (myLocation.distanceTo(a.getLocation()) - myLocation.distanceTo(b.getLocation()));
    }

    /*
    For sorting. Will sort MapLocations by distance away from player.
     */
    public static int compareBotsForInitialSorting(MapLocation a, MapLocation b){
        MapLocation myLocation = rc.getLocation();
        return (int) (myLocation.distanceTo(a) - myLocation.distanceTo(b));
    }

    public static void debug_println(String out) {
        System.out.println(out);
    }

    public static void debug_print(String out) {
        System.out.print(out);
    }
}