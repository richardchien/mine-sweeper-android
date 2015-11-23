package com.richardchien.minesweeper;

import java.util.*;

public class MineSweeperGame {
    /*
     * Row from 0 to rowN - 1
     * Column from 0 to colN - 1
     * Condition to win: All square without bomb are digged
     */

    public enum GameState {Waiting, Playing, Win, Lose}

    private GameState state;

    public GameState getState() {
        return state;
    }

    private class Point {
        private int row;
        private int col;

        public int getCol() {
            return col;
        }

        public int getRow() {
            return row;
        }


        public Point(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Point point = (Point) o;
            return row == point.row && col == point.col;
        }

        @Override
        public int hashCode() {
            int result = row;
            result = 31 * result + col;
            return result;
        }
    }

    /*
     * Use to check over the squares around
     */
    private Point[] next;

    private int[][] map;

    public int[][] getMapToDisplay() {
        int[][] mapToDisplay = new int[rowN][colN];
        for (int i = 0; i < rowN; i++) {
            for (int j = 0; j < colN; j++) {
                Point p = new Point(i, j);
                if (!shownSet.contains(p)) {
                    mapToDisplay[i][j] = kMapUnshown;
                } else {
                    mapToDisplay[i][j] = map[i][j];
                }
                if (flagSet.contains(p)) {
                    mapToDisplay[i][j] = kMapFlaged;
                }
            }
        }
        return mapToDisplay;
    }

    public static final int kMapBomb = -1;
    public static final int kMapUnshown = -2;
    public static final int kMapFlaged = -3;

    private int rowN, colN, bombN;
    private Set<Point> shownSet;
    private Set<Point> bombSet;
    private Set<Point> flagSet;

    /*
     * Initialize the game
     */
    public MineSweeperGame(int rowN, int colN, int bombN) {
        next = new Point[8];
        next[0] = new Point(0, 1);
        next[1] = new Point(1, 1);
        next[2] = new Point(1, 0);
        next[3] = new Point(1, -1);
        next[4] = new Point(0, -1);
        next[5] = new Point(-1, -1);
        next[6] = new Point(-1, 0);
        next[7] = new Point(-1, 1);

        state = GameState.Waiting;
        this.rowN = rowN;
        this.colN = colN;
        this.bombN = bombN;
        map = new int[rowN][colN];
        shownSet = new HashSet<>();
        bombSet = new HashSet<>();
        flagSet = new HashSet<>();
    }

    /*
     * Prepare for a new game round
     */
    @Deprecated
    public void startGame() {
        shownSet.clear();
        bombSet.clear();
        flagSet.clear();

        for (int i = 0; i < rowN; i++) {
            for (int j = 0; j < colN; j++) {
                map[i][j] = 0;
            }
        }

        Random random = new Random();

        int i = 0;
        while (i < bombN) {
            int row = random.nextInt(rowN);
            int col = random.nextInt(colN);
            if (map[row][col] != kMapBomb) {
                map[row][col] = kMapBomb;
                Point newBomb = new Point(row, col);
                bombSet.add(newBomb);

                /*
                 * Increase the count of squares around the bomb
                 */
                int r, c;
                for (Point offset : next) {
                    r = row + offset.getRow();
                    c = col + offset.getCol();
                    if (pointIsValid(new Point(r, c)) && map[r][c] != kMapBomb) {
                        map[r][c]++;
                    }
                }
            }
            i = bombSet.size();
        }

        state = GameState.Waiting;
    }

    public void prepareGame() {
        shownSet.clear();
        bombSet.clear();
        flagSet.clear();

        for (int i = 0; i < rowN; i++) {
            for (int j = 0; j < colN; j++) {
                map[i][j] = 0;
            }
        }
    }

    private void startGameAt(int row, int col) {
        Random random = new Random();

        int i = 0;
        while (i < bombN) {
            int newRow = random.nextInt(rowN);
            int newCol = random.nextInt(colN);
            if (map[newRow][newCol] != kMapBomb && !(newRow == row && newCol == col)) {
                map[newRow][newCol] = kMapBomb;
                Point newBomb = new Point(newRow, newCol);
                bombSet.add(newBomb);

                /*
                 * Increase the count of squares around the bomb
                 */
                int r, c;
                for (Point offset : next) {
                    r = newRow + offset.getRow();
                    c = newCol + offset.getCol();
                    if (pointIsValid(new Point(r, c)) && map[r][c] != kMapBomb) {
                        map[r][c]++;
                    }
                }
            }
            i = bombSet.size();
        }

        state = GameState.Playing;
    }

    /*
     * Game state may change after digging
     */
    public boolean digAt(int row, int col) {
        Point digPoint = new Point(row, col);

        if (!pointIsValid(digPoint)) {
            return false;
        }

        if (state == GameState.Waiting) {
            /*
             * Begin to play
             */
            startGameAt(row, col);
        }

        if (shownSet.contains(digPoint) || flagSet.contains(digPoint) || state != GameState.Playing) {
            /*
             * The point was digged or game is not playing
             */
            return false;
        }

        if (bombSet.contains(digPoint)) {
            state = GameState.Lose;
            for (int i = 0; i < rowN; i++) {
                for (int j = 0; j < colN; j++) {
                    Point p = new Point(i, j);
                    shownSet.add(p);
                }
            }
            flagSet.clear();
            return true;
        }

        shownSet.add(digPoint);
        if (map[row][col] == 0) {
            digExpand(digPoint);
        }

        /*
         * Check win or not
         */
        int sizeOfUnion, sizeOfIntersection;
        Set<Point> checkSet = new HashSet<>();
        checkSet.addAll(shownSet);
        checkSet.retainAll(bombSet);
        sizeOfIntersection = checkSet.size();
        checkSet.clear();
        checkSet.addAll(shownSet);
        checkSet.addAll(bombSet);
        sizeOfUnion = checkSet.size();
        if (sizeOfIntersection == 0 && sizeOfUnion == rowN * colN) {
            state = GameState.Win;
            for (Point p : bombSet) {
                flagSet.add(p);
            }
        }

        return true;
    }

    public boolean isFlagged(int row, int col) {
        Point flagPoint = new Point(row, col);
        return flagSet.contains(flagPoint);
    }

    public boolean flagAt(int row, int col) {
        Point flagPoint = new Point(row, col);

        if (!pointIsValid(flagPoint)) {
            return false;
        }

        if (state == GameState.Waiting) {
            /*
             * Begin to play
             */
            startGameAt(row, col);
        }

        if (!shownSet.contains(flagPoint)) {
            flagSet.add(flagPoint);
        }

        return true;
    }

    public boolean unflagAt(int row, int col) {
        Point flagPoint = new Point(row, col);

        if (!pointIsValid(flagPoint)) {
            return false;
        }

        for (Point p : flagSet) {
            if (p.equals(flagPoint)) {
                flagSet.remove(p);
                break;
            }
        }

        return true;
    }

    private void digExpand(Point p) {
        /*
         * Use breadth first search to find all blank square around (row, col)
         */

        Set<Point> checkedSet = new HashSet<>();
        Queue<Point> queue = new LinkedList<>();
        queue.offer(p);
        while (queue.size() > 0) {
            Point currPoint = queue.poll();
            shownSet.add(currPoint);
            int row = currPoint.getRow();
            int col = currPoint.getCol();

            if (map[row][col] != 0) {
                continue;
            }

            int r, c;
            for (Point offset : next) {
                r = row + offset.getRow();
                c = col + offset.getCol();
                Point newPoint = new Point(r, c);
                if (pointIsValid(newPoint) && !checkedSet.contains(newPoint) && !bombSet.contains(newPoint)) {
                    queue.offer(newPoint);
                }
            }

            checkedSet.add(currPoint);
        }
    }

    private boolean pointIsValid(Point p) {
        int r = p.getRow();
        int c = p.getCol();
        return r < this.rowN && r >= 0 && c < this.colN && c >= 0;
    }
}
