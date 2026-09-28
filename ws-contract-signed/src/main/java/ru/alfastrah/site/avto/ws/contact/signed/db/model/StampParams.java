package ru.alfastrah.site.avto.ws.contact.signed.db.model;

public class StampParams {
    private float llx;
    private float lly;
    private float urx;
    private float ury;
    private int fontSize;
    private int page;
    private boolean isStampVisible;

    public float getLlx() {
        return llx;
    }

    public void setLlx(float llx) {
        this.llx = llx;
    }

    public float getLly() {
        return lly;
    }

    public void setLly(float lly) {
        this.lly = lly;
    }

    public float getUrx() {
        return urx;
    }

    public void setUrx(float urx) {
        this.urx = urx;
    }

    public float getUry() {
        return ury;
    }

    public void setUry(float ury) {
        this.ury = ury;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public boolean isStampVisible() {
        return isStampVisible;
    }

    public void setStampVisible(boolean stampVisible) {
        isStampVisible = stampVisible;
    }

    @Override
    public String toString() {
        return "StampParams{" +
                "llx=" + llx +
                ", lly=" + lly +
                ", urx=" + urx +
                ", ury=" + ury +
                ", fontSize=" + fontSize +
                ", page=" + page +
                '}';
    }

}
