package com.example.machenike.wifi;

/**
 * Created by dsw on 2015/9/30.
 */
public class ImageInfor {
    private String name;
    private int imageId;

    public ImageInfor(int imageId, String name) {
        this.imageId = imageId;
        this.name = name;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
