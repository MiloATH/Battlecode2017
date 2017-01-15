package rush;

import battlecode.common.*;


public class BotScout extends RobotPlayer {

    public static Boolean nearbyGardener;
    public static TreeInfo[] previousTrees = {};
    public static int treeCount;

    public static void loop() throws GameActionException {
        treeCount = 0;
        while (true) {
            try {
                victoryPointsEndgameCheck();
                dodge();
                shakeNeighbors();
                //TODO optimise to not have to use rc.hasMoved()
                //Attack other team's gardeners only
                RobotInfo[] bots = rc.senseNearbyRobots();
                nearbyGardener = false;
                for (RobotInfo b : bots) {
                    if (b.getTeam() != rc.getTeam()) {
                        if (b.getType() == RobotType.ARCHON) {//TODO: only broadcast once
                            rc.broadcast(ENEMY_SEEN_CHANNEL, encodeBroadcastLoc(b.getLocation()));
                        }
                        else if (b.getType() == RobotType.GARDENER) {
                            nearbyGardener = true;
                            Direction opponent = rc.getLocation().directionTo(b.getLocation());
                            //TODO: Don't run over your own bullets
                            float distance = (float) (b.getLocation().distanceTo(rc.getLocation()) - 0.25);
                            distance = distance < 0 ? 0 : distance;
                            if (distance < 2.5 && rc.canFireSingleShot()) {
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
                        else if (b.getType() == RobotType.SOLDIER) {
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
                            if (distance < 3 && !rc.hasMoved() && !rc.hasAttacked() && rc.canMove(opponent.opposite(), distance)) {
                                System.out.println("MOVING " + (distance) + " in direction " + opponent.opposite().toString());
                                rc.move(opponent.opposite(), distance);
                            }
                        }
                    }
                }

                //Didn't move
                if (!rc.hasMoved()) {
                    MapLocation loc = decodeBroadcastLoc(rc.readBroadcast(ENEMY_GARDENER_SEEN_CHANNEL));
                    MapLocation me = rc.getLocation();
                    //If within sight (real sight is 10) of location, there are no nearby enemy gardeners and hasn't attacked
                    if (loc != null && loc.x - me.x < 7 && loc.y - me.y < 7 && !nearbyGardener && !rc.hasAttacked()) {//Enemy was probably destroyed or escaped
                        rc.broadcast(ENEMY_GARDENER_SEEN_CHANNEL, 0);
                    }
                    if (loc != null && rc.canMove(loc)) {//Go to seen gardener
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
                Clock.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static void scoutWander() throws GameActionException {

        //TODO: early game check location of opponent archons. get location of opponent archons with symmetry of our archons
        try {
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
        //System.out.println("DENSITY OF TREES: " + treeDensity);
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
}