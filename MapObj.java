package COMP1030.finalProject;

/**
 * Application Purpose: Final Project --RPG Game
 * Author: Keisho Seiho 200553984
 * 
 * Structure: I want to use the java point method which we watched the youtube on blackboard, but this is beyond the lecture slides, 
 *            so I use multidimensional array instead of it.
 *            seekTreasure() total supported by 21(Sub support 0~20) methods
 * Function: When moving the hero, player could see the current location on a chess board, at the specific point, will have specific game event.
 * 
 * 
 * Date: 2023-6-27~30
 */

import java.io.Console;
import java.util.Random;
import java.util.Scanner;

public class MapObj {

    private int yAxisLocation;
    private int xAxisLocation;
    private int inputDirection;
    private boolean alreadyExecuted2x2;
    private boolean alreadyExecuted2x7;
    private boolean alreadyExecuted7x2;
    private boolean alreadyExecuted7x7;
    private int bossExist = 0;
    private int monsterHP = StructureObj.st.enemy.getMonsterHP();
    private int bossHP = StructureObj.st.enemy.getBossHP();
    // player could encounter max 30 monsters
    private int alreadyEncountered[] = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21,
            22, 23, 24, 25, 26, 27, 28, 29 };

    // the first array is the x-axis coordinates of each monster, the second array
    // is the corresponding y-axis coordinates of each monster
    private int monsterPosition[][] = {
            { 2, 3, 3, 3, 2, 1, 1, 6, 7, 8, 8, 8, 7, 6, 6, 1, 2, 3, 3, 3, 2, 1, 1, 6, 7, 8, 8, 7, 6, 6 },
            { 1, 1, 2, 3, 3, 3, 2, 1, 1, 1, 2, 3, 3, 3, 2, 6, 6, 6, 7, 8, 8, 8, 7, 6, 6, 6, 7, 8, 8, 7 } };

    // Creat default constructor (use for main method)
    public MapObj() {
    }

    // Creat constructor with all 3 parameters (use for walkAroundTheMap method)
    public MapObj(int yAxisLocation, int xAxisLocation, int inputDirection) {
        this.yAxisLocation = yAxisLocation;
        this.xAxisLocation = xAxisLocation;
        this.inputDirection = inputDirection;
    }

    /*---------------------------------Walk Around the Map----------------------------------------------------------------------*/
    public void walkAroundTheMap() {
        // Create new map object, player initial location is (1,1)
        MapObj map = new MapObj(1, 1, 0);

        while (map.bossExist == 0) {
            map.showMap();
            map.sendLocation();
            System.out.println("[OPTION]:    Please pick a direction: 1.North 2.South 3.East 4.West");
            Console con = System.console();// Create System.console instead of System.in;
            Scanner in = new Scanner(con.reader());// Can not close Scanner(System.in) in a while loop because
                                                   // in.close() will cause error.
            int inputDirection = in.nextInt();
            System.out.println("\n\n");
            map.setInputDirection(inputDirection);
            map.walkOnTheMap();
            map.seekTreasure();
            if (StructureObj.st.pHP <= 0 || StructureObj.st.pMANA <= 0) {
                break;
            }
        }
        if (StructureObj.st.pHP <= 0 || StructureObj.st.pMANA <= 0 || map.bossExist == 2) {
            System.out.println("GAME OVER");
        } else if (map.bossExist == 1) {

            StructureObj.st.showEndingStory();
            showYouWin();
        }

    }

    /*
     * Create show map method, which could be able to show a chess board on
     * terminal;
     * Use nested for loops which means a for loop in another for loop, instantiate
     * a 8x8 "[]"
     */
    public void showMap() {

        // Use nested for loops making a game board
        for (int yDirection = 0; yDirection < 9; yDirection++) {

            for (int xDirection = 0; xDirection < 9; xDirection++) {

                // Display x coordinate unit
                if (yDirection == 0 && xDirection != 0) {
                    System.out.print("[" + xDirection + "]");
                }
                // Display y coordinate unit
                else if (xDirection == 0 && yDirection != 0) {
                    System.out.print("[" + yDirection + "]");
                }

                // Display player location
                else if (xDirection == xAxisLocation && yDirection == yAxisLocation) {
                    System.out.print("[X]");
                }
                // Display location except player
                else {
                    System.out.print("[ ]");
                }

            }
            System.out.println();
        }
    }

    /*
     * Walk on the chess board, this event is controlled by the main method
     * command input;
     * case1 is moving North, case2 is moving South, case3 is moving East, case4 is
     * moving West.
     */
    public int walkOnTheMap() {

        switch (inputDirection) {
            case 1:
                if (yAxisLocation > 1) {
                    yAxisLocation--;
                    return yAxisLocation;
                } else {
                    yAxisLocation = 1;
                    return yAxisLocation;
                }
            case 2:
                if (yAxisLocation < 8) {
                    yAxisLocation++;
                    return yAxisLocation;
                } else {
                    yAxisLocation = 8;
                    return yAxisLocation;
                }
            case 3:
                if (xAxisLocation < 8) {
                    xAxisLocation++;
                    return xAxisLocation;
                } else {
                    xAxisLocation = 8;
                    return xAxisLocation;
                }
            case 4:
                if (xAxisLocation > 1) {
                    xAxisLocation--;
                    return xAxisLocation;
                } else {
                    xAxisLocation = 1;
                    return xAxisLocation;
                }
        }
        return inputDirection;

    }

    /*
     * Create send location method, for clear subdividing, this method is separated
     * from the walkOnTheMap method.
     * The function is showing player's current location by using a plane coordinate
     * system with a connection on the chess board.
     */
    public void sendLocation() {
        System.out
                .println("\n[POSITION]:  " + StructureObj.st.hero.getHeroFirstName() + " corrent location is: " + "("
                        + xAxisLocation + "," + yAxisLocation + ")");

        if (yAxisLocation == 8) {
            System.out.println("[ALERT]:     " + StructureObj.st.hero.getHeroFirstName()
                    + " have arrived at the SOUTH END of the world.");
        }

        if (yAxisLocation == 1) {
            System.out.println("[ALERT]:     " + StructureObj.st.hero.getHeroFirstName()
                    + " have arrived at the NORTH END of the world.");
        }

        if (xAxisLocation == 8) {
            System.out.println("[ALERT]:     " + StructureObj.st.hero.getHeroFirstName()
                    + " have arrived at the EAST END of the world.");
        }

        if (xAxisLocation == 1) {
            System.out.println("[ALERT]:     " + StructureObj.st.hero.getHeroFirstName()
                    + " have arrived at the WEST END of the world.");
        }
    }

    /*------------------------- Create seek treasure method--(USE SUB SUPPORT METHOD  0~4, 15)--------------- */
    /*
     * This method handles rest point, treasure point, monster point, boss point on
     * the map and show player status
     */
    public void seekTreasure() {
        seekRestPoint();
        seekTreasurePoint1();
        seekTreasurePoint2();
        seekTreasurePoint3();
        seekTreasurePoint4();
        encounterMonster();
        encounterBoss();

        System.out.println("\n[PLAYER STATUS]:  Jesas Bread: " + StructureObj.st.jesasBread);
        System.out.println("                  Jesas Tear: " + StructureObj.st.jesasTear);
        System.out.println("                  Player HP: " + StructureObj.st.pHP);
        System.out.println("                  Player MANA: " + StructureObj.st.pMANA);
        System.out.println();

    }

    /*--SUB SUPPORT METHOD  0---------------Create encounter monster method--(USE SUB SUPPORT METHOD  9, 10)--------------- */
    /*
     * If player encountered a monster, the value of the corresponding element in
     * the array alreadyEncountered[] would become 30 (or any number >29), which
     * means at each position
     * on the map, encounter monster event only happen one time.
     * 
     */
    public int encounterMonster() {
        for (int i = 0; i < 30; i++) {
            if (alreadyEncountered[i] == i) {
                if (xAxisLocation == monsterPosition[0][i] && yAxisLocation == monsterPosition[1][i]) {
                    showMonsterMessage();
                    battle();
                    alreadyEncountered[i] = 30;
                }
            }
        }
        return alreadyEncountered[0];
    }

    /* Create 4 seek treasure event */
    /*
     * like encounter monstor, I use if statement to control method only happen one
     * time. player can not get treasure twice at the same point.
     */

    /*--SUB SUPPORT METHOD  1---------------(USE SUB SUPPORT METHOD  5,7)---------------------------------------------------------*/

    public boolean seekTreasurePoint1() {
        // On this point, encounter treasure event only happen one time.
        if (!alreadyExecuted2x2) {
            if (yAxisLocation == 2 && xAxisLocation == 2) {
                showJesasBreadMessage();
                improveHP();
                alreadyExecuted2x2 = true;
            }
        }
        return alreadyExecuted2x2;
    }

    /*--SUB SUPPORT METHOD  2---------------(USE SUB SUPPORT METHOD  5,7)---------------------------------------------------------*/

    public boolean seekTreasurePoint2() {
        // if statement excute only once
        if (!alreadyExecuted2x7) {
            if (yAxisLocation == 2 && xAxisLocation == 7) {
                showJesasBreadMessage();
                improveHP();
                alreadyExecuted2x7 = true;
            }
        }
        return alreadyExecuted2x7;
    }

    /*--SUB SUPPORT METHOD  3---------------(USE SUB SUPPORT METHOD  6,8)---------------------------------------------------------*/

    public boolean seekTreasurePoint3() {
        // if statement excute only once
        if (!alreadyExecuted7x7) {
            if (yAxisLocation == 7 && xAxisLocation == 7) {
                showJesasTearMessage();
                improveMANA();
                alreadyExecuted7x7 = true;
            }
        }
        return alreadyExecuted7x7;
    }

    /*--SUB SUPPORT METHOD  4---------------(USE SUB SUPPORT METHOD  6,8)---------------------------------------------------------*/

    public boolean seekTreasurePoint4() {
        // if statement excute only once
        if (!alreadyExecuted7x2) {
            if (yAxisLocation == 7 && xAxisLocation == 2) {
                showJesasTearMessage();
                improveMANA();
                alreadyExecuted7x2 = true;
            }
        }
        return alreadyExecuted7x2;
    }

    /* Create show treasure message method */
    /*--SUB SUPPORT METHOD  5-----------------------------------------------------------------------------*/

    public void showJesasBreadMessage() {
        System.out.println(
                "[GAME EVENT]:  " + StructureObj.st.hero.getHeroFirstName() + " get one piece of Jesas Bread!");
        System.out.println(
                "               According to legend, this bread is one piece of the miracle loaves feeding the 5,000.");
        System.out
                .println("               Jesas: NO! I dropped my leftover on the chessboard when I fight with Satan.");
    }

    /*--SUB SUPPORT METHOD  6-----------------------------------------------------------------------------*/
    public void showJesasTearMessage() {
        System.out
                .println("[GAME EVENT]:  " + StructureObj.st.hero.getHeroFirstName() + " get one drop of Jesas Tear!");
        System.out.println(
                "               According to legend, this drop of tear is the one Jesas shed for the suffering.");
        System.out.println("               Satan: NO!.Jesas cried about losing the game.");
    }

    /*
     * Create HP improve method: as player get the treasure, original HP + 500, and
     * set HP
     */
    /*--SUB SUPPORT METHOD  7-----------------------------------------------------------------------------*/

    public double improveHP() {
        StructureObj.st.pHP = StructureObj.st.hero.getPlayerHP() + 500;
        StructureObj.st.hero.setPlayerHP(StructureObj.st.pHP);
        StructureObj.st.jesasBread++;
        return StructureObj.st.jesasBread;
    }

    /*
     * Create MANA improve method: as player get the treasure, original MANA + 500,
     * and set MANA
     */
    /*--SUB SUPPORT METHOD  8-----------------------------------------------------------------------------*/
    public double improveMANA() {
        StructureObj.st.pMANA = StructureObj.st.hero.getPlayerMANA() + 500;
        StructureObj.st.hero.setPlayerMANA(StructureObj.st.pMANA);
        StructureObj.st.jesasTear++;
        return StructureObj.st.jesasTear;
    }

    /*--SUB SUPPORT METHOD  9-----------------------------------------------------------------------------*/
    /*
     * Creat show monster message method: every time when enconter monster, show
     * this message
     */
    public void showMonsterMessage() {
        System.out.println("[GAME EVENT]:  " + StructureObj.st.hero.getHeroFirstName() + " ran into a monster!");

    }

    /*--SUB SUPPORT METHOD  10---------------(USE SUB SUPPORT METHOD  11,12)---------------------------------------------------------*/
    /*
     * Create Battle method: in the real world, HP is similar as the fighter's life,
     * and MANA is energy, if the fighter lost energy he couldn't attack, and if his
     * body damaged, he would have less chance to dodge the enemy.
     * 
     * So, the HP affects the chance of dodge, and HP = 0 means game over, MANA = 0
     * also means game over, for this reason, HP&mana alert message is necessary.
     * 
     * And, after alert, escape form the battle(escape method) is required.
     * 
     * And, player can not escape from Boss.
     */
    public void battle() {
        System.out.println("1. Attack  2.Escape");
        Console con = System.console();
        Scanner in = new Scanner(con.reader());
        int input = in.nextInt();
        switch (input) {
            case 1:
                attack();
                break;
            case 2:
                escape();
                break;
            default:
                escape();
                break;
        }

    }

    /*--SUB SUPPORT METHOD  11-----------------------------------------------------------------------------*/
    /*
     * Create escape method: escape from battle, but the next time at the same
     * position the monster will not be there because of the encounter method.
     * With my limited Java knowledge, I escape from this bug...
     */
    public void escape() {
        System.out.println("[GAME EVENT]:  " + StructureObj.st.hero.getHeroFirstName()
                + " ESCAPE SUCCESSFULLY! \nMonster: Come back and fight! You coward!");
    }

    /*--SUB SUPPORT METHOD  12---------------(USE SUB SUPPORT METHOD  13,14)---------------------------------------------------------*/
    /*
     * Cteate attack method: player dodge chance depend on player HP, >=80 have 40%
     * chance, <50 have 10%, else have 20%.
     * Each attack will cost 3 MANA, deals 5 damage to monster, and lost 4 player
     * HP.
     * player HP or MANA <15 will get alert message.
     * When player HP or MANA is <= 0, player die, if monster HP is <=0, monster be
     * kiled.
     */
    public void attack() {
        monsterHP();// make sure monster has its original HP in each monster attack envent
        int dodgeChance;
        if (StructureObj.st.pHP >= 80) {
            dodgeChance = 4;
        } else if (StructureObj.st.pHP >= 50 && StructureObj.st.pHP < 80) {
            dodgeChance = 2;
        } else {
            dodgeChance = 1;
        }

        int playerDamage = 5;// each attack will damage monster 5 HP from player
        int playerMANA_Cost = 3;// each attack will cost player 3MANA
        int monsterDamage = 4;// each attack will damage player 4 HP from monster

        while (monsterHP > 0 && StructureObj.st.pHP > 0 && StructureObj.st.pMANA > 0) {
            Random randHitChance = new Random();
            int hitChance = randHitChance.nextInt(11);
            if (hitChance < dodgeChance) {
                System.out
                        .println("[BATTLE INFO]:  " + StructureObj.st.hero.getHeroFirstName() + " dodged the attack!");
                continue;
            }
            StructureObj.st.pHP = StructureObj.st.pHP - monsterDamage;
            StructureObj.st.pMANA = StructureObj.st.pMANA - playerMANA_Cost;
            monsterHP = monsterHP - playerDamage;
            System.out.println("[BATTLE INFO]:  " +
                    StructureObj.st.hero.getHeroFirstName() + " took a hit and has " + StructureObj.st.pHP
                    + " HP left");
            System.out.println(
                    "[BATTLE INFO]:  " + StructureObj.st.hero.getHeroFirstName() + " deals 5 damage to monster and has "
                            + StructureObj.st.pMANA + " MANA left");

            alertHPandMANA();

            if (StructureObj.st.pHP <= 0 || StructureObj.st.pMANA <= 0) {
                StructureObj.st.pHP = 0;
                StructureObj.st.pMANA = 0;
                System.out.println("[BATTLE INFO]:  " + StructureObj.st.hero.getHeroFirstName() + " is dead.");
                break;
            }
            if (monsterHP <= 0) {
                monsterHP = 0;
                System.out
                        .println("[BATTLE INFO]:  " + StructureObj.st.hero.getHeroFirstName() + " killed the monster");
                break;
            }
        }
    }

    /*--SUB SUPPORT METHOD  13-----------------instantiate monster initial HP method-----------------------*/
    /* Each time killed a monster, monster HP shold be reset for next battle */
    public int monsterHP() {
        monsterHP = StructureObj.st.enemy.getMonsterHP();
        return monsterHP;
    }

    /*--SUB SUPPORT METHOD  14-----------------------------------------------------------------------------*/
    /* Create HP&MANA alert method: when player HP or MANA < 15, alert message */
    public void alertHPandMANA() {
        if (StructureObj.st.pHP < 15 || StructureObj.st.pMANA < 15) {
            System.out.println("[ALERT]:        " + StructureObj.st.hero.getHeroFirstName() + " IS DYING! RUN!");
        }
    }

    /*--SUB SUPPORT METHOD  15-----instantiate player rest method: HP & MANA recover-(USE SUB SUPPORT METHOD 16,17) -----------------*/
    /* At rest(1,1) point player HP&MANA will refill */
    public void seekRestPoint() {
        if (yAxisLocation == 1 && xAxisLocation == 1) {
            restHP();
            System.out.println("[GAME EVENT]:  Full HP recovered");
            restMANA();
            System.out.println("[GAME EVENT]:  Full MANA recovered");
        }

    }

    /*--SUB SUPPORT METHOD   16-----------------instantiate player rest method: HP recover------------------------------*/
    public int restHP() {
        StructureObj.st.pHP = StructureObj.st.hero.getPlayerHP();
        return StructureObj.st.pHP;
    }

    /*--SUB SUPPORT METHOD   17-----------------instantiate player rest method: MANA recover--------------------------- */
    public int restMANA() {
        StructureObj.st.pMANA = StructureObj.st.hero.getPlayerMANA();
        return StructureObj.st.pMANA;
    }

    /*--SUB SUPPORT METHOD  18-----------------instantiate Boss initial HP method-----------------------*/
    /*
     * boss HP reference StructureObj new class st object of enemy (10 times of
     * monster HP)
     */
    public int bossHP() {
        bossHP = StructureObj.st.enemy.getBossHP();
        return bossHP;
    }

    /*--SUB SUPPORT METHOD  19-----------------Cteate encounter boss method---(USE SUB SUPPORT METHOD 20)------------------------*/
    /*
     * walk on map is a loop method, if boss died, player will get out of the loop,
     * I use bossExist variable to handle loop, bossExist=1 is good end(true
     * ending), bossExist=2 is bad end.
     * Player can not escape from boss.
     */
    public int encounterBoss() {

        if (yAxisLocation == 8 && xAxisLocation == 8) {

            System.out.println("[GAME EVENT]   Satan: HAHAHAHAH, Human " + StructureObj.st.hero.getGender()
                    + " are you ready for challenging me?");
            System.out.println(StructureObj.st.hero.getHeroFirstName()
                    + "  1. Yes, in the name of God, die, demon!  2. No, I..., I beg your pardon me, I am sorry to bother your rest.");
            Console con = System.console();
            Scanner in = new Scanner(con.reader());
            int input = in.nextInt();
            in.close();
            switch (input) {
                case 1:
                    attackBoss();
                    return bossExist;
                case 2:
                    System.out.println("[GAME EVENT]   Satan: HAHAHAHAH, humans will be humans.");
                    System.out.println("[GAME EVENT]   " + StructureObj.st.hero.getHeroFirstName()
                            + " died because of the act of weakness.");
                    StructureObj.st.pHP = 0;
                    StructureObj.st.pMANA = 0;
                    return bossExist = 2;
                default:
                    System.out.println("[GAME EVENT]   Satan: HAHAHAHAH, humans will be humans.");
                    System.out.println(StructureObj.st.hero.getHeroFirstName() + "DIED");
                    StructureObj.st.pHP = 0;
                    StructureObj.st.pMANA = 0;
                    return bossExist = 2;
            }

        }
        return bossExist;
    }

    /*--SUB SUPPORT METHOD  20-----------------Creat attackBoss method-------------------------------------------------------------------- */
    /* Fight with boss, player dodge chance lower than the case of monster. */
    public void attackBoss() {
        bossHP(); // for another round may be useful
        int dodgeChance;
        if (StructureObj.st.pHP >= 80) {
            dodgeChance = 2;
        } else if (StructureObj.st.pHP >= 50 && StructureObj.st.pHP < 80) {
            dodgeChance = 1;
        } else {
            dodgeChance = 0;
        }

        int playerDamage = 5;// each attack will damage boss 5 HP from player
        int playerMANA_Cost = 3;// each attack will cost player 3MANA
        int bossDamage = 7;// each attack will damage player 7 HP from boss

        while (bossHP > 0 && StructureObj.st.pHP > 0 && StructureObj.st.pMANA > 0) {
            Random randHitChance = new Random();
            double hitChance = randHitChance.nextDouble(10.0) * 10;
            if (hitChance < dodgeChance) {
                System.out
                        .println("[BATTLE INFO]:  " + StructureObj.st.hero.getHeroFirstName() + " dodged the attack!");
                continue;
            }
            StructureObj.st.pHP = StructureObj.st.pHP - bossDamage;
            StructureObj.st.pMANA = StructureObj.st.pMANA - playerMANA_Cost;
            bossHP = bossHP - playerDamage;
            System.out.println("[BATTLE INFO]:  " +
                    StructureObj.st.hero.getHeroFirstName() + " took a hit and has " + StructureObj.st.pHP
                    + " HP left");
            System.out.println(
                    "[BATTLE INFO]:  " + StructureObj.st.hero.getHeroFirstName()
                            + " deals 5 damage to the dragon and has "
                            + StructureObj.st.pMANA + " MANA left");

            if (StructureObj.st.pHP <= 0 || StructureObj.st.pMANA <= 0) {
                StructureObj.st.pHP = 0;
                StructureObj.st.pMANA = 0;
                System.out.println("[BATTLE INFO]:  " + StructureObj.st.hero.getHeroFirstName() + " is dead.");
                bossExist = 2;
                break;
            }
            if (bossHP <= 0) {
                bossHP = 0;
                System.out
                        .println("[BATTLE INFO]:  " + StructureObj.st.hero.getHeroFirstName() + " killed the dragon");
                bossExist = 1;
                break;
            }
        }
    }

    /*--SUB SUPPORT METHOD  21-----------------Create event for winner--------------------------------------------------------*/
    /* this is just for fun */
    public void showYouWin() {
        // instantiate a 9x9 array
        String twoArray[][] = new String[9][9];
        for (int i = 0; i < 9; i++) {
            for (String[] a : twoArray) {
                a[i] = "[ ]";

            }
        }
        twoArray[1][1] = "[Y]";
        twoArray[2][2] = "[O]";
        twoArray[3][3] = "[U]";
        twoArray[5][5] = "[W]";
        twoArray[6][6] = "[I]";
        twoArray[7][7] = "[N]";
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 8; j++) {

                System.out.print(twoArray[i][j]);
            }
            System.out.println(twoArray[0][i]);
        }
    }

    /*-------------------------------Creat getters and setters---------------------------------------------- */
    public int getyAxisLocation() {
        return yAxisLocation;
    }

    public void setyAxisLocation(int yAxisLocation) {
        this.yAxisLocation = yAxisLocation;
    }

    public int getxAxisLocation() {
        return xAxisLocation;
    }

    public void setxAxisLocation(int xAxisLocation) {
        this.xAxisLocation = xAxisLocation;
    }

    public int getInputDirection() {
        return inputDirection;
    }

    public void setInputDirection(int inputDirection) {
        this.inputDirection = inputDirection;
    }
    /*---------------------------------------------------------------------------------------------------------- */
}
