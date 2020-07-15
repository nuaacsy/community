package com.nowcoder.community.util;

import com.sun.org.apache.regexp.internal.RE;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {
    private static final Logger logger= LoggerFactory.getLogger(SensitiveFilter.class);

    private static final String REPLACEMENT="***";

    private TrieNode rootNode=new TrieNode();

    //初始化方法 在构造之前bean前实例化这个方法
    @PostConstruct
    public void init(){
        try (
            InputStream is=this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
            BufferedReader reader=new BufferedReader(new InputStreamReader(is));
        ){
            String keyword;
            while((keyword=reader.readLine())!=null){
                this.addKeyword(keyword);
            }

        }catch (IOException e){
            logger.error("加载敏感词失败："+e.getMessage());
        }
    }

    //将一个敏感词添加到前缀树中去
    private void addKeyword(String keyword){
        TrieNode tempNode=rootNode;
        for(int i=0;i<keyword.length();i++){
            char c=keyword.charAt(i);
            TrieNode subNode=tempNode.getSubNode(c);
            if(subNode==null){
                //初始化子节点
                subNode=new TrieNode();
                tempNode.addSubNode(c,subNode);
            }
            //指向子节点 下一层循环
            tempNode=subNode;
            if(i==keyword.length()-1){
                tempNode.setKeywordEnd(true);
            }
        }
    }

    /**过滤 敏感词
     *
     * @param text 待过滤文本
     * @return 过滤后的文本
     */
    public String fileter(String text){
        if(StringUtils.isBlank(text) )return null;
        TrieNode tempNode=rootNode;
        int begin=0;
        int position=0;
        StringBuilder sb=new StringBuilder();
        while(position<text.length()){
            char c=text.charAt(position);

            //跳过符号
            if(isSymbol(c)){
                if(tempNode==rootNode){
                    sb.append(c);
                    begin++;
                }
                position++;
                continue;
            }
            //检查下级节点
            tempNode=tempNode.getSubNode(c);
            if(tempNode==null){
                //以begin开头不是敏感词
                sb.append(text.charAt(begin));
                position=++begin;
                tempNode=rootNode;
            }else if(tempNode.isKeywordEnd()){
                sb.append(REPLACEMENT);
                begin=++position;
                tempNode=rootNode;
            }else {
                position++;
            }
        }
        //将最后一批字符计入结果
        sb.append(text.substring(begin));
        return sb.toString();
    }

    //判断是否为符号
    private boolean isSymbol(Character c){
        //东亚文字范围之外
        return !CharUtils.isAsciiAlphanumeric(c)&&(c<0x2E80||c>0x9FFF);
    }

    private class TrieNode{


        //关键词结束标识
        private boolean isKeywordEnd=false;

        //当前节点的子节点
        private Map<Character,TrieNode> subNodes=new HashMap<>();

        public boolean isKeywordEnd(){
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd){
            isKeywordEnd=keywordEnd;
        }

        public void addSubNode(Character c,TrieNode node){
            subNodes.put(c,node);
        }

        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }
    }






}
