package org.imooc.common.route;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: yp
 * @date: 2024/12/5 10:10
 * @description:路由前缀树实现
 */
public class RouteTrie {

    private final TrieNode root;

    public RouteTrie(){
        this.root = new TrieNode("");
    }

    public void addRoute(String path, RouteHandler handler){
        String[] segments = path.split("/");
        TrieNode current = root;

        for (String segment : segments) {
            if(segment.isEmpty()) continue;
            current.children.putIfAbsent(segment, new TrieNode(segment));
            current = current.children.get(segment);
        }

        current.isEnd = true;
        current.handler = handler;
    }

    // 匹配路由
    public RouteHandler matchRoute(String path) {
        String[] segments = path.split("/"); // 按 '/' 分割路径
        List<String> segmentList = new ArrayList<>(segments.length);
        for (String segment : segments) {
            if(StringUtils.isNotEmpty(segment)){
                segmentList.add(segment);
            }
        }
        return matchRouteHelper(segmentList.toArray(new String[0]), 0, root);
    }

    private RouteHandler matchRouteHelper(String[] segments, int index, TrieNode node) {
        if (node == null) return null; // 匹配失败
        if (index == segments.length) {
            return node.isEnd ? node.handler : null; // 是否到达终点
        }

        String currentSegment = segments[index];

        // 优先匹配固定路径
        if (node.children.containsKey(currentSegment)) {
            RouteHandler result = matchRouteHelper(segments, index + 1, node.children.get(currentSegment));
            if (result != null) return result;
        }

        // 回溯：尝试匹配路径变量
        for (Map.Entry<String, TrieNode> entry : node.children.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("{") && key.endsWith("}")) { // 判断是否为路径变量
                RouteHandler result = matchRouteHelper(segments, index + 1, entry.getValue());
                if (result != null) return result;
            }
        }

        return null; // 没有匹配到
    }


    /**
     * @author: yp
     * @date: 2024/12/5 10:07
     * @description:动态路径树节点
     */
    public static class TrieNode {

        // 当前路由段
        private String segment;
        // 是否叶子节点
        private Boolean isEnd = false;
        // 子节点映射
        private Map<String, TrieNode> children;
        // 匹配成功后对应的路由处理器
        private RouteHandler handler;

        public TrieNode(String segment){
            this.segment = segment;
            children = new ConcurrentHashMap<>();
        }

        public TrieNode(){
            children = new ConcurrentHashMap<>();
        }

        public String getSegment() {
            return segment;
        }

        public void setSegment(String segment) {
            this.segment = segment;
        }

        public Boolean getEnd() {
            return isEnd;
        }

        public void setEnd(Boolean end) {
            isEnd = end;
        }

        public Map<String, TrieNode> getChildren() {
            return children;
        }

        public void setChildren(Map<String, TrieNode> children) {
            this.children = children;
        }

        public RouteHandler getHandler() {
            return handler;
        }

        public void setHandler(RouteHandler handler) {
            this.handler = handler;
        }
    }


}
