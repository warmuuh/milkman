package milkman.utils;

import javafx.scene.paint.Color;

public class ColorUtil {

    public static String toWeb(Color color) {
        int r = (int)Math.round(color.getRed() * 255.0);
        int g = (int)Math.round(color.getGreen() * 255.0);
        int b = (int)Math.round(color.getBlue() * 255.0);
        int o = (int)Math.round(color.getOpacity() * 255.0);
        return String.format("#%02x%02x%02x%02x" , r, g, b, o);
    }
}
