package org.imooc.common.config;


/**
 * @author: yp
 * @date: 2024/12/2 14:52
 * @description:
 */
public class ConfigServerData {
    private String dataId;
    private String groupId;
    private String serverPrefixPath;

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getDataId() {
        return dataId;
    }

    public void setServerPrefixPath(String serverPrefixPath) {
        this.serverPrefixPath = serverPrefixPath;
    }

    public String getServerPrefixPath() {
        return serverPrefixPath;
    }
}
