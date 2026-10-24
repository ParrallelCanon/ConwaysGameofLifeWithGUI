package org.example.pleaseworkconway;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Random;

public class ConwaysFX extends Application {
    /**
     * Creator: Bogdan Ursu
     * Aims: create a conways game of life, with a 10^2 board.
     * progress:
     * 16/8/26 - got the thing to start, started coding the main project
     * 17/8/26 - fixed all the logic issues in game, as well as making it functional, with GUI (autostep, step, randomise board, speed slider, pick which squares can be changed, endgame)
     * 18/7/26 - added finishing touches to make it easier to understand (more labels (lblFill and lblExplain))
     * 21/8/26 - added the feature where when you toggle a cell directly it stops the timeline
     * 24/10/26 - added the inversley proportional button size feature, and started to perfect it
     *  also added the section to make it not go over the screen, no matter the resolution
     *
     * What did I learn?
     *      Basic functions of JavaFX
     *          Buttons, labels, sliders, grid, timeline
     *      How to optimise conways game of life from my previous attempt (see the non-GUI version, as well as the GUI version)
     *      How to make a JavaFX Project
     */

    //creating the way to make the things
    public static Random rand = new Random();

    public static double dScreenHeight;
    public static double dScreenWidth;

    public static int iGap = 1;

    public static int iSize = 100;
    public static int iButtonSize;


    public static int iPadding = 10;

    //size and button size are inversly proportional, so s = k/b, where s = 26 & b = 40
    //so 26*40 = k = 1040
    //but not entirely screen size, so use Screen.getPrimary().getVisualBounds.getHeight()

    public static int[][] iBoard;
    public static Button[][] btnCells;

    public static Label lblGens;
    public static Label lblPopul;

    private static Slider sldSpeed;
    private static Label lblSpeed;

    private static Button btnAuto;
    private static Timeline timeline;

    public static int iGens = 0;
    public static int iPopul = 0;
    public static boolean bEnd = false;
    public static boolean bAutoRunning = false;

    //from previous version
    public static boolean neighbourToBool(int iNCount, boolean bAlive) {
        boolean bBool;

        if (bAlive) {
            bBool = (iNCount == 2 || iNCount == 3);
        } else {
            bBool = (iNCount == 3);
        }

        return (bBool);
    }

    public static void checkPopul() {
        int i;
        int j;

        iPopul = 0;

        for (i = 0; i < iSize; i++) {
            for (j = 0; j < iSize; j++) {
                if (iBoard[i][j] == 1) {
                    iPopul++;
                }
            }
        }
    }

    //start to do the GUI

    //builds the stage / scene to put everything on
    @Override
    public void start(Stage stage) {
        dScreenHeight = Screen.getPrimary().getVisualBounds().getHeight();
        dScreenWidth = Screen.getPrimary().getVisualBounds().getWidth();

        GridPane grid = buildGrid();
        VBox sidePanel = buildSidePanel();

        BorderPane root = new BorderPane();
        root.setCenter(grid);
        root.setRight(sidePanel);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Conway's Game of Life");
        stage.show();
        updateLabels();
    }

    //create the grid
    private GridPane buildGrid() {
        iSize = Math.min(100, iSize);

        int iButtonHeight = (int) ((dScreenHeight - 100 - (iSize - 1) * iGap - 2 * iPadding) / iSize);
        int iButtonWidth = (int) ((dScreenWidth - 175 - 20 - (iSize - 1) * iGap - 2 * iPadding) / iSize);
        iButtonSize = Math.min(iButtonHeight, iButtonWidth);
        iButtonSize = Math.max(iGap * 4, iButtonSize);

        System.out.println("Cell size is " + iButtonSize + ", and the cell count is " + (int) Math.pow(iSize, 2));

        iBoard = new int[iSize][iSize];
        btnCells = new Button[iSize][iSize];

        int i;
        int j;

        GridPane grid = new GridPane();

        //sets the iSizes and stuff of the grid
        grid.setHgap(iGap);
        grid.setVgap(iGap);
        grid.setPadding(new Insets(iPadding));

        for (i = 0; i < iSize; i++) {
            for (j = 0; j < iSize; j++) {
                //makes togglable buttons for every single cell
                Button cell = new Button();
                cell.setMinSize(iButtonSize, iButtonSize);
                cell.setPrefSize(iButtonSize, iButtonSize);
                cell.setMaxSize(iButtonSize, iButtonSize);
                cell.setStyle(cellStyle(false));

                final int fI = i;
                final int fJ = j;
                cell.setOnAction(e -> toggleCell(fI, fJ));

                btnCells[i][j] = cell;
                grid.add(cell, j, i); // j and i are reversed here
            }
        }
        return (grid);
    }

        //changes the btnCells state from one to the other, including visually
        public static void toggleCell(int i, int j) {
        if (bEnd)
        {
            checkPopul();
            return; // stops from changing if the thing is true
        }
            if(bAutoRunning)
            {
                timeline.stop();
                bAutoRunning = false;
            }
        iBoard[i][j] = (iBoard[i][j] == 1) ? 0 : 1;
        btnCells[i][j].setStyle(cellStyle(iBoard[i][j] == 1));
        checkPopul();
        }

        //shows how to change the colours of the btnCells
        public static String cellStyle(boolean alive) {
            return alive
            ? "-fx-background-color: black;"
            : "-fx-background-color: white; -fx-border-color: gray;";
        }

    //side panel with all the things inside
    VBox buildSidePanel() {

        //explaining label
        Label lblExplain = new Label("Press any cell to toggle it");
        Label lblFill = new Label("");

        //labels
        lblGens = new Label("Generation: " + iGens);
        lblPopul = new Label("Population: " + iPopul);

        //buttons
        Button btnStep = new Button("Step");
        btnStep.setOnAction(e -> step());

        btnAuto = new Button("Toggle Auto");
        btnAuto.setOnAction(e -> toggleAuto());

        Button btnEnd = new Button("End Game");
        btnEnd.setOnAction(e -> endGame());

        Button btnReset = new Button("Reset");
        btnReset.setOnAction(e -> reset());

        Button btnRand = new Button("Randomise Board");
        btnRand.setOnAction(e -> randBoard());

        //slider
        sldSpeed = new Slider(50, 1000, 300); // min then max, then initial delay between thing in ms
        sldSpeed.setShowTickLabels(true);
        sldSpeed.setShowTickMarks(true);

        lblSpeed = new Label("Auto Speed: 300ms");
        sldSpeed.valueProperty().addListener((obs, oldVal, newVal) -> {
            lblSpeed.setText("Auto Speed: " + newVal.intValue() + "ms");
            if (bAutoRunning)
            {
                timeline.stop();
                startTimeline(newVal.intValue());
            }
        });

        //where all the items go
        VBox box = new VBox(12, lblExplain, lblFill, lblGens, lblPopul, btnStep, btnAuto, btnReset, btnEnd, btnRand, lblSpeed, sldSpeed );
        box.setPadding(new Insets(10));
        box.setAlignment(Pos.TOP_CENTER);
        box.setPrefWidth(175);
        return(box);
    }

    public static void step() {
        if (bEnd) return;

        int[][] next = new int[iSize][iSize];

        for (int i = 0; i < iSize; i++)
        {
            for (int j = 0; j < iSize; j++)
            {
                int neighbours = countNeighbours(i, j);
                boolean alive = iBoard[i][j] == 1;
                next[i][j] = neighbourToBool(neighbours, alive) ? 1 : 0;
            }
        }

        for (int i = 0; i < iSize; i++)
        {
            for (int j = 0; j < iSize; j++)
            {
                iBoard[i][j] = next[i][j];
                btnCells[i][j].setStyle(cellStyle(iBoard[i][j] == 1));
            }
        }

        iGens++;
        checkPopul();
        updateLabels();
    }

        private static int countNeighbours(int i, int j) {
            int count = 0;
            for (int di = -1; di <= 1; di++)
            {
                for (int dj = -1; dj <= 1; dj++)
                {
                    if (di == 0 && dj == 0) continue;

                    int ni = i + di;
                    int nj = j + dj;

                    if (ni >= 0 && ni < iSize && nj >= 0 && nj < iSize)
                    {
                        if (iBoard[ni][nj] == 1) count++;
                    }
                }
            }
            return count;
        }

    public static void toggleAuto() {
        if (bAutoRunning)
        {
            timeline.stop();
            bAutoRunning = false;
        }
        else
        {
            startTimeline((int) sldSpeed.getValue());
            bAutoRunning = true;
        }
    }

    public static void endGame() {
        bEnd = true;
        if (timeline != null)
        {
            timeline.stop();
        }
        bAutoRunning = false;
        btnAuto.setText("Toggle Auto");
        btnAuto.setDisable(true);

        for (int i = 0; i < iSize; i++)
            for (int j = 0; j < iSize; j++)
                btnCells[i][j].setDisable(true);

        lblGens.setText(lblGens.getText() + " (ended)");
    }

    public static void reset() {
        if (timeline != null)
        {
            timeline.stop();
        }
        bAutoRunning = false;
        bEnd = false;
        btnAuto.setText("Toggle Auto");
        btnAuto.setDisable(false);

        for (int i = 0; i < iSize; i++)
        {
            for (int j = 0; j < iSize; j++)
            {
                iBoard[i][j] = 0;
                btnCells[i][j].setDisable(false);
                btnCells[i][j].setStyle(cellStyle(false));
            }
        }
        iGens = 0;
        iPopul = 0;
        updateLabels();
    }

    public static void randBoard() {
        if (bEnd) return;

        for (int i = 0; i < iSize; i++)
        {
            for (int j = 0; j < iSize; j++)
            {
                iBoard[i][j] = rand.nextInt(2); // 0 or 1
                btnCells[i][j].setStyle(cellStyle(iBoard[i][j] == 1));
            }
        }

        iGens = 0;
        checkPopul();
        updateLabels();
    }

    public static void updateLabels() {
        lblGens.setText("Generation: "+ iGens);
        lblPopul.setText("Population: " + iPopul);
    }

    private static void startTimeline(int millis) {
        timeline = new Timeline(new KeyFrame(Duration.millis(millis), e -> step()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    public static void main(String[] args)
    {
        int i;
        int j;

        System.out.println("Starting the game!");

        launch(args);

        System.out.println("Thank you for playing!");

    }
}
