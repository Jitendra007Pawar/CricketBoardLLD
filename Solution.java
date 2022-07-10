import java.util.*;
import java.util.regex.*;

enum BallStatus {
    VALID, INVALID;
}

/*
    This Enum represents match results
 */
enum MatchResult {
    InProgress, Team_One, Team_Two, Tie
}


//----------------------------------------------------------------------------------------------


/*
    This will be used to define status of Ball
    This can be valid or InValid
 */

/*
    This Eception will thrown for invaild exception
 */
class InvalidInputExeption extends Exception {
    public InvalidInputExeption(String s) {
        super(s);
    }
}


//----------------------------------------------------------------------------------------------


/*
    This Eception will thrown for invaild exception
 */
class DuplicateInputExeption extends Exception {
    public DuplicateInputExeption(String s) {
        super(s);
    }
}


//----------------------------------------------------------------------------------------------


/*
    This will be used to define each ball of over
 */
class Ball {
    private BallStatus ballStatus;
    /*
        Total runs scored on ball
     */
    private int runsScored;
    /*
        This define that wicket is taken on this ball or not
     */
    private boolean isWicket;

    Ball(BallStatus ballStatus, int runsScored, boolean isWicket) {
        this.ballStatus = ballStatus;
        this.runsScored = runsScored;
        this.isWicket = isWicket;
    }

    public BallStatus getBallStatus() {
        return ballStatus;
    }

    public boolean isWicket() {
        return isWicket;
    }

    public int getRunsScored() {
        return runsScored;
    }
}


//----------------------------------------------------------------------------------------------

/*
    This class contains util methods
*/
class Utils {
    /*
        This method wil be used to check string is number or not
    */
    private static Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        return pattern.matcher(strNum).matches();
    }
}


//----------------------------------------------------------------------------------------------


/*
    This class wil be used for actual procesing of each ball
 */
class ProcessingBallHelper {
    public Ball interpretBall(String currentBall) throws InvalidInputExeption {
        switch (currentBall) {
            case "W":
                return new Ball(BallStatus.VALID, 0, true);
            case "Wd":
                return new Ball(BallStatus.INVALID, 0, false);
            case "N":
                return new Ball(BallStatus.INVALID, 0, false);
        }


        if (!Utils.isNumeric(currentBall)) {
            throw new InvalidInputExeption("Invalid Input :- " + currentBall);
        }

        int scoreOnCurrentBall = Integer.parseInt(currentBall);
        if (scoreOnCurrentBall > 6 || scoreOnCurrentBall == 5) {
            throw new InvalidInputExeption("Invalid Input :- " + currentBall);
        }
        return new Ball(BallStatus.VALID, scoreOnCurrentBall, false);
    }
}


//----------------------------------------------------------------------------------------------


/*
    This is base class player which contains attribute common across multiple players
 */
abstract class Player {
    String name;
}


//----------------------------------------------------------------------------------------------


/*
    This is class represent each Btasman which is playing
 */
class Batsman extends Player {
    private int runsScored;
    private boolean onStrike;
    private int fours;
    private int sixes;
    private int ballPlayed;

    Batsman(String name) {
        this.name = name;
        this.runsScored = 0;
    }

    public void setPlayerOnStike() {
        this.onStrike = true;
    }

    public void removePlayerFromStrike() {
        this.onStrike = false;
    }

    public void updatePlayerStats(Ball ball) {
        if (ball.getRunsScored() == 4) {
            fours++;
        } else if (ball.getRunsScored() == 6) {
            sixes++;
        }
        if (ball.getBallStatus().equals(ball.getBallStatus().VALID)) {
            ballPlayed++;
        }
        runsScored += ball.getRunsScored();
    }

    private String getStatus() {
        if (onStrike) {
            return "*";
        }
        return "";
    }

    public void printStatsForCurrentBatsMan() {
        String template = "%-10s %26s %16s %19s %19s %19s %n";
        System.out.printf(template, name + getStatus(), runsScored, fours, sixes, ballPlayed, String.format("%.2f", runsScored * 100.0 / ballPlayed));
    }

}


//----------------------------------------------------------------------------------------------


/*
    This is class represent each Baller which is playing
 */
class Bowler extends Player {
    private int runsConceded;
    private int noOfWickets;
    private int dotBall;
    private int totalNoOfballs;


    Bowler(String name) {
        this.name = name;
        this.runsConceded = 0;
    }

    public void updatePlayerStats(Ball ball) {
        if (ball.getRunsScored() == 0) {
            dotBall++;
        } else if (ball.isWicket()) {
            noOfWickets++;
        }

        if (ball.getBallStatus().equals(ball.getBallStatus().VALID)) {
            totalNoOfballs++;
        }

        runsConceded += ball.getRunsScored();
    }

    public void printStatsForCurrentBowler() {
        int noOfOvers = totalNoOfballs / 6;
        String noOfOversString = noOfOvers + (totalNoOfballs % 6 != 0 ? "." + totalNoOfballs % 6 + "" : "");
        String template = "%-10s %26s %16s %19s %19s %19s %n";
        System.out.printf(template, name, runsConceded, noOfOversString, noOfWickets, dotBall, String.format("%.2f", (runsConceded * 6.0) / totalNoOfballs));
    }

}


//----------------------------------------------------------------------------------------------


/*
    This class represnt each Team which is playing
 */
class Team {
    private List<Batsman> players;
    private HashMap<String, Bowler> oppositeTeamBowlerList;
    private String teamName;
    private int nextPlayerIndex;
    private int playerOnStikeIndex;
    private int oppositeEndPlayerIndex;
    private int extras;
    private int totalRuns;
    private int noOfwickets;

    Team(List<String> nameOfPlayers, String teamName) {
        players = new ArrayList<>();
        oppositeTeamBowlerList = new HashMap<>();
        for (int i = 0; i < nameOfPlayers.size(); i++) {
            players.add(new Batsman(nameOfPlayers.get(i)));
        }
        this.teamName = teamName;
        players.get(0).setPlayerOnStike();
        players.get(1).setPlayerOnStike();
        nextPlayerIndex = 2;
        playerOnStikeIndex = 0;
        oppositeEndPlayerIndex = 1;
    }

    public String getTeamName() {
        return teamName;
    }

    public int getExtras() {
        return extras;
    }

    public int getTotalRuns() {
        return totalRuns;
    }

    public int getNoOfwickets() {
        return noOfwickets;
    }

    private void setNextBatsman() {
        players.get(playerOnStikeIndex).removePlayerFromStrike();
        playerOnStikeIndex = nextPlayerIndex;
        players.get(nextPlayerIndex).setPlayerOnStike();
        nextPlayerIndex++;
    }

    public void changeStrike() {
        int tempIndex = playerOnStikeIndex;
        playerOnStikeIndex = oppositeEndPlayerIndex;
        oppositeEndPlayerIndex = tempIndex;
    }

    public void updateStutasOfTeam(String bowlerName, Ball ball) {

        players.get(playerOnStikeIndex).updatePlayerStats(ball);

        if (oppositeTeamBowlerList.get(bowlerName) == null) {
            oppositeTeamBowlerList.put(bowlerName, new Bowler(bowlerName));
        }
        oppositeTeamBowlerList.get(bowlerName).updatePlayerStats(ball);

        totalRuns += ball.getRunsScored();
        if (ball.isWicket()) {
            noOfwickets++;
            if (noOfwickets == players.size() - 1) {
                players.get(playerOnStikeIndex).removePlayerFromStrike();
                return;
            }
            setNextBatsman();
        } else if (ball.getRunsScored() % 2 != 0) {
            changeStrike();
        }

        if (ball.getBallStatus().equals(BallStatus.INVALID)) {
            extras++;
            totalRuns++;
        }
    }

    public void printStats() {
        System.out.println("Batting stats :- ");
        String titleTemplate = "%-10s %26s %16s %19s %19s %19s %n";
        System.out.printf(titleTemplate, "Player Name", "Score", "4s", "6s", "Balls", "Strike Rate");
        for (int i = 0; i < players.size(); i++) {
            players.get(i).printStatsForCurrentBatsMan();
        }
        System.out.println();
        System.out.println("Bowling stats :- ");
        System.out.printf(titleTemplate, "Player Name", "Total runs Conceded", "no Of Overs", "no Of Wickets", "dot Balls", "Economy");
        for (String currentBowler : oppositeTeamBowlerList.keySet()) {
            oppositeTeamBowlerList.get(currentBowler).printStatsForCurrentBowler();
        }
        System.out.println();
    }

    public boolean isAllOut() {
        if (noOfwickets == players.size() - 1) {
            return true;
        }
        return false;
    }
}


//----------------------------------------------------------------------------------------------

/*
    This class represnt match details which is current team and overs played
 */
class Match {
    int noOfOvers;
    int noOfPlayers;
    int getNoOfOversPlayed;
    int noOfBallOfOver;
    private Team battingTeam;
    private List<Team> teams;
    private MatchResult result;
    private ProcessingBallHelper processingBallHelper;

    Match(int noOfPlayers, int noOfOvers) {
        this.noOfPlayers = noOfPlayers;
        this.noOfOvers = noOfOvers;
        noOfBallOfOver = 0;
        getNoOfOversPlayed = 0;
        teams = new ArrayList<>();
        processingBallHelper = new ProcessingBallHelper();
        result = MatchResult.InProgress;
    }

    public Team getBattingTeam() {
        return battingTeam;
    }

    public void setBattingTeam(Team battingTeam) {
        this.battingTeam = battingTeam;
        noOfBallOfOver = 0;
        getNoOfOversPlayed = 0;
    }

    public Team createTeam(List<String> playerNames, String teamName) {
        Team team = new Team(playerNames, teamName);
        teams.add(team);
        return team;
    }

    public void processBall(String bowlerName, String currentBall) throws InvalidInputExeption {
        Ball ball = processingBallHelper.interpretBall(currentBall);
        if (ball.getBallStatus().equals(BallStatus.VALID)) {
            noOfBallOfOver++;
        }
        battingTeam.updateStutasOfTeam(bowlerName, ball);
    }

    public boolean isCurrentOverDone() {
        if (noOfBallOfOver == 6) {
            noOfBallOfOver = 0;
            getNoOfOversPlayed++;
            battingTeam.changeStrike();
            return true;
        }
        return false;
    }

    public boolean isAllOut() {
        if (battingTeam.isAllOut()) {
            return true;
        }
        return false;
    }

    public void printStats() {
        System.out.println();
        battingTeam.printStats();
        System.out.println("Total : " + battingTeam.getTotalRuns() + "/" + battingTeam.getNoOfwickets());
        System.out.println("Overs : " + getNoOfOversPlayed + "." + noOfBallOfOver);
        System.out.println("Team Extras : " + battingTeam.getExtras());
        System.out.println();
    }

    public boolean isMatchOver() {
        if (teams.get(0).getTotalRuns() < battingTeam.getTotalRuns()) {
            result = MatchResult.Team_Two;
            return true;
        }
        return false;
    }

    public void updateMatchStatus() {
        if (teams.get(0).getTotalRuns() > battingTeam.getTotalRuns()) {
            result = MatchResult.Team_One;
        } else if (teams.get(0).getTotalRuns() == battingTeam.getTotalRuns()) {
            result = MatchResult.Tie;
        }
    }

    public void printMatchResult() {
        if (MatchResult.Team_One.equals(result)) {
            int diff = teams.get(0).getTotalRuns() - battingTeam.getTotalRuns();
            System.out.println("Result: Team 1 won the match by " + diff + " runs");
        } else if (MatchResult.Team_Two.equals(result)) {
            int diff = noOfPlayers - battingTeam.getNoOfwickets() - 1;
            System.out.println("Result: Team 2 won the match by " + diff + " wickets");
        } else {
            System.out.println("Result: Match tied waiting for Super over");
        }
    }
}

//----------------------------------------------------------------------------------------------


public class Solution {

    static Match currentCricketMatch;
    static Scanner sc;

    public static void getOversInput(int noOfOvers) {
        for (int i = 0; i < noOfOvers; i++) {
            System.out.println("Enter Name Of Bowler of " + (i + 1) + " Over :- ");
            String bowlerName = sc.next();
            System.out.println("Over " + (i + 1) + ":");
            while (!currentCricketMatch.isCurrentOverDone() && !currentCricketMatch.isAllOut() && !currentCricketMatch.isMatchOver()) {
                try {
                    currentCricketMatch.processBall(bowlerName, sc.next());
                } catch (InvalidInputExeption exeption) {
                    System.out.println("Enter Valid Input Again");
                }
            }
            System.out.println("Scorecard for " + currentCricketMatch.getBattingTeam().getTeamName());
            currentCricketMatch.printStats();
            if (currentCricketMatch.isAllOut() || currentCricketMatch.isMatchOver()) {
                break;
            }
        }
    }

    public static void getPlayersInput(int noOfPlayers, List<String> listOfPlayersInTeam) {
        int i = 0;
        while (i < noOfPlayers) {
            try {
                String currentPlayer = sc.next();
                if (listOfPlayersInTeam.contains(currentPlayer)) {
                    throw new DuplicateInputExeption("Player already Exists please enter some other name");
                }
                listOfPlayersInTeam.add(currentPlayer);
                i++;
            } catch (DuplicateInputExeption duplicateInputExeption) {
                System.out.println(duplicateInputExeption.getMessage());
            }
        }
    }

    public static int getNoOfPlayers() throws InvalidInputExeption {
        int noOfPlayers = 0;
        try {
            noOfPlayers = sc.nextInt();
        } catch (InputMismatchException exeption) {
            throw new InvalidInputExeption("Invalid input Expecting no of players");
        }
        return noOfPlayers;
    }

    public static int getNoOfOvers() throws InvalidInputExeption {
        int noOfOvers;
        try {
            noOfOvers = sc.nextInt();
        } catch (InputMismatchException exeption) {
            throw new InvalidInputExeption("Invalid input Expecting no of overs");
        }
        return noOfOvers;
    }

    public static void main(String args[]) throws InvalidInputExeption {
        sc = new Scanner(System.in);
        System.out.println("No. of players for each team:");
        int noOfPlayers = getNoOfPlayers();

        System.out.println("No. of overs:");
        int noOfOvers = getNoOfOvers();

        currentCricketMatch = new Match(noOfPlayers, noOfOvers);

        System.out.println("Batting Order for team 1:");
        List<String> listOfPlayersInTeamA = new ArrayList<>();
        getPlayersInput(noOfPlayers, listOfPlayersInTeamA);

        Team team = currentCricketMatch.createTeam(listOfPlayersInTeamA, "A");
        currentCricketMatch.setBattingTeam(team);
        getOversInput(noOfOvers);

        System.out.println("Batting Order for team 2:");
        List<String> listOfPlayersInTeamB = new ArrayList<>();
        getPlayersInput(noOfPlayers, listOfPlayersInTeamB);

        Team teamB = currentCricketMatch.createTeam(listOfPlayersInTeamB, "B");
        currentCricketMatch.setBattingTeam(teamB);
        getOversInput(noOfOvers);

        currentCricketMatch.updateMatchStatus();
        currentCricketMatch.printMatchResult();
    }
}