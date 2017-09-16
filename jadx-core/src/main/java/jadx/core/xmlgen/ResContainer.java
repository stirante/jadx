package jadx.core.xmlgen;

import jadx.core.codegen.CodeWriter;
import jadx.core.utils.exceptions.JadxRuntimeException;
import javafx.scene.image.Image;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ResContainer implements Comparable<ResContainer> {

    private final String name;
    private final List<ResContainer> subFiles;

    @Nullable
    private CodeWriter content;
    @Nullable
    private BufferedImage image;
    @Nullable
    private Image fxImage;

    private ResContainer(String name, List<ResContainer> subFiles) {
        this.name = name;
        this.subFiles = subFiles;
    }

    public static ResContainer singleFile(String name, CodeWriter content) {
        ResContainer resContainer = new ResContainer(name, Collections.emptyList());
        resContainer.content = content;
        return resContainer;
    }

    public static ResContainer singleImageFile(String name, InputStream content) {
        ResContainer resContainer = new ResContainer(name, Collections.emptyList());
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[16384];
            while ((nRead = content.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            content.close();
            ByteArrayInputStream is = new ByteArrayInputStream(buffer.toByteArray());
            resContainer.image = ImageIO.read(is);
            is.reset();
            resContainer.fxImage = new Image(is);
            buffer.close();
            is.close();
        } catch (Exception e) {
            throw new JadxRuntimeException("Image load error", e);
        }
        return resContainer;
    }

    public static ResContainer multiFile(String name) {
        return new ResContainer(name, new ArrayList<>());
    }

    public String getName() {
        return name;
    }

    public String getFileName() {
        return name.replace("/", File.separator);
    }

    @Nullable
    public CodeWriter getContent() {
        return content;
    }

    public void setContent(@Nullable CodeWriter content) {
        this.content = content;
    }

    @Nullable
    public BufferedImage getImage() {
        return image;
    }

    @Nullable
    public Image getFxImage() {
        return fxImage;
    }

    public List<ResContainer> getSubFiles() {
        return subFiles;
    }

    @Override
    public int compareTo(@NotNull ResContainer o) {
        return name.compareTo(o.name);
    }

    @Override
    public String toString() {
        return "Res{" + name + ", subFiles=" + subFiles + "}";
    }
}
