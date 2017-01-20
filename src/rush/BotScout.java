package rush;

import battlecode.common.*;

import java.awt.*;
import java.util.Arrays;

public class BotScout extends RobotPlayer {

    public static Boolean nearbyGardener;
    public static int treeCount;
    public static Boolean stayInPlace = false;
    public static float MAX_SCOUT_SHOOTING_DISTANCE = 2f;
    public static float MAX_SCOUT_TREE_HIDE_FROM_ENEMY = 0;//2.5f;
    public static TreeInfo nextTree = null;
    public static TreeInfo previousTree = null;
    public static MapLocation topLeft;
    public static Boolean scoutMap =false;

    public static void loop() throws GameActionException {
        treeCount = 0;
        while (true) {
            try {
                victoryPointsEndgameCheck();
                //int numberOfScoutsMade = rc.readBroadcast(SCOUTS_CHANNEL);
                /*if(numberOfScoutsMade<=5 || scoutMap){
                    scoutMap = true;
                    findMapSize();
                }*/
                //dodge();//Now dodges after trying to hide in a tree
                shakeNeighbors();
                //TODO optimise to not have to use rc.hasMoved()
                //Attack other team's gardeners only
                if (!rc.isLocationOccupiedByTree(rc.getLocation())) {
                    stayInPlace = false;
                }
                RobotInfo[] bots = rc.senseNearbyRobots();
                nearbyGardener = false;
                Arrays.sort(bots, (a, b) -> compareBotsForInitialSorting(a,b));
                for(RobotInfo b:bots){
                    debug_println("Distance from scout: " + rc.getLocation().distanceTo(b.getLocation()));
                }
                if (bots.length == 0) {
                    previousTree = nextTree;
                    nextTree = null;
                    stayInPlace = false;
                }
                for (RobotInfo b : bots) {
                    if (b.getTeam() != rc.getTeam()) {
                        debug_println("ENEMY SEEN");
                        if (b.getType() == RobotType.ARCHON) {//TODO: only broadcast once
                            rc.broadcast(ENEMY_SEEN_CHANNEL, encodeBroadcastLoc(b.getLocation()));
                        } else {//if (b.getType() == RobotType.GARDENER) {
                            debug_println("ENEMY IS NOT AN ARCHON SEEN");
                            if(b.getType()==RobotType.GARDENER) {
                                nearbyGardener = true;
                            }
                            Direction opponent = rc.getLocation().directionTo(b.getLocation());
                            float distance = (float) (b.getLocation().distanceTo(rc.getLocation()));
                            distance = distance < 0 ? 0 : distance;
                            if (distance < MAX_SCOUT_SHOOTING_DISTANCE && rc.canFireSingleShot()) {
                                //System.out.println("FIRING");
                                rc.fireSingleShot(opponent);
                                if (rc.readBroadcast(ENEMY_GARDENER_SEEN_CHANNEL) == 0) {
                                    rc.broadcast(ENEMY_GARDENER_SEEN_CHANNEL, encodeBroadcastLoc(b.getLocation()));
                                }
                            }
                            debug_println("Stay in place???: " + stayInPlace);
                            if (!stayInPlace && !rc.hasMoved()) {
                                //TODO: Don't run over your own bullets.
                                nextTree = treeHideNavigateTo(b.getLocation(), senseNearbyTrees, bots);
                                if (nextTree == null) {//Find tree
                                    debug_println("NEXT TREE IS NULL");
                                    nextTree = treeHideNavigateTo(b.getLocation(), senseNearbyTrees, bots);
                                }
                                if(previousTree==null && nextTree!=null){//COULD BE A PROBLEM
                                    previousTree=nextTree;
                                }
                                debug_println("NEXT TREE IS " + (nextTree==null ? "null": nextTree.toString()));
                                debug_println("CAN MOVE TOWARDS OPPONENT: " + rc.canMove(opponent, distance));
                                if (nextTree != null) {//If still null then there aren't any trees found by treeHideNavigateTo()
                                    /*
                                    TODO: configure nextTree so it goes to the tree
                                     */
                                    //Check if already at best location
                                    MapLocation me = rc.getLocation();
                                    if (nextTree.getLocation().distanceTo(me)<Math.max(0,nextTree.getRadius()-rc.getType().bodyRadius)+0.001){//nextTree.getLocation().x - me.x < 0.001 && nextTree.getLocation().y - me.y < 0.001) {
                                        debug_println("NEAR nextTree");
                                        if (!rc.hasMoved()) {
                                            //If large neutral tree then move to edge of tree towards enemy
                                            if(nextTree.getTeam()==Team.NEUTRAL && nextTree.getRadius()!=GameConstants.BULLET_TREE_RADIUS){
                                                goToEdgeOfTree(nextTree,b);
                                            }
                                            else if(rc.canMove(nextTree.getLocation())) {
                                                rc.move(nextTree.getLocation());
                                            }
                                        }
                                        previousTree = nextTree;
                                        nextTree = null;
                                        if (distance <= MAX_SCOUT_TREE_HIDE_FROM_ENEMY) {
                                            debug_println("Distance from enemy: " + distance);
                                            stayInPlace = true;
                                            debug_println("Check before moving: STAY IN PLACE");
                                        }
                                    } else {
                                        //rc.setIndicatorDot(nextTree.getLocation(), 255, 0, 0);
                                        MapLocation bestTreeLoc = nextTree.getLocation();
                                        //rc.setIndicatorLine(rc.getLocation(), bestTreeLoc, 255, 0, 0);
                                        MapLocation meNow = rc.getLocation();
                                        if (stepOnToLocation(bestTreeLoc)) {
                                            debug_println("MOVING TO Best Tree: " + bestTreeLoc.toString());
                                            meNow = bestTreeLoc;
                                        } else {
                                            debug_println("NAVING TO " + bestTreeLoc.toString());
                                            navigateTo(bestTreeLoc);
                                            meNow = rc.getLocation().add(goingDir, rc.getType().strideRadius);
                                        }
                                        distance = meNow.distanceTo(b.getLocation());
                                        ////System.out.println("My Location: " + meNow.toString());
                                        ////System.out.println("x: " + (bestTreeLoc.x - meNow.x) + " y: " + (bestTreeLoc.y - meNow.y) + " d: " + distance);
                                        if (nextTree.getLocation().distanceTo(meNow)<Math.max(0,nextTree.getRadius()-rc.getType().bodyRadius)+0.001){//(bestTreeLoc.x - meNow.x < 0.001 && bestTreeLoc.y - meNow.y < 0.001) {
                                            previousTree = nextTree;
                                            nextTree = null;
                                            //System.out.println("x: " + (bestTreeLoc.x - meNow.x) + " y: " + (bestTreeLoc.y - meNow.y) + " d: " + distance + " FIND NEXT TREE");
                                            //System.out.println("Distance: " + distance + " d less than max?: " + (distance < MAX_SCOUT_TREE_HIDE_FROM_ENEMY));
                                            if (distance <= MAX_SCOUT_TREE_HIDE_FROM_ENEMY) {
                                                //System.out.println("Distance from enemy: " + distance);
                                                stayInPlace = true;
                                                ////System.out.println("x: " + (bestTreeLoc.x - meNow.x) + " y: " + (bestTreeLoc.y - meNow.y) + " d: " + distance + " STAY IN PLACE");
                                            }
                                        }
                                    }
                                } else if ( b.getType()==RobotType.GARDENER && rc.canMove(opponent, distance)) {
                                    debug_println("Moving towards opponent");
                                    //rc.setIndicatorLine(rc.getLocation(),rc.getLocation().add(opponent,distance), 255, 255,255);
                                    //goToExactLocation(b.getLocation());
                                    //stepOnToLocation(b.getLocation());
                                    rc.move(opponent, distance);
                                }
                            }
                        }
                    }
                }

                //ATTACK OTHER UNITS EXCEPT ARCHONS
                if (!rc.hasAttacked() && rc.canFireSingleShot() && bots.length > 0) {
                    for (RobotInfo bot : bots) {
                        if (bot.getTeam() != rc.getTeam() && bot.getType() != RobotType.ARCHON) {
                            rc.fireSingleShot(rc.getLocation().directionTo(bot.getLocation()));
                            break;
                        }
                    }
                }

                if (!nearbyAnythingButArchon(bots)) {
                    stayInPlace = false;
                }
                //Try to dodge
                if (!stayInPlace && nextTree == null && !rc.hasMoved()) {
                    //System.out.println("DODGING");
                    dodge();
                }
                //Didn't move
                if (!stayInPlace && nextTree == null && !rc.hasMoved()) {
                    MapLocation loc = decodeBroadcastLoc(rc.readBroadcast(ENEMY_GARDENER_SEEN_CHANNEL));
                    MapLocation me = rc.getLocation();
                    //If within sight (real sight is 10) of location, there are no nearby enemy gardeners and hasn't attacked
                    if (loc != null && loc.x - me.x < 7 && loc.y - me.y < 7 && !nearbyGardener && !rc.hasAttacked()) {//Enemy was probably destroyed or escaped
                        rc.broadcast(ENEMY_GARDENER_SEEN_CHANNEL, 0);
                    }
                    if (loc != null && rc.canMove(loc)) {//Go to seen gardener
                        //System.out.println("GOTO SEEN GARDENER");
                        rc.move(loc);
                    } else if(!nearbyGardener){//wander
                        scoutWander();
                    }
                }
                roundNum = rc.getRoundNum();
                if (roundNum < ROUND_TO_BROADCAST_TREE_DENSITY) {
                    updateTreeCount();
                } else if (roundNum == ROUND_TO_BROADCAST_TREE_DENSITY) {
                    broadcastTreeDensity();
                }
                debug_println("\n");
                Clock.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static void scoutWander() throws GameActionException {

        //TODO: early game check location of opponent archons. get location of opponent archons with symmetry of our archons
        try {
            //Check broadcast for gardeners
            //MapLocation[] broadcastedLocs = rc.senseBroadcastingRobotLocations();


            debug_println("WANDER");
            if (!rc.hasMoved()) {
                while (Clock.getBytecodesLeft() > 100) {
                    if (rc.canMove(goingDir) && !rc.hasMoved()) {
                        rc.move(goingDir);
                        return;
                    }
                    goingDir = randomDirection();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateTreeCount() {
        numberOfRoundsAlive++;
        TreeInfo[] trees = rc.senseNearbyTrees();
        //TODO: Below can be optimized with sort or hash
        treeCount += trees.length;
        /*for (TreeInfo t : trees) {
            for (TreeInfo old : previousTrees) {
                if (t.getID() == old.getID()) {
                    treeCount--;
                }
            }
        }
        previousTrees = trees;*/
    }

    public static void broadcastTreeDensity() throws GameActionException {
        double treeDensity = treeCount / numberOfRoundsAlive;
        ////System.out.println("DENSITY OF TREES: " + treeDensity);
        rc.broadcast(TREE_DENSITY_CHANNEL, (int) treeDensity);
        /*
                NOTE: Density is based on life of scout, NOT ACCOUNTED FOR SIZE OF MAP. NOT REALLY DENSITY!
                //TODO: divide by actual map size or distance covered, not round number.
                MAP'S typical density at round 125
                MAP         |   Density
                --------------------
                Barrier     |   ~4 , <=5
                --------------------
                DenseForest |   ~6, <=11
                --------------------
                Enclosure   |   ~1  , <=3
                --------------------
                Hurdle      |   ~4, <=5
                --------------------
                LineOfFire  |   ~135, <145
                --------------------
                MagicWood   |   ~36, <41
                --------------------
                SparseForest|   ~2, <5
                --------------------
                Shrine      |   ~1, <2
                 */
    }

    /*
        Navigate towards loc by hiding in trees.
        NOTE: shakeNeighbors() has to have been called.
     */
    public static TreeInfo treeHideNavigateTo(MapLocation loc, TreeInfo[] trees, RobotInfo[] bots) throws GameActionException {
        //Check if touching a tree
        MapLocation me = rc.getLocation();
        TreeInfo bestHidingTree = null;
        float bestHidingTreeDistanceToMe = -1f;
        float distanceToLoc = me.distanceTo(loc);
        //Since there is a problem with sensing something you are on, check the previous tree if it was really the optimal tree.
        /*if(previousTree!=null){
            bestHidingTree = previousTree;
            bestHidingTreeDistanceToMe = me.distanceTo(previousTree.getLocation());
        }*/

        //Find trees that decreases the distance
        for (TreeInfo t : trees) {
            MapLocation treeLoc = t.getLocation();
            if (treeLoc.distanceTo(loc) < distanceToLoc) {
                //Find the one that is minimum distance away from me.
                float distanceToMe = treeLoc.distanceTo(me);
                if ((bestHidingTreeDistanceToMe == -1f || distanceToMe < bestHidingTreeDistanceToMe) && !treeTakenAlready(t, bots)) {
                    bestHidingTreeDistanceToMe = distanceToMe;
                    bestHidingTree = t;
                }
            }
        }
        debug_println("##Can sense previous tree?? "+((previousTree!=null)?rc.canSenseTree(previousTree.getID()):"UNKNOWN/NUll"));
        debug_println("###Distance to Location: " + distanceToLoc + " Is circle Occ??? " + rc.isCircleOccupiedExceptByThisRobot(rc.getLocation(),rc.getType().bodyRadius+0.1f));
        if(bestHidingTree==null && rc.isCircleOccupiedExceptByThisRobot(rc.getLocation(),rc.getType().bodyRadius+0.1f)){
            debug_println("BEST TREE IS Previous Tree");
            return previousTree;
        }
        debug_println("BEST HIDING TREE: " + (bestHidingTree!=null ? bestHidingTree.toString(): "NULL"));
        debug_println("Previous Tree is: " + (previousTree!=null ? previousTree.toString():"NULL"));
        return bestHidingTree;
    }

    /*
    returns true if tree is already occupied by robot
     */
    public static Boolean treeTakenAlready(TreeInfo tree, RobotInfo[] bots) {
        for (RobotInfo bot : bots) {
            if (bot.getLocation().x - tree.getLocation().x < tree.radius && bot.getLocation().y - tree.getLocation().y < tree.radius) {
                return true;
            }
        }
        return false;
    }

    public static Boolean stepOnToLocation(MapLocation loc) throws GameActionException {//TODO: CHECK THIS ACTUALLY WORKS. Actually lands on loc?
        if (rc.canMove(loc) && rc.getLocation().distanceTo(loc) <= rc.getType().strideRadius) {
            //System.out.println("STEP ON LOCATION");
            rc.move(loc);
            return true;
        }
        return false;
    }

    public static void goToExactLocation(MapLocation loc) throws GameActionException{
        if(!rc.hasMoved() && rc.canMove(loc) && rc.getLocation().distanceTo(loc) <= rc.getType().strideRadius){
            rc.move(rc.getLocation().directionTo(loc),rc.getLocation().distanceTo(loc));
        }
    }

    //For large trees scouts need to go to edge to fire
    public static void goToEdgeOfTree(TreeInfo t, RobotInfo towardsEnemy) throws GameActionException{
        MapLocation treeLoc = t.getLocation();
        MapLocation myLoc = rc.getLocation();
        Direction goInDirection = myLoc.directionTo(towardsEnemy.getLocation());
        float distanceToEdgeBeforeBodyGoesOver = Math.max(t.getRadius()- rc.getType().bodyRadius,0);
        MapLocation idealSpot = treeLoc.add(goInDirection,distanceToEdgeBeforeBodyGoesOver);
        //rc.setIndicatorDot(idealSpot,0,255,0);
        debug_println("Ideal location: " + idealSpot.toString());
        if(!stepOnToLocation(idealSpot)) {
            if (rc.canMove(goInDirection, distanceToEdgeBeforeBodyGoesOver) && !rc.hasMoved()) {
                rc.move(goInDirection, distanceToEdgeBeforeBodyGoesOver);
            }
        }
    }

    /*
    Also broadcast size

    public static void findMapSize() throws GameActionException{
        if(topLeft==null) {
            navigateTo(new MapLocation(0, 0));
        }
        else {
            navigateTo(new MapLocation(99999, 99999));
        }
        if(!rc.hasMoved())
    }*/
    public static boolean nearbyAnythingButArchon(RobotInfo[] bots){
        for(RobotInfo b:bots){
            if(b.getTeam()!=rc.getTeam() && b.getType()!=RobotType.ARCHON){
                return true;
            }
        }
        return false;
    }

    public static int compareBotsForInitialSorting(RobotInfo a, RobotInfo b){
        MapLocation myLocation = rc.getLocation();
        return (int) (myLocation.distanceTo(a.getLocation()) - myLocation.distanceTo(b.getLocation()));
    }

}