package castle.comp3021.assignment.protocol.io;

import castle.comp3021.assignment.player.ConsolePlayer;
import castle.comp3021.assignment.protocol.*;
import castle.comp3021.assignment.protocol.exception.InvalidConfigurationError;
import castle.comp3021.assignment.protocol.exception.InvalidGameException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

public class Deserializer {
    @NotNull
    private Path path;

    private Configuration configuration;

    private Integer[] storedScores;

    Place centralPlace;

    private ArrayList<MoveRecord> moveRecords = new ArrayList<>();



    public Deserializer(@NotNull final Path path) throws FileNotFoundException {
        if (!path.toFile().exists()) {
            throw new FileNotFoundException("Cannot find file to load!");
        }

        this.path = path;
    }


    /**
     * Returns the first non-empty and non-comment line from the reader.
     *
     * @param br {@link BufferedReader} to read from.
     * @return First line that is a parsable line, or {@code null} there are no lines to read.
     * @throws IOException if the reader fails to read a line.
     */
    @Nullable
    private String getFirstNonEmptyLine(@NotNull final BufferedReader br) throws IOException {
        do {

            String s = br.readLine();

            if (s == null) {
                return null;
            }
            if (s.isBlank() || s.startsWith("#")) {
                continue;
            }

            return s;
        } while (true);
    }


    public void parseGame() {
        try (var reader = new BufferedReader(new FileReader(path.toFile()))) {
            String line;

            int size;
            if ((line = getFirstNonEmptyLine(reader)) != null) {
                try{
                    size = Integer.parseInt(line.split(":")[1].strip());
                } catch (Exception e){
                    throw new InvalidGameException("Fail to parse board size. Please check format!");
                }
            } else {
                throw new InvalidGameException("Unexpected EOF when parsing number of board size");
            }

            int numMovesProtection;
            if ((line = getFirstNonEmptyLine(reader)) != null) {
                try {
                    numMovesProtection = Integer.parseInt(line.split(":")[1].strip());
                } catch (Exception e){
                    throw new InvalidGameException("Fail to parse numMovesProtection. Please check format!");
                }
            } else {
                throw new InvalidGameException("Unexpected EOF when parsing number of columns");
            }

            if ((line = getFirstNonEmptyLine(reader)) != null) {
                try {
                    centralPlace = parsePlace(line.split(":")[1].strip());
                } catch (Exception e){
                    throw new InvalidGameException("Unexpected EOF when parsing central place." + e.getMessage());
                }
            }


            int numPlayers;
            if ((line = getFirstNonEmptyLine(reader)) != null) {
                try {
                    numPlayers = Integer.parseInt(line.split(":")[1].strip());
                } catch (Exception e){
                    throw new InvalidGameException("Failed to parse number of player. Please check format!");
                }
//                System.out.println("numPlayers = " + numPlayers);
            } else {
                throw new InvalidGameException("Unexpected EOF when parsing number of players");
            }

            Player[] players = new Player[numPlayers];
            storedScores = new Integer[numPlayers];

            for (int i = 0; i < numPlayers; i++){
                String playerName;
                int score;

                if ((line = getFirstNonEmptyLine(reader)) != null) {
                    try {
                        playerName = line.split(";")[0].split(":")[1].strip();
                    } catch (Exception e){
                        throw new InvalidGameException("Failed to parse player's name. Please check format!");
                    }
                    players[i] = new ConsolePlayer(playerName);

                    try {
                        score = Integer.parseInt(line.split("; ")[1].split(":")[1].strip());
                    } catch (Exception e){
                        throw new InvalidGameException("Parse score failed. Please check format!");
                    }

                    storedScores[i] = score;

                } else {
                    throw new InvalidGameException("Unexpected EOF when parsing information of players");
                }
            }

            try {
                configuration = new Configuration(size, players, numMovesProtection);
            } catch (InvalidConfigurationError e){
                throw e;
            }

            while(true) {
                if ((line = getFirstNonEmptyLine(reader)) != null){
                    if (line.startsWith("END")) {
                        break;
                    }
                    else{
                        try {
                            moveRecords.add(parseMoveRecord(line.strip()));
                        } catch (Exception e){
                            throw new InvalidGameException("Parse move record failed. Please check format!");
                        }
                    }
                }
            }

        } catch (IOException ioe) {
            throw new InvalidGameException(ioe);
        }
    }

    public Configuration getLoadedConfiguration(){
        return configuration;
    }

    public Integer[] getStoredScores(){
        return storedScores;
    }

    public Place getCentralPlace(){
        return centralPlace;
    }

    public ArrayList<MoveRecord> getMoveRecords(){
        return moveRecords;
    }

    private MoveRecord parseMoveRecord(String moveRecordString){
        //TODO
        var segments = moveRecordString.split("; ");
        var playerName = segments[0].split(":")[1].strip();
//        System.out.println("playerName:" + playerName);

        Player player = new ConsolePlayer(playerName);

        try {
            var move = parseMove(segments[1]);
            return new MoveRecord(player, move);
        } catch (InvalidConfigurationError e){
            throw e;
        }
    }

    private Place parsePlace(String inputString) {
        inputString = inputString.replace("(","").replace(")","");
        var segments = inputString.split(",");
        if (segments.length < 2) {
            return null;
        }
        try {
            var x = Integer.parseInt(segments[0].strip());
            var y = Integer.parseInt(segments[1].strip());
            return new Place(x, y);
        } catch (NumberFormatException e) {
            throw e;
        }
    }

    private Move parseMove(String str) {
//        System.out.println("parse move" + str);
        str = str.split(":")[1];
        var segments = str.split("->");
        if (segments.length < 2) {
            throw  new InvalidConfigurationError("One move should contain both source and target!");
        }
        var source = parsePlace(segments[0].strip());
        if (source == null) {
            throw new InvalidConfigurationError("Source place is empty!");
        }
        var destination = parsePlace(segments[1].strip());
        if (destination == null) {
            throw new InvalidConfigurationError("Target place is empty!");
        }
        return new Move(source, destination);
    }
}
