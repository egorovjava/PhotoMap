package com.gmail.egorovsonalexey;

import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import processing.core.PConstants;
import processing.core.PGraphics;

import java.util.Date;
import java.util.HashMap;

public class PhotoMarker extends SimplePointMarker {

    public PhotoMarker(Location location, HashMap<String, Object> properties) {
        super(location, properties);
    }

    public void showTitle(PGraphics pg, float x, float y)
    {
        double _fileSize = this.getFileSize() / 1000000f;
        String title = String.format("%s; %s; %3.1f MB",
                this.getFileName(), this.getCreateDate(), _fileSize);
        pg.pushStyle();

        pg.rectMode(PConstants.CORNER);

        pg.stroke(110);
        pg.fill(255,255,255);
        pg.rect(x + radius, y + radius, pg.textWidth(title) + 6, 18, 5);

        pg.textAlign(PConstants.LEFT, PConstants.TOP);
        pg.fill(0);
        pg.text(title, x + radius + 3, y + radius + 3);


        pg.popStyle();
    }

    private String getFileName()
    {
        return this.getStringProperty("fileName");
    }

    public String getUrl()
    {
        return this.getStringProperty("url");
    }

    public Date getCreateDate() { return (Date) this.getProperty("createDate"); }

    public String getFileId() { return this.getStringProperty("fileId"); }

    private String getDevice() { return this.getStringProperty( "device"); }

    private long getFileSize() { return (long)this.getProperty("size"); }

    @Override
    public void draw(PGraphics pg, float x, float y) {
        if (!hidden) {
            //pg.fill(255, 0, 255);
            pg.fill(this.color);
            pg.ellipse(x, y, radius, radius);
            if (selected) {
                showTitle(pg, x, y);
            }
        }
    }
}
