import java.util.ArrayList;
import java.util.Scanner;

enum CellState {
    FOG("~"),
    SHIP("O"),
    HIT("X"),
    MISS("M");
    public final String symbol;

    CellState(String symbol) {
        this.symbol = symbol;
    }
}

/**
 * Cell class stores it's coordinates and state.
 */
class Cell {
    public int x;
    public int y;
    public CellState cellState;

    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
        this.cellState = CellState.FOG;
    }

    @Override
    public String toString() {
        return String.format("(x: %d, y: %d, |%s|)", this.x, this.y, this.cellState.symbol);
    }

}

class Ship {
    public String name;
    public int health;
    public ArrayList<Cell> cells;

    public Ship(String name, int health) {
        this.name = name;
        this.health = health;
        this.cells = new ArrayList<>();
    }

    public void addCell(Cell cell) {
        this.cells.add(cell);
    }

    /**
     * Returns false if
     * ship has lost
     * all health.
     */
    public boolean checkHealth() {
        this.health = 0;
        for (Cell cell:cells) {
            if (cell.cellState != CellState.HIT) {
                health++;
            }
        }
        return this.health != 0;
    }

}

class BattleMap {
    public String playerName;
    public final int MAP_SIZE;
    public Cell[][] mapArray;
    public ArrayList<Ship> shipsList;

    //int shipsAliveCount;

    public BattleMap() {
        MAP_SIZE = 10;
        mapArray = new Cell[MAP_SIZE][MAP_SIZE];

        for (int y = 0; y < MAP_SIZE; y++) {
            for (int x = 0; x < MAP_SIZE; x++) {
                mapArray[y][x] = new Cell(x, y);
            }
        }

        shipsList = new ArrayList<>();
    }

    @Override
    public String toString() {
        StringBuilder map = new StringBuilder();

        map.append("  1 2 3 4 5 6 7 8 9 10\n");
        for (int y = 0; y < MAP_SIZE; y++) {
            // Unicode capital letters start at 65
            map.append((char) (y + 65)).append(" ");

            for (int x = 0; x < MAP_SIZE; x++) {
                map.append(mapArray[y][x].cellState.symbol).append(" ");
            }
            map.append("\n");
        }

        return map.toString();
    }

    /**
     * Prints map with
     * hidden ships.
     */
    public String toStringHidden() {
        StringBuilder map = new StringBuilder();

        map.append("  1 2 3 4 5 6 7 8 9 10\n");
        for (int y = 0; y < MAP_SIZE; y++) {
            // Unicode capital letters start at 65
            map.append((char) (y + 65)).append(" ");

            for (int x = 0; x < MAP_SIZE; x++) {
                if (mapArray[y][x].cellState == CellState.SHIP) {
                    map.append(CellState.FOG.symbol).append(" ");
                } else {
                    map.append(mapArray[y][x].cellState.symbol).append(" ");
                }
            }
            map.append("\n");
        }

        return map.toString();
    }

    static boolean checkIfUserInputMatchesFormat(String input) {
        String regex = "([A-Z])([0-9]{1,2})";
        return input.matches(regex);
    }

    /**
     * Translates user input
     * coordinate
     * (letter, number) format
     * to a more convenient
     * (int, int) format for
     * internal use. Returns
     * a Cell object, which can be used
     * for ships placement or hits.
     */
    static Cell translateCoordinate(String coordinate) {
        if (!checkIfUserInputMatchesFormat(coordinate)) {
            return new Cell(-1, -1);
        }

        StringBuilder strCellY = new StringBuilder();
        StringBuilder strCellX = new StringBuilder();

        for (char symbol : coordinate.toCharArray()) {
            if (!Character.isDigit(symbol)) {
                strCellY.append(symbol);
            } else {
                strCellX.append(symbol);
            }
        }

        // Unicode capital letters start at 65
        // -1 because game field starts with 1/A, while array with 0/0
        int cellY = ((int) strCellY.toString().toCharArray()[0]) - 65;
        int cellX = Integer.parseInt(strCellX.toString()) - 1;

        return new Cell(cellX, cellY);
    }

    /**
     * Translates user input
     * coordinates
     * (letter, number) format
     * to a more convenient
     * (int, int) format for
     * internal use. Returns
     * an array of two Cell
     * objects, which can be used
     * for ships placement.
     */
    static Cell[] translateInputCoordinates(String inputStr) {
        String[] stringCellsArray = inputStr.split(" ");

        return new Cell[]{
                translateCoordinate(stringCellsArray[0]),
                translateCoordinate(stringCellsArray[1])
        };
    }

    /**
     * During placement, ships can't touch each other.
     * Checks cells adjacent to cell being placed
     * and cell itself (checking crossing over other ships),
     * returns false if cell or any adjacent cells
     * (excluding diagonals) are Cell.SHIP, otherwise
     * returns true.
     */
    public boolean checkIfCellHasNoAdjacentShips(Cell cell) {
        // Cell itself can't be a ship
        if (cell.cellState == CellState.SHIP) return false;

        // Check cell to the:
        // Left
        if (cell.x != 0) {
            if (mapArray[cell.y][cell.x - 1].cellState == CellState.SHIP) return false;
        }
        // Right
        if (cell.x != MAP_SIZE - 1) {
            if (mapArray[cell.y][cell.x + 1].cellState == CellState.SHIP) return false;
        }
        // Top
        if (cell.y != 0) {
            if (mapArray[cell.y - 1][cell.x].cellState == CellState.SHIP) return false;
        }
        // Bottom
        if (cell.y != MAP_SIZE - 1) {
            if (mapArray[cell.y + 1][cell.x].cellState == CellState.SHIP) return false;
        }

        return true;
    }

    /**
     * Checks if all cells in a ship being placed
     * do not have any adjacency with other ships
     * and do not cross other ships.
     * Returns false if any adjacency or crossing
     * is found, otherwise true.
     */
    public boolean checkIfShipHasNoAdjacentShips(Cell cellStart, Cell cellEnd) {
        // Check if ship placed vert. or horizontally
        // <= and => in for() because both cells are included
        if (cellStart.y != cellEnd.y) {
            // Check if ship's beginning cell is higher than ending cell
            if (cellStart.y < cellEnd.y) {
                for (int y = cellStart.y; y <= cellEnd.y; y++) {
                    if (!checkIfCellHasNoAdjacentShips(mapArray[y][cellStart.x])) return false;
                }
            } else {
                for (int y = cellStart.y; y >= cellEnd.y; y--) {
                    if (!checkIfCellHasNoAdjacentShips(mapArray[y][cellStart.x])) return false;
                }
            }
        } else {
            if (cellStart.x < cellEnd.x) {
                for (int x = cellStart.x; x <= cellEnd.x; x++) {
                    if (!checkIfCellHasNoAdjacentShips(mapArray[cellStart.y][x])) return false;
                }
            } else {
                for (int x = cellStart.x; x >= cellEnd.x; x--) {
                    if (!checkIfCellHasNoAdjacentShips(mapArray[cellStart.y][x])) return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks if input ship's size fits it's class.
     */
    public static boolean checkShipSize(Cell cellFirst, Cell cellSecond, Ship ship) {
        // Check if ship placed vert. or horizontally
        // Plus 1 because both (start/end) cells are included
        if (cellFirst.y != cellSecond.y) {
            return Math.abs(cellFirst.y - cellSecond.y) + 1 == ship.health;
        } else {
            return Math.abs(cellFirst.x - cellSecond.x) + 1 == ship.health;
        }
    }

    /**
     * Ship cannot have a diagonal form.
     * Check if value of at least one dimension
     * is same.
     * Returns true if placement is correct,
     * false otherwise.
     */
    public static boolean checkShipNotDiagonal(Cell cellFirst, Cell cellSecond) {
        if ((cellFirst.y != cellSecond.y) && (cellFirst.x != cellSecond.x)) return false;
        return true;
    }

    /**
     * Checks if cell is
     * in bounds of game field.
     */
    public boolean checkIfCoordinatesAreInBounds(Cell cell) {
        if ((cell.x < 0 || cell.x > MAP_SIZE - 1) || (cell.y < 0 || cell.y > MAP_SIZE - 1)) {
            return false;
        }

        return true;
    }

    /**
     * Checks if ship placement
     * follows the rules of the game
     * and places it using
     * coordinates of first and
     * second cells, and ship class.
     */
    public boolean placeShip(Cell cellStart, Cell cellEnd, Ship ship) {

        if (!checkShipNotDiagonal(cellStart, cellEnd)) return false;
        if (!checkShipSize(cellStart, cellEnd, ship)) return false;
        if (!checkIfShipHasNoAdjacentShips(cellStart, cellEnd)) return false;

        // Check if ship placed vert. or horizontally
        // <= and => in for() because both cells are included
        if (cellStart.y != cellEnd.y) {
            // Check if ship's beginning cell is higher than ending cell
            if (cellStart.y < cellEnd.y) {
                for (int y = cellStart.y; y <= cellEnd.y; y++) {
                    mapArray[y][cellStart.x].cellState = CellState.SHIP;
                    ship.addCell(mapArray[y][cellStart.x]);
                }
            } else {
                for (int y = cellStart.y; y >= cellEnd.y; y--) {
                    mapArray[y][cellStart.x].cellState = CellState.SHIP;
                    ship.addCell(mapArray[y][cellStart.x]);
                }
            }
        } else {
            if (cellStart.x < cellEnd.x) {
                for (int x = cellStart.x; x <= cellEnd.x; x++) {
                    mapArray[cellStart.y][x].cellState = CellState.SHIP;
                    ship.addCell(mapArray[cellStart.y][x]);
                }
            } else {
                for (int x = cellStart.x; x >= cellEnd.x; x--) {
                    mapArray[cellStart.y][x].cellState = CellState.SHIP;
                    ship.addCell(mapArray[cellStart.y][x]);
                }
            }
        }

        return true;
    }

    /**
     * Starts the game by
     * letting player place his ships.
     */
    public void startGameShipPlacement() {
        Scanner scanner = new Scanner(System.in);
        String userInput;
        Cell[] userCells;

        System.out.println(this);

        Ship aircraftCarrier = new Ship("Aircraft Carrier", 5);
        Ship battleship = new Ship("Battleship", 4);
        Ship submarine = new Ship("Submarine", 3);
        Ship cruiser = new Ship("Cruiser", 3);
        Ship destroyer = new Ship("Destroyer", 2);

        // Aircraft carrier placement
        System.out.println("Enter the coordinates of the Aircraft Carrier (5 cells):");
        while (true) {
            userInput = scanner.nextLine();
            userCells = translateInputCoordinates(userInput);

            if (placeShip(userCells[0], userCells[1], aircraftCarrier)) {
                System.out.println(this);
                break;
            } else {
                System.out.println("Error! Incorrect input. Try again:");
            }
        }
        this.shipsList.add(aircraftCarrier);

        // Battleship
        System.out.println("Enter the coordinates of the Battleship (4 cells):");
        while (true) {
            userInput = scanner.nextLine();
            userCells = translateInputCoordinates(userInput);

            if (placeShip(userCells[0], userCells[1], battleship)) {
                System.out.println(this);
                break;
            } else {
                System.out.println("Error! Incorrect input. Try again:");
            }
        }
        this.shipsList.add(battleship);

        // Submarine
        System.out.println("Enter the coordinates of the Submarine (3 cells):");
        while (true) {
            userInput = scanner.nextLine();
            userCells = translateInputCoordinates(userInput);

            if (placeShip(userCells[0], userCells[1], submarine)) {
                System.out.println(this);
                break;
            } else {
                System.out.println("Error! Incorrect input. Try again:");
            }
        }
        this.shipsList.add(submarine);

        // Cruiser
        System.out.println("Enter the coordinates of the Cruiser (3 cells):");
        while (true) {
            userInput = scanner.nextLine();
            userCells = translateInputCoordinates(userInput);

            if (placeShip(userCells[0], userCells[1], cruiser)) {
                System.out.println(this);
                break;
            } else {
                System.out.println("Error! Incorrect input. Try again:");
            }
        }
        this.shipsList.add(cruiser);

        // Destroyer
        System.out.println("Enter the coordinates of the Destroyer (2 cells):");
        while (true) {
            userInput = scanner.nextLine();
            userCells = translateInputCoordinates(userInput);

            if (placeShip(userCells[0], userCells[1], destroyer)) {
                System.out.println(this);
                break;
            } else {
                System.out.println("Error! Incorrect input. Try again:");
            }
        }
        this.shipsList.add(destroyer);

    }

    /**
     * Returns true if hit
     * was valid.
     */
    public boolean shoot(Cell cell) {
        if (!checkIfCoordinatesAreInBounds(cell)) return false;

        // Shots at already hit/missed cells are not considered different
        if (mapArray[cell.y][cell.x].cellState == CellState.SHIP
                || mapArray[cell.y][cell.x].cellState == CellState.HIT) {
            if (mapArray[cell.y][cell.x].cellState == CellState.SHIP) {
                mapArray[cell.y][cell.x].cellState = CellState.HIT;
                System.out.println("You hit a ship!");
            } else {
                System.out.println("You hit a ship!");
            }

        } else {
            mapArray[cell.y][cell.x].cellState = CellState.MISS;
            System.out.println("You missed!");
        }
        return true;
    }

}

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        BattleMap playerOneMap = new BattleMap();

        System.out.println("Player 1, place your ships on the game field");
        playerOneMap.startGameShipPlacement();
        System.out.println("Press Enter and pass the move to another player");
        scanner.nextLine();


        BattleMap playerTwoMap = new BattleMap();

        System.out.println("Player 2, place your ships on the game field");
        playerTwoMap.startGameShipPlacement();
        System.out.println("Press Enter and pass the move to another player");
        scanner.nextLine();

        String userInput;

        while (true) {
            // Player One turn

            System.out.println(playerTwoMap.toStringHidden());
            System.out.println("---------------------");
            System.out.println(playerOneMap);
            System.out.println("Player 1, it's your turn:");

            while (true) {
                userInput = scanner.nextLine();
                if (playerTwoMap.shoot(BattleMap.translateCoordinate(userInput))) {
                    break;
                } else {
                    System.out.println("Error! You entered the wrong coordinates! Try again:");
                }
            }

            // Win condition
            ArrayList<Ship> checkShipList = new ArrayList<>(playerTwoMap.shipsList);
            for (int i = 0; i < checkShipList.size(); i++) {
                if (!checkShipList.get(i).checkHealth()) {
                    System.out.println("You sank a ship!");
                    playerTwoMap.shipsList.remove(i);
                    break;
                }
            }
            if (playerTwoMap.shipsList.isEmpty()) break;

            System.out.println("Press Enter and pass the move to another player");
            scanner.nextLine();

            // Player Two turn

            System.out.println(playerOneMap.toStringHidden());
            System.out.println("---------------------");
            System.out.println(playerTwoMap);
            System.out.println("Player 1, it's your turn:");

            while (true) {
                userInput = scanner.nextLine();
                if (playerOneMap.shoot(BattleMap.translateCoordinate(userInput))) {
                    break;
                } else {
                    System.out.println("Error! You entered the wrong coordinates! Try again:");
                }
            }

            // Win condition
            checkShipList = new ArrayList<>(playerOneMap.shipsList);
            for (int i = 0; i < checkShipList.size(); i++) {
                if (!checkShipList.get(i).checkHealth()) {
                    System.out.println("You sank a ship!");
                    playerOneMap.shipsList.remove(i);
                    break;
                }
            }
            if (playerOneMap.shipsList.isEmpty()) break;

            System.out.println("Press Enter and pass the move to another player");
            scanner.nextLine();
        }

        System.out.println("You sank the last ship. You won. Congratulations!");
    }
}