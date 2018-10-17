package bekaku.android.forum.model;

import bekaku.android.forum.util.ApiUtil;

public class ForumSetting {

    private long id;
    private int userId;
    private String username;
    private String picture;
    private String email;
    private String serverApi;
    private String created;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getServerApi() {
        return serverApi;
    }

    public void setServerApi(String serverApi) {
        this.serverApi = serverApi;
    }

    public String getApiMainUrl(){
        return this.getServerApi()+ApiUtil.API_URI;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }
}
