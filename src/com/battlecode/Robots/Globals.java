package com.battlecode.Robots;

import battlecode.common.*;

public class Globals {
    public static RobotController rc;
    public static MapLocation here;
    public static Team friendly;
    public static Team enemy;
    public static Direction homeDir;
    public static MapLocation centerOfAllInitialArchons;
    public static MapLocation centerOfFriendlyInitialArchons;
    public static MapLocation centerOfEnemyInitialArchons;
    public static MapLocation[] getArchonNumber;

    public static int numberOfInitialArchon;

    public static RobotInfo[] visibleEnemies = null;
    public static RobotInfo[] visibleFriendlies = null;
    public static RobotInfo[] visibleBullets = null;
    public static RobotInfo[] visibleOnTargetBullets = null;

    public static void init(RobotController theRC) {
        rc = theRC;
        friendly = rc.getTeam();
        enemy = friendly.opponent();

        numberOfInitialArchon = getInitialArchonLocations(friendly).length;

        centerOfFriendlyInitialArchons = new MapLocation(0,0);
        centerOfEnemyInitialArchons = new MapLocation(0,0);
        centerOfFriendlyInitialArchons = ArchonCenter(1.0 / (double)numberOfInitialArchon, centerOfFriendlyInitialArchons);
        centerOfEnemyInitialArchons = ArchonCenter(1.0 / (double)numberOfInitialArchon, centerOfEnemyInitialArchons);
    }

    public static MapLocation ArchonCenter(double f, MapLocation a) {
        return new MapLocation((int)Math.round(f * a.x), (int)Math.round(f * a.y));
    }
}
