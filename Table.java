package com.Chessx.gui;


import com.Chessx.engine.board.Board;
import com.Chessx.engine.board.BoardUtils;
import com.Chessx.engine.board.Move;
import com.Chessx.engine.board.Tiles;
import com.Chessx.engine.piece.piece;
import com.Chessx.engine.player.MoveTransistions;
import com.google.common.collect.Lists;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static javax.swing.SwingUtilities.isLeftMouseButton;
import static javax.swing.SwingUtilities.isRightMouseButton;

public class Table {
    private final JFrame gameFrame;
    private final BoardPanel boardPanel;
    private Board chessBoard;
    private BoardDirection boardDirection;


    private static Dimension OUTER_FRAME_DIMENSION = new Dimension(600, 600);
    private static Dimension BOARD_PANEL_DIMENSION = new Dimension(400, 350);
    private static Dimension TITE_PANEL_DIMENSION = new Dimension(10, 10);
    private static String defaultPieceImagePath = "Art";

private boolean highLightLegalMoves;

    private Tiles sourceTile;
    private Tiles destinationTile;
    private piece humanMovedpiece;
    private final Color lightTileColor = Color.MAGENTA;
    private final Color darkTileColor = Color.black;

    public Table ( ) {
        this.gameFrame = new JFrame("JChess");
        this.gameFrame.setLayout(new BorderLayout());
        final JMenuBar tableMenuBar = createFileMenuBar();
        this.gameFrame.setJMenuBar(tableMenuBar);
        this.gameFrame.setSize(OUTER_FRAME_DIMENSION);
        this.boardPanel = new BoardPanel();
        this.chessBoard = Board.createStandardBoard();
        this.boardDirection=BoardDirection.NORMAL;
        this.gameFrame.add(this.boardPanel, BorderLayout.CENTER);
        this.gameFrame.setVisible(true);
        this.highLightLegalMoves=false;
    }

    private JMenuBar createFileMenuBar ( ) {
        final JMenuBar tableMenuBar = new JMenuBar();
        tableMenuBar.add(createFileMenu());
        tableMenuBar.add(createPreferenceMenu());
        return tableMenuBar;

    }

    private JMenu createFileMenu ( ) {
        final JMenu fileMenu = new JMenu("File");
        final JMenuItem openPGN = new JMenuItem("Load pgn file");
        openPGN.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent e) {
                System.out.println("open up that pgn file");
            }
        });
        fileMenu.add(openPGN);
        final JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent e) {
                System.exit(0);
            }
        });
        fileMenu.add(exitMenuItem);
        return fileMenu;
    }

    private JMenu createPreferenceMenu ( ) {
        final JMenu preferenceMenu = new JMenu();
        final JMenuItem flipBoardMenuItem = new JMenuItem("Flip Board");
        flipBoardMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent e) {
                  boardDirection=boardDirection.opposite();
                boardPanel.drawBoard(chessBoard);
            }
        });
        preferenceMenu.add(flipBoardMenuItem);
        preferenceMenu.addSeparator();
        final JCheckBoxMenuItem legalMoveHighLighterCheckBox = new JCheckBoxMenuItem("HighLight legal Moves", false);
        legalMoveHighLighterCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent e) {
                  highLightLegalMoves=legalMoveHighLighterCheckBox.isSelected();
            }
        });

        preferenceMenu.add(legalMoveHighLighterCheckBox);
        return preferenceMenu;

    }

    public enum  BoardDirection{
        NORMAL {
            @Override
            List<TilePanel> traverse (List<TilePanel> boardtiles) {
                return boardtiles;
            }

            @Override
            BoardDirection opposite ( ) {
                return FLIPPED;
            }
        },
        FLIPPED {
            @Override
            List<TilePanel> traverse (List<TilePanel> boardtiles) {
                return Lists.reverse(boardtiles);
            }

            @Override
            BoardDirection opposite ( ) {
                return NORMAL;
            }
        };


        abstract List<TilePanel> traverse (final List<TilePanel> boardtiles);
        abstract BoardDirection opposite();
    }

    private class BoardPanel extends JPanel {
        final List<TilePanel> boardTiles;
        BoardPanel ( ) {
            super(new GridLayout(8, 8));
            this.boardTiles = new ArrayList<>();
            for (int i = 0; i < BoardUtils.NUM_TILES; i++) {
                final TilePanel tilePanel = new TilePanel(this,i);
                this.boardTiles.add((tilePanel));
                add(tilePanel);
            }
            setPreferredSize(BOARD_PANEL_DIMENSION);
            validate();
        }
        public void drawBoard(final Board board){
            removeAll();
            for(final TilePanel boardTile:boardDirection.traverse(boardTiles)){
              boardTile. drawTile(board);
                add(boardTile);
            }
            validate();
            repaint();
        }
    }

    public static class MoveLog{
        private final List<Move> moves;
        MoveLog(){
            this.moves=new ArrayList<>();
        }

        public List<Move> getMoves ( ) {
            return this.moves;
        }
        public void addMove(final Move move){
            this.moves.add(move);

        }
        public int size(){
            return this.moves.size();
        }
        public void clear(){
            this.moves.clear();

        }
        public Move removeMove(int index){
            return this.moves.remove(index);
        }
        public boolean removeMove(final Move move){
            return this.moves.remove(move);
        }
    }

    private class TilePanel extends JPanel {
        private final int tileId;

        TilePanel (final BoardPanel boardPanel, final int tileId) {
            super(new GridLayout());
            this.tileId = tileId;
            setPreferredSize(TITE_PANEL_DIMENSION);
            assignTileColor();
            assignTilePieceIcon(chessBoard);


            addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked (MouseEvent e) {
                    if(isRightMouseButton(e)){
                        sourceTile=null;
                        destinationTile=null;
                        humanMovedpiece=null;
                    }else if(isLeftMouseButton(e)){
                        if(sourceTile==null){
                            sourceTile=chessBoard.getTiles(tileId);
                            humanMovedpiece=sourceTile.getpiece();
                            if(humanMovedpiece==null){
                                sourceTile=null;
                            }
                        }else{
                            destinationTile=chessBoard.getTiles(tileId);
                            final Move move=Move.MoveFactory.createMove(chessBoard,sourceTile.getTilescoordinate()
                                    ,destinationTile.getTilescoordinate());
                            final MoveTransistions transistions= chessBoard.currentPlayer().makeMove(move);
                            if(transistions.getMoveStatus().isDone()){
                                chessBoard=transistions.getTransistionBoard();
                            }
                            sourceTile=null;
                            destinationTile=null;
                            humanMovedpiece=null;

                        }
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run ( ) {
                                boardPanel.drawBoard(chessBoard);
                            }
                        });

                    }
                }

                @Override
                public void mousePressed (MouseEvent e) {

                }

                @Override
                public void mouseReleased (MouseEvent e) {

                }

                @Override
                public void mouseEntered (MouseEvent e) {

                }

                @Override
                public void mouseExited (MouseEvent e) {

                }
            });
            validate();
        }
        public void drawTile(final Board board){
            assignTileColor();
            assignTilePieceIcon(board);
            validate();
            repaint();
        }
        private void assignTilePieceIcon(final Board board){
            this.removeAll();
            if(board.getTiles(this.tileId).istileoccupied()){
                try{
                        final BufferedImage image =
                                ImageIO.read(new File(defaultPieceImagePath+board.
                                        getTiles(this.tileId).getpiece().getpieceallaince().
                                toString().substring(0, 1) + "Art/pieces/plaiin.gif" +
                                        board.getTiles(this.tileId).getpiece().toString() +
                                        ".gif"));
                        add(new JLabel(new ImageIcon(image)));
                        }
                    catch(final IOException e  ){
                    e.printStackTrace();
                }
            }
        }
private void highLightLegalsMoves(final Board board){
            if(true){
                for(final Move move:pieceLegalMoves(board)){
                    try{
                        add(new JLabel(new ImageIcon(ImageIO.read(new File("Art/Misc/green_dot.png")))));
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
}
        private Collection<Move> pieceLegalMoves(final Board board){
            if(humanMovedpiece!=null&&humanMovedpiece.getpieceallaince()==board.currentPlayer().getAllaince()){
                return humanMovedpiece.calculatelegalmoves(board);
            }
            return Collections.emptyList();
        }
        private void assignTileColor ( ) {
            if(BoardUtils.EIGHT_RANK.get(this.tileId)||
                    BoardUtils.SIXTH_RANK.get(this.tileId)||
                    BoardUtils.FOURTH_RANK.get(this.tileId)||
                    BoardUtils.SECOND_RANK.get(this.tileId)){
                setBackground(this.tileId%2==0?lightTileColor:darkTileColor);
            }else if(BoardUtils.SEVENTH_RANK.get(this.tileId)||
                    BoardUtils.FIFTH_RANK.get(this.tileId)||
                    BoardUtils.THIRD_RANK.get(this.tileId)||
                    BoardUtils.FIRST_RANK.get(this.tileId)){
                setBackground(this.tileId%2!=0?lightTileColor:darkTileColor);
            }

        }
    }
}
