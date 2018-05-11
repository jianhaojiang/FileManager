package com.jjh.filemanager.bean;

import org.litepal.crud.DataSupport;

/**
 * Created by jjh on 2018/5/9.
 */

public class EncryptedItem extends DataSupport{
    private String oldName;
    private String privateName;
    private String oldPath;
    private String privatePath;

    public String getOldName() {
        return oldName;
    }

    public void setOldName(String oldName) {
        this.oldName = oldName;
    }

    public String getPrivateName() {
        return privateName;
    }

    public void setPrivateName(String privateName) {
        this.privateName = privateName;
    }

    public String getOldPath() {
        return oldPath;
    }

    public void setOldPath(String oldPath) {
        this.oldPath = oldPath;
    }

    public String getPrivatePath() {
        return privatePath;
    }

    public void setPrivatePath(String privatePath) {
        this.privatePath = privatePath;
    }
}
