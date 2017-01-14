package rush;

import battlecode.common.*;

import java.util.HashMap;

public class Globals{
    public static RobotController rc;
    public static MapLocation here;
    public static Team friendly;
    public static Team enemy;

    public static Direction homeDir;
    public static MapLocation centerOfAllInitialArchons;
    public static MapLocation centerOfFriendlyInitialArchons;
    public static MapLocation centerOfEnemyInitialArchons;
    public static MapLocation[] FriendlyInitialArchonLocations;
    public static MapLocation[] EnemyInitialArchonLocations;

    public static HashMap< MapLocation , Boolean > InitialEnemyArchonLocationStatfriendly;
    public static boolean initialEnemyArchonLocatonsChecked;

    public static MapLocation[] initialFriendlyArchonLocations;

    public static MapLocation[] getArchonNumber;

    public static boolean setUpInitialGlobalInformation = false;
    public static boolean scoutedFirstEnemyArchonLocation = false;
    public static boolean scoutedSecondEnemyArchonLocation = false;
    public static boolean scoutedThirdEnemyArchonLocation = false;
    public static Direction awayFromEnemy;
    public static Direction towardsEnemy;


	public static int numberOfInitialArchon;

    public static RobotInfo[] visibleEnemies = null;
    public static RobotInfo[] visibleFriendlies = null;
    public static RobotInfo[] visibleBullets = null;
    public static RobotInfo[] visibleOnTargetBullets = null;


    public static int minSquadSize = 5;

    public static void init(RobotController theRC) {
        rc = theRC;
        friendly = rc.getTeam();
        enemy = friendly.opponent();

        System.out.println(numberOfInitialArchon + " Initial archons");
        numberOfInitialArchon =1;//TODO
        centerOfFriendlyInitialArchons = new MapLocation(0,0);
        centerOfEnemyInitialArchons = new MapLocation(0,0);
        centerOfFriendlyInitialArchons = ArchonCenter(1.0 / (double)numberOfInitialArchon, centerOfFriendlyInitialArchons);
        centerOfEnemyInitialArchons = ArchonCenter(1.0 / (double)numberOfInitialArchon, centerOfEnemyInitialArchons);
        //System.out.println(centerOfFriendlyInitialArchons.x);
        //System.out.println(centerOfEnemyInitialArchons.directionTo(centerOfFriendlyInitialArchons).radians);
        awayFromEnemy = Direction.getWest();//centerOfEnemyInitialArchons.directionTo(centerOfFriendlyInitialArchons);
        //System.out.println(awayFromEnemy);
        FriendlyInitialArchonLocations = rc.getInitialArchonLocations(friendly);
        EnemyInitialArchonLocations = rc.getInitialArchonLocations(enemy);
        numberOfInitialArchon = FriendlyInitialArchonLocations.length;
        centerOfFriendlyInitialArchons = new MapLocation(0,0);
        centerOfEnemyInitialArchons = new MapLocation(0,0);
        centerOfAllInitialArchons = new MapLocation(0,0);
        for (MapLocation a : FriendlyInitialArchonLocations) {
            centerOfFriendlyInitialArchons = FastMath.addVec(centerOfFriendlyInitialArchons, a);
        }
        for (MapLocation a : EnemyInitialArchonLocations) {
            centerOfEnemyInitialArchons = FastMath.addVec(centerOfEnemyInitialArchons, a);
        }
        centerOfAllInitialArchons = FastMath.addVec(centerOfFriendlyInitialArchons, centerOfEnemyInitialArchons);
        centerOfFriendlyInitialArchons = FastMath.multiplyVec(1.0 / (double)numberOfInitialArchon, centerOfFriendlyInitialArchons);
        centerOfEnemyInitialArchons = FastMath.multiplyVec(1.0 / (double)numberOfInitialArchon, centerOfEnemyInitialArchons);
        centerOfAllInitialArchons = FastMath.multiplyVec(0.5 / (double)numberOfInitialArchon, centerOfAllInitialArchons);
        awayFromEnemy = centerOfEnemyInitialArchons.directionTo(centerOfFriendlyInitialArchons);
        towardsEnemy = awayFromEnemy.opposite();
    }

    public static MapLocation ArchonCenter(double f, MapLocation a) {
        return new MapLocation((float)f * a.x, (float)f * a.y);

    }
}
