package org.imooc.core.filter.router;


import java.util.HashMap;
import java.util.Map;

/**
 * @author: yp
 * @date: 2024/12/4 15:00
 * @description:
 */
public class Configuration {

    public static final Configuration INSTANCE = new Configuration();

    public static final Configuration getInstance(){
        return INSTANCE;
    }

    private Configuration(){}

    private String registerAddress;
    private String username;
    private String password;
    private String protocol;
    private Map<String, String> registerTypeMap = new HashMap<>();

    public void setRegisterType(String key, String registerAddress){
        registerTypeMap.put(key, registerAddress);
    }

    public String getRegisterType(String type) {
        return registerTypeMap.get(type);
    }

    public String getRegisterAddress() {
        return registerAddress;
    }

    public void setRegisterAddress(String registerAddress) {
        this.registerAddress = registerAddress;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getProtocol() {
        return protocol;
    }
}
