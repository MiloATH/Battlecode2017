package rush;

import battlecode.common.*;

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
                //System.out.println(bots.length + " ROBOTS SEEN");
                if (bots.length == 0) {
                    previousTree = nextTree;
                    nextTree = null;
                    stayInPlace = false;
                }
                for (RobotInfo b : bots) {
                    if (b.getTeam() != rc.getTeam()) {
                        ////System.out.println("ENEMY SEEN");
                        if (b.getType() == RobotType.ARCHON) {//TODO: only broadcast once
                            rc.broadcast(ENEMY_SEEN_CHANNEL, encodeBroadcastLoc(b.getLocation()));
                        } else if (b.getType() == RobotType.GARDENER) {
                            System.out.println("ENEMY GARDENER SEEN");
                            nearbyGardener = true;
                            Direction opponent = rc.getLocation().directionTo(b.getLocation());
                            float distance = (float) (b.getLocation().distanceTo(rc.getLocation()));
                            distance = distance < 0 ? 0 : distance;
                            if (distance < MAX_SCOUT_SHOOTING_DISTANCE && rc.canFireSingleShot()) {
                                System.out.println("FIRING");
                                rc.fireSingleShot(opponent);
                                if (rc.readBroadcast(ENEMY_GARDENER_SEEN_CHANNEL) == 0) {
                                    rc.broadcast(ENEMY_GARDENER_SEEN_CHANNEL, encodeBroadcastLoc(b.getLocation()));
                                }
                            }
                            System.out.println("Stay in place???: " + stayInPlace);
                            if (!stayInPlace && !rc.hasMoved()) {
                                //TODO: Don't run over your own bullets.
                                ////System.out.println("MOVING " + (distance) + " in direction " + opponent.toString());
                                if (nextTree == null) {//Find tree
                                    System.out.println("NEXT TREE IS NULL");
                                    nextTree = treeHideNavigateTo(b.getLocation(), senseNearbyTrees, bots);
                                }
                                System.out.println("NEXT TREE IS " + (nextTree==null ? "null": nextTree.toString()));
                                if (nextTree != null) {//If still null then there aren't any trees found by treeHideNavigateTo()
                                    /*
                                    TODO: configure nextTree so it goes to the tree
                                     */
                                    //Check if already at best location
                                    MapLocation me = rc.getLocation();
                                    if (nextTree.getLocation().x - me.x < 0.001 && nextTree.getLocation().y - me.y < 0.001) {
                                        System.out.println("NEAR nextTree");
                                        if (!rc.hasMoved() && rc.canMove(nextTree.getLocation())) {
                                            rc.move(nextTree.getLocation());
                                        }
                                        previousTree = nextTree;
                                        nextTree = null;
                                        if (distance <= MAX_SCOUT_TREE_HIDE_FROM_ENEMY) {
                                            System.out.println("Distance from enemy: " + distance);
                                            stayInPlace = true;
                                            System.out.println("Check before moving: STAY IN PLACE");
                                        }
                                    } else {
                                        rc.setIndicatorDot(nextTree.getLocation(), 255, 0, 0);
                                        MapLocation bestTreeLoc = nextTree.getLocation();
                                        rc.setIndicatorLine(rc.getLocation(), bestTreeLoc, 255, 0, 0);
                                        MapLocation meNow = rc.getLocation();
                                        if (stepOnToLocation(bestTreeLoc)) {
                                            System.out.println("MOVING TO Best Tree: " + bestTreeLoc.toString());
                                            meNow = bestTreeLoc;
                                        } else {
                                            System.out.println("NAVING TO " + bestTreeLoc.toString());
                                            navigateTo(bestTreeLoc);
                                            meNow = rc.getLocation().add(goingDir, rc.getType().strideRadius);
                                        }
                                        distance = meNow.distanceTo(b.getLocation());
                                        //System.out.println("My Location: " + meNow.toString());
                                        //System.out.println("x: " + (bestTreeLoc.x - meNow.x) + " y: " + (bestTreeLoc.y - meNow.y) + " d: " + distance);
                                        if (bestTreeLoc.x - meNow.x < 0.001 && bestTreeLoc.y - meNow.y < 0.001) {
                                            previousTree = nextTree;
                                            nextTree = null;
                                            //System.out.println("x: " + (bestTreeLoc.x - meNow.x) + " y: " + (bestTreeLoc.y - meNow.y) + " d: " + distance + " FIND NEXT TREE");
                                            //System.out.println("Distance: " + distance + " d less than max?: " + (distance < MAX_SCOUT_TREE_HIDE_FROM_ENEMY));
                                            if (distance <= MAX_SCOUT_TREE_HIDE_FROM_ENEMY) {
                                                System.out.println("Distance from enemy: " + distance);
                                                stayInPlace = true;
                                                //System.out.println("x: " + (bestTreeLoc.x - meNow.x) + " y: " + (bestTreeLoc.y - meNow.y) + " d: " + distance + " STAY IN PLACE");
                                            }
                                        }
                                    }
                                } else if (rc.canMove(opponent, distance)) {
                                    System.out.println("Moving towards opponent");
                                    rc.setIndicatorLine(rc.getLocation(),rc.getLocation().add(opponent,distance), 255, 255,255);
                                    rc.move(opponent, distance);
                                }
                            }
                        }
                        /*else if (b.getType() == RobotType.SOLDIER) {
                            nearbyGardener = true;
                            Direction opponent = rc.getLocation().directionTo(b.getLocation());
                            //TODO: Don't run over your own bullets
                            float distance = (float) (b.getLocation().distanceTo(rc.getLocation()) - 0.25);
                            distance = distance < 0 ? 0 : distance;
                            if (distance < 6 && rc.canFireSingleShot()) {
                                rc.fireSingleShot(opponent);
                                if (rc.readBroadcast(ENEMY_GARDENER_SEEN_CHANNEL) == 0) {
                                    rc.broadcast(ENEMY_GARDENER_SEEN_CHANNEL, encodeBroadcastLoc(b.getLocation()));
                                }
                                break;
                            } else if (!rc.hasMoved() && !rc.hasAttacked() && rc.canMove(opponent, distance)) {
                                System.out.println("MOVING " + (distance) + " in direction " + opponent.toString());
                                rc.move(opponent, distance);
                            }
                        }
                        else if (b.getType() == RobotType.LUMBERJACK) {
                            nearbyGardener = true;
                            Direction opponent = rc.getLocation().directionTo(b.getLocation());
                            //TODO: Don't run over your own bullets
                            float distance = (float) (b.getLocation().distanceTo(rc.getLocation()) - 0.25);
                            distance = distance < 0 ? 0 : distance;
                            if (distance < 7 && rc.canFireSingleShot()) {
                                rc.fireSingleShot(opponent);
                                if (rc.readBroadcast(ENEMY_GARDENER_SEEN_CHANNEL) == 0) {
                                    rc.broadcast(ENEMY_GARDENER_SEEN_CHANNEL, encodeBroadcastLoc(b.getLocation()));
                                }
                                break;
                            } else if (distance > 10 && !rc.hasMoved() && !rc.hasAttacked() && rc.canMove(opponent, distance)) {
                                System.out.println("MOVING " + (distance) + " in direction " + opponent.toString());
                                rc.move(opponent, distance);
                            }
                            if (distance < 4 && !rc.hasMoved() && !rc.hasAttacked() && rc.canMove(opponent.opposite(), distance)) {
                                System.out.println("MOVING " + (distance) + " in direction " + opponent.opposite().toString());
                                rc.move(opponent.opposite(), distance);
                            }
                        }*/
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

                if (!nearbyGardener) {//TODO:Could include all enemy units instead of just gardeners
                    stayInPlace = false;
                }
                //Try to dodge
                if (!stayInPlace && nextTree == null && !rc.hasMoved()) {
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
                    } else {//wander
                        scoutWander();
                    }
                }
                roundNum = rc.getRoundNum();
                if (roundNum < ROUND_TO_BROADCAST_TREE_DENSITY) {
                    updateTreeCount();
                } else if (roundNum == ROUND_TO_BROADCAST_TREE_DENSITY) {
                    broadcastTreeDensity();
                }
                System.out.println("\n");
                Clock.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static void scoutWander() throws GameActionException {

        //TODO: early game check location of opponent archons. get location of opponent archons with symmetry of our archons
        try {
            //System.out.println("WANDER");
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
        if(previousTree!=null){
            bestHidingTree = previousTree;
            bestHidingTreeDistanceToMe = me.distanceTo(previousTree.getLocation());
        }
        //Find trees that decreases the distance
        for (TreeInfo t : trees) {
            MapLocation treeLoc = t.getLocation();
            if (treeLoc.distanceTo(loc) < distanceToLoc - 2.5) {
                //Find the one that is minimum distance away from me.
                float distanceToMe = treeLoc.distanceTo(me);
                if ((bestHidingTreeDistanceToMe == -1 || distanceToMe < bestHidingTreeDistanceToMe) && !treeTakenAlready(t, bots)) {
                    bestHidingTreeDistanceToMe = distanceToMe;
                    bestHidingTree = t;
                }
            }
        }
        System.out.println("BEST HIDING TREE: " + (bestHidingTree!=null ? bestHidingTree.toString(): "NULL"));
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

    public static Boolean stepOnToLocation(MapLocation loc) throws GameActionException {
        if (rc.canMove(loc) && rc.getLocation().distanceTo(loc) <= rc.getType().strideRadius) {
            //System.out.println("STEP ON LOCATION");
            rc.move(loc);
            return true;
        }
        return false;
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


}