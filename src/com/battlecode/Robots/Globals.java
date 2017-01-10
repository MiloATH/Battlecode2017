package com.battlecode.Robots;

import battlecode.common.*;

public class Globals {
    public static RobotController rc;
    public static MapLocation here;
    public static Team friendly;
    public static Team enemy;

    public static RobotInfo[] visibleEnemies = null;
    public static RobotInfo[] visibleFriendlies = null;
    public static RobotInfo[] visibleBullets = null;
    public static RobotInfo[] visibleOnTargetBullets = null;

    public static void init(RobotController theRC) {
        rc = theRC;
        friendly = rc.getTeam();
        enemy = friendly.opponent();
    }
}
