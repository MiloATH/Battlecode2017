package com.battlecode.Robots;

import battlecode.common.*;

public class RobotPlayer extends Globals {

    public static void run(RobotController rc) throws GameActionException {

        switch (rc.getType()) {
            case ARCHON:
                BotArchon.loop();
                break;

            case GARDENER:
                BotGardener.loop();
                break;

            case SCOUT:
                BotScout.loop();
                break;

            case SOLDIER:
                BotSoldier.loop();
                break;

            case TANK:
                BotTank.loop();
                break;

            case LUMBERJACK:
                BotLumberJack.loop();
                break;
        }
    }
}