package jonathan.ftp.sh.pain;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.io.Serializable;

public class Line implements Serializable {

    public GeneralPath content;
    public byte thiccness;
    public Color color;

    public Line(){

    }

    public Line(GeneralPath path, byte thicc, Color iHopeItsPink) {
        content = path;
        thiccness = thicc;
        color = iHopeItsPink;
    }

    public byte getThiccness() {
        return thiccness;
    }

    public Color getColor() {
        return color;
    }

    public GeneralPath getPath() {
        return content;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setThiccness(byte thiccness) {
        this.thiccness = thiccness;
    }

    public void setPath(GeneralPath content) {
        this.content = content;
    }

    public void setContent(GeneralPath content) {
        this.content = content;
    }

    public GeneralPath getContent() {
        return content;
    }
}