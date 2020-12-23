package castle.comp3021.assignment.protocol.io;

import castle.comp3021.assignment.gui.FXJesonMor;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;

public class Serializer {
    @NotNull
    private static final Serializer INSTANCE = new Serializer();

    /**
     * @return Singleton instance of this class.
     */
    @NotNull
    public static Serializer getInstance() {
        return INSTANCE;
    }



    public void saveToFile(FXJesonMor fxJesonMor) {
        final var targetDir = getTargetSaveDirectory();
        String outputString = fxJesonMor.toString();
        if (targetDir != null) {
            exportToFile(targetDir.toPath(), outputString);
        }
    }

    @Nullable
    private File getTargetSaveDirectory() {
        final var chooser = new FileChooser();
        chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Move Record File", "*.map"));
        return chooser.showSaveDialog(null);
    }

    public void exportToFile(@NotNull Path path, String outputString){
        final var file = path.toFile();
        if (file.exists()) {
            file.delete();
        }

        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (final var wr = new BufferedWriter(new PrintWriter(file))) {
            wr.write(outputString);
        } catch (final IOException e) {
            final var box = new Alert(Alert.AlertType.ERROR);
            box.setTitle("Error");
            box.setHeaderText("Cannot save move record to file!");
            box.setContentText(e.getMessage());
            box.showAndWait();
        }
    }

}
