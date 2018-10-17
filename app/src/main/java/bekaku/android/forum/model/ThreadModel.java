package bekaku.android.forum.model;

import java.io.Serializable;

public class ThreadModel implements Serializable {

    private int threadId = 0;
    private String subject;
    private String content;
    private String created;
    private int userAccountId = 0;
    private String userAccountName;
    private String userAccountPicture;
    private int postCount = 0;
    private int voteUp = 0;
    private int voteDown = 0;

    private boolean userVoteUp = false;
    private boolean userVoteDown = false;
    private boolean userComment = false;


    public int getThreadId() {
        return threadId;
    }

    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public int getUserAccountId() {
        return userAccountId;
    }

    public void setUserAccountId(int userAccountId) {
        this.userAccountId = userAccountId;
    }

    public String getUserAccountName() {
        return userAccountName;
    }

    public void setUserAccountName(String userAccountName) {
        this.userAccountName = userAccountName;
    }

    public String getUserAccountPicture() {
        return userAccountPicture;
    }

    public void setUserAccountPicture(String userAccountPicture) {
        this.userAccountPicture = userAccountPicture;
    }

    public int getPostCount() {
        return postCount;
    }

    public void setPostCount(int postCount) {
        this.postCount = postCount;
    }

    public int getVoteUp() {
        return voteUp;
    }

    public void setVoteUp(int voteUp) {
        this.voteUp = voteUp;
    }

    public int getVoteDown() {
        return voteDown;
    }

    public void setVoteDown(int voteDown) {
        this.voteDown = voteDown;
    }

    public boolean isUserVoteUp() {
        return userVoteUp;
    }

    public void setUserVoteUp(boolean userVoteUp) {
        this.userVoteUp = userVoteUp;
    }

    public boolean isUserVoteDown() {
        return userVoteDown;
    }

    public void setUserVoteDown(boolean userVoteDown) {
        this.userVoteDown = userVoteDown;
    }

    public boolean isUserComment() {
        return userComment;
    }

    public void setUserComment(boolean userComment) {
        this.userComment = userComment;
    }
}
