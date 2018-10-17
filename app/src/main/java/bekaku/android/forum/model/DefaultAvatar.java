package bekaku.android.forum.model;

public class DefaultAvatar {

    private String imgApiUrl;
    private String imgApiFolder;
    private boolean selected = false;

    public DefaultAvatar(String imgApiUrl, String imgApiFolder) {
        this.imgApiUrl = imgApiUrl;
        this.imgApiFolder = imgApiFolder;
    }

    public String getImgApiUrl() {
        return imgApiUrl;
    }

    public void setImgApiUrl(String imgApiUrl) {
        this.imgApiUrl = imgApiUrl;
    }

    public String getImgApiFolder() {
        return imgApiFolder;
    }

    public void setImgApiFolder(String imgApiFolder) {
        this.imgApiFolder = imgApiFolder;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
