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
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Arrays;
import java.util.Random;

public class ConwaysFX extends Application {
    /**
     * Aims: create a conways game of life, with a 10^2 board.
     *
     * progress:
     * 16/8/26 - got the thing to start, started coding the main project
     * 17/8/26 - fixed all the logic issues in game, as well as making it functional, with GUI (autostep, step,
     *  randomise board, speed slider, pick which squares can be changed, endgame)
     * 18/7/26 - added finishing touches to make it easier to understand (more labels (lblFill and lblExplain))
     * 21/8/26 - added the feature where when you toggle a cell directly it stops the timeline
     * 24/10/26 - added the inversley proportional button size feature, and started to perfect it
     *  also added the section to make it not go over the screen, no matter the resolution
     * 27/10/27 - added the starter stage, in order to let people change their simulation settings
     * 28/10/26 - added the WORLD OF COLOURS: changed the ambient colours to a dark mode, might make
     *  it so that you can swap between them, but that might be further scope creep. Furthermore,
     *  as cells age their colours do as well, going from a dark brick-red, to an aquamarine
     *  gradient, should probably add an explanation
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

    public static int iSize = 50;
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

        if (bAlive)
        {
            bBool = (iNCount >= iMinSurvive && iNCount <= iMaxSurvive);
        } else
        {
            bBool = (iNCount >= iMinBirth && iNCount <= iMaxBirth);
        }

        return (bBool);
    }

    public static void checkPopul() {
        int i;
        int j;

        iPopul = 0;

        for (i = 0; i < iSize; i++) {
            for (j = 0; j < iSize; j++) {
                if (iBoard[i][j] > 0)
                {
                    iPopul++;
                }
            }
        }

        updateLabels();
    }

    //start to do the GUI

    //builds the stage / scene to put everything on
    @Override
    public void start(Stage stage) {
        dScreenHeight = Screen.getPrimary().getVisualBounds().getHeight();
        dScreenWidth = Screen.getPrimary().getVisualBounds().getWidth();

        VBox starterPanel = buildStarterPanel();

        BorderPane starterRoot = new BorderPane();
        starterRoot.setCenter(starterPanel);

        Scene starter = new Scene(starterRoot);
        starter.getStylesheets().add(ConwaysFX.class.getResource("darkMode.css").toExternalForm());

        stage.setWidth(512);

        stage.setScene(starter);
        stage.setTitle("Conway's Game of Life");
        stage.show();
    }

    public static Button btnStart;

    public static int iMinSurvive = 2;
    public static int iMaxSurvive = 3;
    public static int iMinBirth = 3;
    public static int iMaxBirth = 3;

    public static VBox buildStarterPanel()
    {
        Label explain = new Label("EPILEPSY WARNING: If you suffer from epileptic seizures or other issues, then do not increase the autospeed too high, for flashing lights.");
        explain.setWrapText(true);
        explain.setMaxWidth(512);

        //size
        Slider sldSize = new Slider(5, 100, 50);
        sldSize.setShowTickLabels(true);
        sldSize.setShowTickMarks(true);

        Label lblSize = new Label("Enter the size of the grid");
        sldSize.valueProperty().addListener((obs, oldVal, newVal) -> {
            lblSize.setText("Grid Size: " + newVal.intValue());
                iSize = (newVal.intValue());
        });

        //min survive

            //explaining what that means
            Label explainTwo = new Label("These are the conditions for the game, with it creating the 'Conways Multiverse' (A name I know from youtuber \"carykh\"), with the minimum and maximum for cells to survive or be born. However, it is limited to connected rulestrings. The default configuration is the default configuration for Conway's Game of Life.");
            explainTwo.setWrapText(true);
            explainTwo.setMaxWidth(512);

        Slider sldMinSurvive = new Slider(0, 8, 2);
        sldMinSurvive.setShowTickLabels(true);
        sldMinSurvive.setShowTickMarks(true);

        Label lblMinSurvive = new Label("Enter the minimum neighbour count to survive");
        sldMinSurvive.valueProperty().addListener((obs, oldVal, newVal) -> {
            lblMinSurvive.setText("Minimum size to survive: " + newVal.intValue());
            iMinSurvive = (newVal.intValue());
        });

        //max survive
        Slider sldMaxSurvive = new Slider(0, 8, 3);
        sldMaxSurvive.setShowTickLabels(true);
        sldMaxSurvive.setShowTickMarks(true);

        Label lblMaxSurvive = new Label("Enter the maximum neighbour count to survive");
        sldMaxSurvive.valueProperty().addListener((obs, oldVal, newVal) -> {
            lblMaxSurvive.setText("Maximum size to survive: " + newVal.intValue());
            iMaxSurvive = (newVal.intValue());
        });

        //min birth
        Slider sldMinBirth = new Slider(0, 8, 3);
        sldMinBirth.setShowTickLabels(true);
        sldMinBirth.setShowTickMarks(true);

        Label lblMinBirth = new Label("Enter the minimum neighbour count to be born");
        sldMinBirth.valueProperty().addListener((obs, oldVal, newVal) -> {
            lblMinBirth.setText("Minimum size to be born: " + newVal.intValue());
            iMinBirth = (newVal.intValue());
        });

        //max birth
        Slider sldMaxBirth = new Slider(0, 8, 3);
        sldMaxBirth.setShowTickLabels(true);
        sldMaxBirth.setShowTickMarks(true);

        Label lblMaxBirth = new Label("Enter the maximum neighbour count to be born");
        sldMaxBirth.valueProperty().addListener((obs, oldVal, newVal) -> {
            lblMaxBirth.setText("Maximum size to be born: " + newVal.intValue());
            iMaxBirth = (newVal.intValue());
        });

        //button to start
        btnStart = new Button("Start the simulation.");
        btnStart.setOnAction(e -> startButton());

        VBox panel = new VBox(8, explain, lblSize, sldSize, explainTwo, lblMinSurvive, sldMinSurvive, lblMaxSurvive, sldMaxSurvive, lblMinBirth, sldMinBirth, lblMaxBirth, sldMaxBirth, btnStart);

        panel.setAlignment(Pos.CENTER);

        panel.setPrefWidth(512);

        return(panel);
    }

        public static void startButton()
        {
            if((iMaxBirth < iMinBirth) || (iMaxSurvive < iMinSurvive))
            {
                return;
            }

            Stage stage = (Stage) btnStart.getScene().getWindow();

            GridPane grid = buildGrid();
            VBox sidePanel = buildSidePanel();

            for (int i = 0; i < iSize; i++)
            {
                Arrays.fill(iBoard[i], -1);
            }

            BorderPane root = new BorderPane();
            root.setCenter(grid);
            root.setRight(sidePanel);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(ConwaysFX.class.getResource("darkMode.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Conway's Game of Life");

            stage.sizeToScene();
            stage.centerOnScreen();

            updateLabels();
        }


    //create the grid
    private static GridPane buildGrid() {
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
                cell.setStyle(cellStyle(-1));

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
        iBoard[i][j] = (iBoard[i][j] > 0) ? 0 : 1;
        btnCells[i][j].setStyle(cellStyle(iBoard[i][j]));
        checkPopul();
        }

        //shows how to change the colours of the btnCells
        public static String cellStyle(int iAliveness) {
            if (iAliveness < 0)
            {
                return "-fx-background-color: rgb(105,105,105);";   // never alive
            }
            else if (iAliveness == 0)
            {
                return "-fx-background-color: rgb(169,169,169);";   // was alive, now dead
            }
            else if (iAliveness >= 128)
            {
                return "-fx-background-color: rgb(127,255,212);";
            }
            else if (iAliveness >= 64)
            {
                return "-fx-background-color: rgb(152,251,152);";
            }
            else if (iAliveness >= 32)
            {
                return "-fx-background-color: rgb(238,232,170);";
            }
            else if (iAliveness >= 16)
            {
                return "-fx-background-color: rgb(255,165,0);";
            }
            else if (iAliveness >= 8)
            {
                return "-fx-background-color: rgb(255,69,0);";
            }
            else if (iAliveness >= 4)
            {
                return "-fx-background-color: rgb(240,128,128);";
            }
            else if (iAliveness >= 2)
            {
                return "-fx-background-color: rgb(233,150,122);";
            }
            else if (iAliveness >= 1)
            {
                return "-fx-background-color: rgb(178,34,34);";
            }
            else
            {
                return "-fx-background-color: black;";   // should never be reached
            }
        }

    //side panel with all the things inside
    static VBox buildSidePanel() {

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
                boolean bAlive = iBoard[i][j] > 0;
                boolean bWillLive = neighbourToBool(neighbours, bAlive);

                if (bWillLive && bAlive)
                {
                    next[i][j] = iBoard[i][j] + 1;   // survivor ages by one
                }
                else if (bWillLive && !bAlive)
                {
                    next[i][j] = 1;                  // newborn starts at 1
                }
                else if (!bWillLive && bAlive)
                {
                    next[i][j] = 0;                  // just died
                }
                else
                {
                    next[i][j] = iBoard[i][j];       // dead before and still dead: keep -1 or 0
                }
            }
        }

        for (int i = 0; i < iSize; i++)
        {
            for (int j = 0; j < iSize; j++)
            {
                iBoard[i][j] = next[i][j];
                btnCells[i][j].setStyle(cellStyle(iBoard[i][j]));
            }
        }

        iGens++;
        checkPopul();
    }

        private static int countNeighbours(int xPos, int yPos) {
            int iCount = 0;

            int i = 0;
            int j = 0;
            int iBoundry = 0;
            int jBoundry = 0;

            for (i = -1; i <= 1; i++)
            {
                for (j = -1; j <= 1; j++)
                {
                    if (i == 0 && j == 0) continue; // skips the thing if it on itself

                    iBoundry = xPos + i;
                    jBoundry = yPos + j;

                    if (iBoundry >= 0 && iBoundry < iSize && jBoundry >= 0 && jBoundry < iSize)
                    {
                        if (iBoard[iBoundry][jBoundry] > 0)
                        {
                            iCount++;
                        }
                    }
                }
            }
            return iCount;
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
                iBoard[i][j] = -1;
                btnCells[i][j].setDisable(false);
                btnCells[i][j].setStyle(cellStyle(-1));
            }
        }
        iGens = 0;
        iPopul = 0;
        updateLabels();
    }

    public static void randBoard() {
        if (bEnd) return;

        if(bAutoRunning)
        {
            bAutoRunning = false;
            timeline.stop();
        }

        for (int i = 0; i < iSize; i++)
        {
            for (int j = 0; j < iSize; j++)
            {
                int iRand = 0;
                do
                {
                    iRand = rand.nextInt(-1, 2);
                }while(iRand == 0);
                iBoard[i][j] = iRand;
                btnCells[i][j].setStyle(cellStyle(iRand));
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
