package com.redis.application.sensitive;

import com.redis.application.util.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @ClassName SensitiveWordFilter
 * @Description: TODO
 * @Author wys
 * @Date 2021/1/5-20:43
 * @Version V1.0
 **/
@Slf4j
//@Component
public class SensitiveWordFilter {

    /**
     * 敏感词匹配规则
     */
    public static final int MinMatchType = 1;      //最小匹配规则，如：敏感词库["中国","中国人"]，语句："我是中国人"，匹配结果：我是[中国]人
    public static final int MaxMatchType = 2;      //最大匹配规则，如：敏感词库["中国","中国人"]，语句："我是中国人"，匹配结果：我是[中国人]

    /**
     * 敏感词集合
     */
    public HashMap sensitiveWordMap;

    @PostConstruct
    public void initRedisSetData(){
        log.info("=========敏感词加载开始=========");
        long start = System.currentTimeMillis();
        Set<String> sensitiveWordSet = new HashSet<>();
        InputStreamReader inputStreamReader = new InputStreamReader(SensitiveWordFilter.class.getClassLoader().getResourceAsStream("sensi_words.txt"), StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(inputStreamReader);
        try{
            for(String line = reader.readLine(); line != null; line = reader.readLine()){
                sensitiveWordSet.add(line);
           }
            initSensitiveWordMap(sensitiveWordSet);
            System.out.println("加载敏感词集合大小:"+sensitiveWordMap.size()+"   "+sensitiveWordSet.size());
        }catch(IOException e){
            log.error("敏感词加载出现异常:{}",e);
        }finally {
            try {
                if(reader!=null){
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        long end = System.currentTimeMillis();
        log.info("==========敏感词加载结束========耗时:{}========",end-start);
    }
    /**
     * 初始化敏感词库，构建DFA算法模型
     *
     * @param sensitiveWordSet 敏感词库
     */
    private void initSensitiveWordMap(Set<String> sensitiveWordSet) {
        if(CollectionUtils.isEmpty(sensitiveWordSet)){
            return;
        }
        //初始化敏感词容器，减少扩容操作
        sensitiveWordMap = new HashMap(sensitiveWordSet.size());
        String key;
        Map nowMap;
        Map<String, String> newWorMap;
        //迭代sensitiveWordSet
        Iterator<String> iterator = sensitiveWordSet.iterator();
        while (iterator.hasNext()) {
            //关键字
            key = iterator.next();
            nowMap = sensitiveWordMap;
            for (int i = 0; i < key.length(); i++) {
                //转换成char型
                char keyChar = key.charAt(i);
                //库中获取关键字
                Object wordMap = nowMap.get(keyChar);
                //如果存在该key，直接赋值，用于下一个循环获取
                if (wordMap != null) {
                    nowMap = (Map) wordMap;
                } else {
                    //不存在则，则构建一个map，同时将isEnd设置为0，因为他不是最后一个
                    newWorMap = new HashMap<>();
                    //不是最后一个
                    newWorMap.put("isEnd", "0");
                    nowMap.put(keyChar, newWorMap);
                    nowMap = newWorMap;
                }
                if (i == key.length() - 1) {
                    //最后一个
                    nowMap.put("isEnd", "1");
                }
            }
        }
        List list=new ArrayList<>();
        list.add(sensitiveWordMap);
        Object[] objects = list.toArray();
        System.out.println(Arrays.toString(objects).getBytes().length/1024+"kb");
    }
    /**
     * 判断文字是否包含敏感字符
     *
     * @param txt       文字
     * @param matchType 匹配规则 1：最小匹配规则，2：最大匹配规则
     * @return 若包含返回true，否则返回false
     */
    public boolean contains(String txt, int matchType) {
        boolean flag = false;
        for (int i = 0; i < txt.length(); i++) {
            int matchFlag = checkSensitiveWord(txt, i, matchType); //判断是否包含敏感字符
            if (matchFlag > 0) {    //大于0存在，返回true
                flag = true;
            }
        }
        return flag;
    }

    /**
     * 判断文字是否包含敏感字符
     *
     * @param txt 文字
     * @return 若包含返回true，否则返回false
     */
    public boolean contains(String txt) {
        return contains(txt, MaxMatchType);
    }
    /**
     * 检查文字中是否包含敏感字符，检查规则如下：<br>
     *
     * @param txt
     * @param beginIndex
     * @param matchType
     * @return 如果存在，则返回敏感词字符的长度，不存在返回0
     */
    private int checkSensitiveWord(String txt, int beginIndex, int matchType) {
        //敏感词结束标识位：用于敏感词只有1位的情况
        boolean flag = false;
        //匹配标识数默认为0
        int matchFlag = 0;
        char word;
        Map nowMap = sensitiveWordMap;
        for (int i = beginIndex; i < txt.length(); i++) {
            word = txt.charAt(i);
            //获取指定key
            nowMap = (Map) nowMap.get(word);
            if (nowMap != null) {//存在，则判断是否为最后一个
                //找到相应key，匹配标识+1
                matchFlag++;
                //如果为最后一个匹配规则,结束循环，返回匹配标识数
                if ("1".equals(nowMap.get("isEnd"))) {
                    //结束标志位为true
                    flag = true;
                    //最小规则，直接返回,最大规则还需继续查找
                    if (MinMatchType == matchType) {
                        break;
                    }
                }
            } else {//不存在，直接返回
                break;
            }
        }
        if (matchFlag < 2 || !flag) {//长度必须大于等于1，为词
            matchFlag = 0;
        }
        return matchFlag;
    }
}
