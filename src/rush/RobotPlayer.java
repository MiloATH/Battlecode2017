package rush;

import battlecode.common.*;
import java.util.*;

public strictfp class RobotPlayer extends rush.Globals {
    static RobotController rc;
    static Random myRand;
    @SuppressWarnings("unused")
    // Keep broadcast channels
    static int GARDENER_CHANNEL = 50;
    static int LUMBERJACK_CHANNEL = 60;
    static int SCOUTS_CHANNEL = 70;
    static int TREE_CHANNEL = 80;
    static int SOLDIER_CHANNEL = 90;
    static int ENEMY_GARDENER_SEEN_CHANNEL = 100;

    // Keep important numbers here
    static int GARDENER_MAX = 15;
    static int LUMBERJACK_MAX = 30;
    static int SCOUT_MAX = 20;
    static int SURPLUS_BULLETS = 500;

    static Direction[] dirList = new Direction[6];
    static Direction goingDir;
    static int openDirFromList;
    static Random rand;
    static int earlyGame = 300;
    static int roundNum;
    //0:Tree
    //1:Scout
    //2:Lumberjack
    //3:Soldier
    static int[] build = {1, 0, 1, 0, 0, 1, 0, 0, 3, 0, 0, 2, 0, 0, 3, 0, 2, 3, 3};

    static TreeInfo[] senseNearbyTrees;
    static TreeInfo[] senseAllTrees;
    static float acceptableMissingTreeHealth = 35;
    static int treeMovingTo;
    static float distanceToTarget = 100000;
    static float targetCurrentHealth = 1000;
    static Direction directionToTarget;

    public static void run(RobotController rc) throws GameActionException {
        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;
        initDirList();
        Globals.init(rc);
        openDirFromList = rc.getID() % 6;
        roundNum = rc.getRoundNum();
        rand = new Random(rc.getID());
        myRand = new Random(rc.getID());
        goingDir = randomDir();
        // Here, we've separated the controls into a different method for each RobotType.
        // You can add the missing ones or rewrite this into your own control structure.
        switch (rc.getType()) {
            case ARCHON:
                runArchon();
                break;
            case GARDENER:
                runGardener();
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
                runTank();
        }
    }

    private static void runTank() throws GameActionException {//TODO
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
                    wander();
                }
                Clock.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    static void runArchon() throws GameActionException {
        while (true) {
            try {
                victoryPointsEndgameCheck();
                dodge();
                //Build gardener if less than max
                int prevNumGard = rc.readBroadcast(GARDENER_CHANNEL);
                if (prevNumGard < GARDENER_MAX * rc.getRoundNum() / rc.getRoundLimit() + 1 || rc.getTeamBullets() >= SURPLUS_BULLETS) {
                    rc.broadcast(GARDENER_CHANNEL, prevNumGard + tryToBuild(RobotType.GARDENER, RobotType.GARDENER.bulletCost));
                }

                //Then wander
                retreat();
                Clock.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static void runGardener() throws GameActionException {
        while (true) {
            try {
                victoryPointsEndgameCheck();
                dodge();
                int prevLum = rc.readBroadcast(LUMBERJACK_CHANNEL);
                int prevScouts = rc.readBroadcast(SCOUTS_CHANNEL);
                int prevTree = rc.readBroadcast(TREE_CHANNEL);
                int prevSold = rc.readBroadcast(SOLDIER_CHANNEL);
                int buildNum = (prevLum + prevScouts + prevTree + prevSold) % build.length;
                if (buildNum < build.length) {
                    switch (build[buildNum]) {
                        case 0://Tree
                            if (tryToPlant()) {
                                rc.broadcast(TREE_CHANNEL, prevTree + 1);
                            }
                            break;
                        case 1://Scout
                            if (prevScouts <= SCOUT_MAX) {
                                rc.broadcast(SCOUTS_CHANNEL, prevScouts + tryToBuild(RobotType.SCOUT));
                            }
                            break;
                        case 2://Lumberjack
                            if (prevLum <= LUMBERJACK_MAX) {
                                rc.broadcast(LUMBERJACK_CHANNEL, prevLum + tryToBuild(RobotType.LUMBERJACK));
                            }
                            break;
                        case 3://Soldiers
                            rc.broadcast(SOLDIER_CHANNEL, prevSold + tryToBuild(RobotType.SOLDIER));
                            break;
                    }
                }
                //now try to water trees
                tryToWater();
                Clock.yield();



            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static void startForesting() throws GameActionException {

        senseNearbyTrees = rc.senseNearbyTrees(2, friendly);
        senseAllTrees = rc.senseNearbyTrees(6, friendly);
        if(tryToWater() == treeMovingTo) {
            targetCurrentHealth = 1000;
        }
        if(senseNearbyTrees.length == 0 && rc.canPlantTree(towardsEnemy)) {
            rc.plantTree(awayFromEnemy);
        }
        if(senseAllTrees.length != 0 && targetCurrentHealth == 1000) {
            for (int i = 0; i <= senseNearbyTrees.length; i++) {
                float checkedHealth = senseNearbyTrees[i].getHealth();
                if (checkedHealth < acceptableMissingTreeHealth && checkedHealth < targetCurrentHealth) {
                    targetCurrentHealth = senseNearbyTrees[i].getHealth();
                    treeMovingTo = senseNearbyTrees[i].getID();
                }
            }
        }

        if (senseAllTrees != null) {
            directionToTarget = rc.getLocation().directionTo(senseNearbyTrees[treeMovingTo].getLocation());
        }
        if (rc.canMove(directionToTarget)) {
            rc.move(directionToTarget);
        } else if (rc.canMove(directionToTarget.rotateLeftDegrees(45))) {
            rc.move(directionToTarget.rotateLeftDegrees(45));
        } else if (rc.canMove(directionToTarget.rotateRightDegrees(90))) {
            rc.move(directionToTarget.rotateRightDegrees(90));
        }
        Clock.yield();
    }

    static void runSoldier() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
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
        return ((int) location.x) * 100000 + (int) location.y;
    }

    public static MapLocation decodeBroadcastLoc(int input) {
        if (input == 0) {
            return null;
        }
        return new MapLocation((int) input / 100000, input % 100000);
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

    public static int tryToWater() throws GameActionException {
        if (rc.canWater()) {
            TreeInfo[] nearbyTrees = rc.senseNearbyTrees(1);
            for (int i = 0; i < nearbyTrees.length; i++)
                if (nearbyTrees[i].getHealth() < acceptableMissingTreeHealth) {
                    if (rc.canWater(nearbyTrees[i].getID())) {
                        rc.water(nearbyTrees[i].getID());
                        return nearbyTrees[i].getID();
                    }
                }
        }
        return -1;
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

    public static Boolean tryToPlant() throws GameActionException {
        //try to build gardeners
        //can you build a gardener?

        if (rc.getTeamBullets() > GameConstants.BULLET_TREE_COST) {//have enough bullets. assuming we haven't built already.
            for (int i = 0; i < 6; i++) {
                if (i != openDirFromList && rc.canPlantTree(dirList[i])) {
                    rc.plantTree(dirList[i]);
                    return true;
                }
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
        return false;
    }

    static boolean trySidestep(BulletInfo bullet) throws GameActionException {

        Direction towards = bullet.getDir();
        MapLocation leftGoal = rc.getLocation().add(towards.rotateLeftDegrees(90), rc.getType().bodyRadius);
        MapLocation rightGoal = rc.getLocation().add(towards.rotateRightDegrees(90), rc.getType().bodyRadius);

        return (tryMove(towards.rotateRightDegrees(90)) || tryMove(towards.rotateLeftDegrees(90)));
    }

    static void retreat() throws GameActionException {
        if(awayFromEnemy != null && !rc.hasMoved() && rc.canMove(awayFromEnemy)) {
            rc.move(awayFromEnemy);
        }
    }

    public static void dodge() throws GameActionException {
        BulletInfo[] bullets = rc.senseNearbyBullets();
        for (BulletInfo bi : bullets) {
            if (willCollideWithMe(bi)) {
                trySidestep(bi);
            }
        }

    }

    public static void victoryPointsEndgameCheck() throws GameActionException {
        //If we have 10000 bullets, end the game.
        if (rc.getTeamBullets() >= 10000 || (rc.getRoundLimit() - rc.getRoundNum() < 2)) {
            rc.donate(rc.getTeamBullets());
        }
    }
}