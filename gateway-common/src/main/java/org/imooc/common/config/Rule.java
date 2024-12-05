package org.imooc.common.config;

import org.imooc.common.enums.HttpCommandType;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @PROJECT_NAME: api-gateway
 * @DESCRIPTION: 核心规则类
 * @USER: WuYang
 * @DATE: 2022/12/31 19:00
 */
public class Rule implements Comparable<Rule>, Serializable {

    /**
     * 规则ID，全局唯一
     */
    private String id;

    /**
     * 规则名称
     */
    private String name;

    /**
     * 协议
     */
    private String protocol;

    /**
     * 后端服务ID
     */
    private String  serviceId;
    /**
     * 请求前缀
     */
    private String prefix;
    /**
     * 路径集合
     */
    private List<PathRule> paths;
    /**
     * 规则排序，对应场景：一个路径对应多条规则，然后只执行一条规则的情况
     */
    private Integer order;

    private String version;

    private String group;

    private String interfaceName;


    private Set<FilterConfig> filterConfigs =new HashSet<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public Set<FilterConfig> getFilterConfigs() {
        return filterConfigs;
    }

    public void setFilterConfigs(Set<FilterConfig> filterConfigs) {
        this.filterConfigs = filterConfigs;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public List<PathRule> getPaths() {
        return paths;
    }

    public void setPaths(List<PathRule> paths) {
        this.paths = paths;
    }

    public Rule(){
        super();
    }

    public Rule(String id, String name, String protocol, String serviceId, String prefix, List<PathRule> paths, Integer order, Set<FilterConfig> filterConfigs) {
        this.id = id;
        this.name = name;
        this.protocol = protocol;
        this.serviceId = serviceId;
        this.prefix = prefix;
        this.paths = paths;
        this.order = order;
        this.filterConfigs = filterConfigs;
    }


    public static class FilterConfig{

        /**
         * 过滤器唯一ID
         */
        private String id;
        /**
         * 过滤器规则描述，{"timeOut":500,"balance":random}
         */
        private String config;

        public FilterConfig(String id, String config){
            this.id = id;
            this.config = config;
        }

        public FilterConfig(String id){
            this.id = id;
        }

        public FilterConfig(){

        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getConfig() {
            return config;
        }

        public void setConfig(String config) {
            this.config = config;
        }

        @Override
        public  boolean equals(Object o){
            if (this == o) return  true;

            if((o== null) || getClass() != o.getClass()){
                return false;
            }

            FilterConfig that =(FilterConfig)o;
            return id.equals(that.id);
        }

        @Override
        public  int hashCode(){
            return Objects.hash(id);
        }
    }

    /**
     * 向规则里面添加过滤器
     * @param filterConfig
     * @return
     */
     public boolean addFilterConfig(FilterConfig filterConfig){
            return filterConfigs.add(filterConfig);
     }

    /**
     * 通过一个指定的FilterID获取FilterConfig
     * @param id
     * @return
     */
     public  FilterConfig getFilterConfig(String id){
         for(FilterConfig config:filterConfigs){
             if(config.getId().equalsIgnoreCase(id)){
                return  config;
             }

         }
         return null;
     }


    public void setGroup(String group) {
        this.group = group;
    }

    public String getGroup() {
        return group;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    /**
     * 根据filterID判断当前Rule是否存在
     * @return
     */
    public boolean hashId(String id) {
        for(FilterConfig filterConfig : filterConfigs) {
            if(filterConfig.getId().equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int compareTo(Rule o) {
        int  orderCompare = Integer.compare(getOrder(),o.getOrder());
        if(orderCompare == 0){
          return getId().compareTo(o.getId());
        }
        return orderCompare;
    }

    @Override
    public  boolean equals(Object o){
        if (this == o) return  true;

        if((o== null) || getClass() != o.getClass()){
            return false;
        }

        FilterConfig that =(FilterConfig)o;
        return id.equals(that.id);
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    @Override
    public  int hashCode(){
        return Objects.hash(id);
    }

    public static class PathRule{
        private String path;
        private Boolean gray;
        /** 是否鉴权；true = 是、false = 否 */
        private Boolean auth;

        private String protocol;

        /** 服务接口；RPC、其他 */
        private String interfaceName;

        /** 服务方法；RPC#method */
        private String methodName;

        /** 参数类型(RPC 限定单参数注册)；new String[]{"java.lang.String"}、new String[]{"cn.bugstack.gateway.rpc.dto.XReq"} */
        private String parameterType;

        /** 网关接口 */
        private String uri;

        private Rule rule;

        private Boolean limit;

        private Integer requestCounts;

        public void setGray(Boolean gray) {
            this.gray = gray;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }

        public Boolean getGray() {
            return gray;
        }

        public Boolean getAuth(){
            return auth;
        }

        public void setAuth(Boolean auth) {
            this.auth = auth;
        }

        public String getProtocol() {
            return protocol;
        }

        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }

        public String getInterfaceName() {
            return interfaceName;
        }

        public void setInterfaceName(String interfaceName) {
            this.interfaceName = interfaceName;
        }

        public String getMethodName() {
            return methodName;
        }

        public void setMethodName(String methodName) {
            this.methodName = methodName;
        }

        public String getParameterType() {
            return parameterType;
        }

        public void setParameterType(String parameterType) {
            this.parameterType = parameterType;
        }

        public void setRule(Rule rule) {
            this.rule = rule;
        }

        public Rule getRule() {
            return rule;
        }

        public void setLimit(boolean limit) {
            this.limit = limit;
        }

        public void setRequestCounts(Integer requestCounts) {
            this.requestCounts = requestCounts;
        }

        public Integer getRequestCounts() {
            return requestCounts;
        }

        public Boolean getLimit() {
            return limit;
        }
    }
}
