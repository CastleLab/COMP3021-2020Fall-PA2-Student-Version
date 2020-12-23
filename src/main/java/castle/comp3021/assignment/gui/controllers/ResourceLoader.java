package castle.comp3021.assignment.gui.controllers;

import castle.comp3021.assignment.protocol.exception.ResourceNotFoundException;
import javafx.scene.image.Image;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Helper class for loading resources from the filesystem.
 */
public class ResourceLoader {


    /**
     * Path to the resources directory.
     */
    @NotNull
    private static final Path RES_PATH;

    static {
        final Path pwd = Paths.get("src", "main", "resources");
        if (!pwd.toFile().exists()) {
            throw new RuntimeException("Cannot find resources directory");
        }
        RES_PATH = pwd.toAbsolutePath();
    }

    /**
     * Retrieves a resource file from the resource directory.
     *
     * @param relativePath Path to the resource file, relative to the root of the resource directory.
     * @return Absolute path to the resource file.
     * @throws ResourceNotFoundException If the file cannot be found under the resource directory.
     */
    @NotNull
    public static String getResource(@NotNull final String relativePath) {
        final var actualPath = RES_PATH.resolve(relativePath);

        if (actualPath.toFile().exists()) {
            return actualPath.toAbsolutePath().toFile().toURI().toString();
        } else {
            throw new ResourceNotFoundException("Cannot find file specified: " + relativePath);
        }
    }

    @NotNull
    public static Image getImage(char typeChar) {
        return switch (typeChar) {
            case 'K' -> new Image(ResourceLoader.getResource("assets/images/whiteK.png"));
            case 'A' -> new Image(ResourceLoader.getResource("assets/images/whiteA.png"));
            case 'k' -> new Image(ResourceLoader.getResource("assets/images/blackK.png"));
            case 'a' -> new Image(ResourceLoader.getResource("assets/images/blackA.png"));
            case 'c' -> new Image(ResourceLoader.getResource("assets/images/center.png"));
            case 'l' -> new Image(ResourceLoader.getResource("assets/images/lightBoard.png"));
            case 'd' -> new Image(ResourceLoader.getResource("assets/images/darkBoard.png"));
            default -> throw new ResourceNotFoundException("Cannot find image type specified: " + typeChar);
        };
    }


}