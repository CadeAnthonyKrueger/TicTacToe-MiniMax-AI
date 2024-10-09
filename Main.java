// Cade Krueger
// This is the main class where we manage the game. The game is represented with
// a 3x3 array where spots are filled according to user input and AI decision-making.
// For how to play, see the description in the ChooseXorO() method. Otherwise, you will
// see that description each time you start a new game. The best you can achieve against
// the AI is a draw, as it will always make the most optimal move.

package Game_Files;

import Game_Files.Interfaces.*;

import java.util.*;

public class Main {

    private static final String[][] board = new String[3][3];
    private static final Scanner console = new Scanner(System.in);
    private static String playerXorO = "";
    private static String opponentXorO = "";
    private static final String[] rowLabels = {"A", "B", "C"};
    private static final HashMap<String, Integer> map = new HashMap<>();
    private static Runnable[] turnOrder;

    public static void main(String[] args) throws InterruptedException {
        InitializeBoard();
        InitializeMap();
        ChooseXorO();
        turnOrder = DecideOrder();

        boolean gameOver = false;
        while (!gameOver) {
            Thread.sleep(800);
            gameOver = Turn();
        }
        console.close();
    }

    private static void InitializeBoard() {
        for (int i = 0; i < 3; i++) { for (int j = 0; j < 3; j++) { board[i][j] = " "; } }
    }

    private static void InitializeMap() { for (int i = 0; i < 3; i++) { map.put(rowLabels[i], i); } }

    private static void ChooseXorO() {
        System.out.println("For this version of Tic Tac Toe, you will start by selecting either ");
        System.out.println("'X' or 'O'. When making inputs on the board, you will use the Letters ");
        System.out.println("[A, B, C] to represent the row of choice, and use numbers [1, 2, 3] to ");
        System.out.println("represent the column of choice. So input 'A1' would refer to the top left ");
        System.out.println("corner of the grid, and input 'C2' would refer to the bottom middle spot ");
        System.out.println("of the grid.");
        PrintBoard();
        playerXorO = VerifyInput(Main::XOInputCondition, playerXorO,
                "Would you like to be 'X' or 'O'? (Type 'X' or 'O'): ",
                "You must pick either 'X' or 'O'.",
                () -> System.out.print(""));
        if (playerXorO.equals("X")) { opponentXorO = "O"; }
        else { opponentXorO = "X"; }
        System.out.println("You have picked '" + playerXorO + "'.");
    }

    // Randomly decide who goes first regardless of choosing 'X' or 'O'
    private static Runnable[] DecideOrder() {
        Runnable playerTurn = Main::PlayerTurn;
        Runnable opponentTurn = () -> {
            try { OpponentTurn(); }
            catch (InterruptedException e) { throw new RuntimeException(e); }
        };
        Runnable[] order = { playerTurn, opponentTurn };
        if (new Random().nextInt(0,2) == 1) {
            Runnable temp = order[0]; order[0] = order[1]; order[1] = temp;
        }
        return order;
    }

    private static boolean Turn() throws InterruptedException {
        for (Runnable turn : turnOrder) {
            turn.run();
            if (GameOver(false)) { return true; }
            Thread.sleep(800);
        }
        return GameOver(false);
    }

    private static void PlayerTurn() {
        System.out.println("It is your turn.");
        PrintBoard();
        String spot = VerifyInput(Main::BoardInputCondition, "",
                "Pick a valid spot on the board (A1 - C3). You are '" + playerXorO + "':",
                "You must pick a valid spot on the board (A1 - C3).",
                Main::PrintBoard);
        FillSpot(TranslateInputToCoords(spot), playerXorO);
        PrintBoard();
    }

    private static void OpponentTurn() throws InterruptedException {
        System.out.println("It is your opponent's turn.");
        for (int i = 0; i < 3; i++) { Thread.sleep(400); System.out.print(". "); }
        System.out.println();
        // This is the AI
        int[] bestMove = MiniMax(opponentXorO, null, 0).spot;
        FillSpot(bestMove, opponentXorO);
        PrintBoard();
        System.out.println("Your opponent has picked " + GetRow(bestMove[0]) + (bestMove[1] + 1) + ".");
    }

    private static SpotScore MiniMax(String XorO, int[] currentSpot, int depth) {
        // Keep track of top level spot and return here with current score
        // Top level means the initial moves that the opponent can make on the current turn
        if (GameOver(true)) { return new SpotScore(currentSpot, CheckBoardState(opponentXorO, playerXorO) - depth); }
        else {
            ArrayList<SpotScore> spotScores = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (board[i][j].equals(" ")) {
                        if (depth == 0) { currentSpot = new int[]{i, j}; }
                        FillSpot(new int[]{i, j}, XorO);
                        spotScores.add(MiniMax(GetOpposite(XorO), currentSpot, depth + 1));
                        EmptySpot(new int[]{i, j});
                    }
                }
            }
            if (XorO.equals(opponentXorO)) { return GetMinOrMax(spotScores, SpotScore::max); }
            else { return GetMinOrMax(spotScores, SpotScore::min); }
        }
    }

    // Makes it so if there are ties, we pick randomly from the max or min values. I could have
    // found the max (or min) with Collections.max() (Collections.min()), but that chooses the
    // first max/min value every time, so this makes it a bit more random. Could have also done
    // that but shuffled the list beforehand, but that would've added unnecessary runtime.
    private static SpotScore GetMinOrMax(ArrayList<SpotScore> spotScores, MinMax minMax) {
        ArrayList<SpotScore> currentList = new ArrayList<>();
        SpotScore temp; SpotScore current = spotScores.get(0); currentList.add(current);
        for (int i = 1; i < spotScores.size(); i++) {
            temp = current;
            current = minMax.get(current, spotScores.get(i));
            if (temp.score != (int) current.score) {
                currentList = new ArrayList<>();
                currentList.add(current);
            } else if (!Arrays.equals(current.spot, temp.spot) && current.compareTo(temp) == 0) {
                currentList.add(current);
            }
        }
        return currentList.get(new Random().nextInt(0, currentList.size()));
    }


    private static int CheckBoardState(String currentPlayer, String opposingPlayer) {
        boolean playerWon = CheckWin(currentPlayer);
        boolean opponentWon = CheckWin(opposingPlayer);
        // Accounting for the depth is not entirely necessary, but it does force the AI to play
        // out the game if it is ever in a losing situation. In practice, we might send in a
        // rigged board where the AI is in a losing situation. Accounting for the depth makes it
        // so the AI will try to prolong its life for as long as possible instead of just
        // allowing itself to lose right away. It also means that if there are 2 win states, but
        // one is at a lower depth, the AI will choose the faster win state every time. It also
        // helped me to visualize different scores when diagramming this problem out on paper.

        // Returning values of 10 is necessary when accounting for the depth within the
        // Minimax algorithm. This is because certain values may be chosen incorrectly
        // depending on what the depth is. If we are minimizing, and we are using values
        // 1, 0 and -1, and we get -1 at depth 4 and 1 at depth 7, we would get values -5
        // and -6 respectively. This is bad because we would be choosing a move for the
        // minimizer that is actually a maximizing move.

        // Since the max depth of the tree (when the board is blank) is 9, 10 is the optimal
        // value for ensuring scores will give the desired result.
        if (playerWon) { return 10; }
        else if (opponentWon) { return -10; }
        else { return 0; }
    }

    private static boolean CheckWin(String XorO) {
        for (int i = 0; i < 3; i++) {
            if ((board[i][0] + board[i][1] + board[i][2]).equals(XorO + XorO + XorO)) {
                return true;
            } else if ((board[0][i] + board[1][i] + board[2][i]).equals(XorO + XorO + XorO)) {
                return true;
            } else if ((board[i][0] + board[1][1] + board[2-i][2]).equals(XorO + XorO + XorO)) {
                return true;
            }
        }
        return false;
    }

    private static boolean GameOver(boolean inMiniMax) {
        int currentBoard = CheckBoardState(playerXorO, opponentXorO);
        if (currentBoard == 10) {
            if (!inMiniMax) System.out.println("You Win!");
            return true;
        } else if (currentBoard == -10) {
            if (!inMiniMax) System.out.println("AI Wins...");
            return true;
        } else if (BoardFull()) {
            if (!inMiniMax) System.out.println("Game is a draw.");
            return true;
        }
        return false;
    }

    // Helper Methods
    private static void FillSpot(int[] coords, String XorO) {
        board[coords[0]][coords[1]] = XorO;
    }

    private static void EmptySpot(int[] coords) {
        board[coords[0]][coords[1]] = " ";
    }

    private static int[] TranslateInputToCoords(String spot) {
        return new int[]{ map.get(spot.substring(0, 1)), Integer.parseInt(spot.substring(1, 2)) - 1 };
    }

    private static String GetOpposite(String XorO) {
        if (XorO.equals("X")) { return "O"; } else return "X";
    }

    private static String GetRow(int n) {
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (entry.getValue() == n) { return entry.getKey(); }
        }
        return null;
    }

    private static boolean BoardFull() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j].equals(" ")) {
                    return false;
                }
            }
        }
        return true;
    }

    // Console input/output methods
    private static void PrintBoard() {
        StringBuilder sb = new StringBuilder("   1  2  3");
        for (int i = 0; i < 3; i++) {
            sb.append(String.format("\n%s [%s][%s][%s]", rowLabels[i], board[i][0], board[i][1], board[i][2]));
        }
        System.out.println(sb);
    }

    private static String VerifyInput(InputCheck condition, String input, String request, String errorMessage, Runnable extra) {
        while (condition.Check(input)) {
            try {
                System.out.println(request);
                input = console.next().toUpperCase();
                if (condition.Check(input))
                {
                    System.out.println(errorMessage);
                    extra.run();
                }
            } catch (Exception e) {
                console.next();
                System.out.println(errorMessage);
                extra.run();
            }
        }
        return input;
    }

    private static boolean XOInputCondition(String input) {
        return !input.equals("X") && !input.equals("O");
    }

    private static boolean BoardInputCondition(String input) {
        if (input.length() == 2) {
            int col;
            try { col = Integer.parseInt(input.substring(1, 2)); }
            catch (NumberFormatException e) { return true; }
            if (map.containsKey(input.substring(0, 1)) && map.containsValue(col - 1)) {
                int[] coords = TranslateInputToCoords(input);
                return !board[coords[0]][coords[1]].equals(" ");
            }
        }
        return true;
    }

}