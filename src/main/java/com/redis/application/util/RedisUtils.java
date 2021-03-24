package com.redis.application.util;

import com.redis.application.constant.GlobalExceptionEnum;
import com.redis.application.exception.OperationException;
import io.lettuce.core.RedisCommandInterruptedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @Classname RedisUtils
 * @Description redis工具类
 * @Date 2020/12/17 17:27
 * @Created by 20113370
 */
@Slf4j
@Component
public final class RedisUtils {

    private static RedisTemplate redisTemplate;

    private static StringRedisTemplate stringRedisTemplate;

    //是否禁用redis缓存虚拟数据0不禁用(开启)1禁用(不开启)

    public static Integer redisVirtualDataState;

    /**
     * 利用set将RedisTemplate注入以便将redisTemplate设置为static这样在其他需要使用的地方就不用再注入了
     */
    @Resource
    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Resource
    public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Value("${redis.virtualData.state:1}")
    public void setRedisVirtualDataState(Integer redisVirtualDataState) {
        this.redisVirtualDataState = redisVirtualDataState;
    }

    static Map<String, Object> locks = new ConcurrentHashMap<>();

    private RedisUtils() {
    }

    /**
     * 写入缓存
     * @param key
     * @param value
     * @return
     */
    public static boolean set(final String key, Object value) {
        boolean result = false;
        try {
            ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
            operations.set(key, value);
            result = true;
        } catch (Exception e) {
            log.error("Redis写入缓存出现异常:", e);
            throw new OperationException(GlobalExceptionEnum.SYSTEM_ERROR);
        }
        return result;
    }

    /**
     * 写入缓存设置时效时间
     * @param key
     * @param value
     * @return
     */
    public static boolean setEX(final String key, Object value, Long expireTime,
                              TimeUnit timeUnit) {
        try {
            ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
            operations.set(key, value);
            return redisTemplate.expire(key, expireTime, timeUnit);
        } catch (Exception e) {
            log.error("Redis写入缓存且设置时效时间出现异常:", e);
            throw new OperationException(GlobalExceptionEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 写入缓存设置时效时间默认毫秒
     * @param key
     * @param value
     * @return
     */
    public static boolean setEX(final String key, Object value, Long expireTime) {
        try {
            return setEX(key, value, expireTime, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("Redis写入缓存设置时效时间默认毫秒出现异常:", e);
            throw new OperationException(GlobalExceptionEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 获取缓存存续时间
     * @param key
     * @param timeUnit
     * @return
     */
    public static Long getExpire(final String key, TimeUnit timeUnit){
        try {
            return redisTemplate.getExpire(key,TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("Redis获取缓存存续时间出现异常:", e);
            throw new OperationException(GlobalExceptionEnum.SYSTEM_ERROR);
        }
    }
    /**
     * 批量删除对应的value
     * @param keys
     */
    public static void remove(final String... keys) {
        for (String key : keys) {
            remove(key);
        }
    }

    /**
     * 批量删除key
     * @param pattern
     */
    public static void removePattern(final String pattern) {
        try {
            Set<Serializable> keys = redisTemplate.keys(pattern);
            if (!keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.error("Redis批量删除key出现异常:", e);
            throw new OperationException(GlobalExceptionEnum.SYSTEM_ERROR);
        }

    }

    /**
     * 删除对应的value
     * @param key
     */
    public static boolean remove(final String key) {
        try {
            if (exists(key)) {
                return redisTemplate.delete(key);
            }
            return Boolean.FALSE;
        } catch (Exception e) {
            log.error("Redis删除对应的value出现异常:", e);
            throw new OperationException(GlobalExceptionEnum.SYSTEM_ERROR);
        }

    }

    /**
     * 判断缓存中是否有对应的value
     * @param key
     * @return
     */
    public static boolean exists(final String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("Redis判断缓存中是否有对应的value出现异常:", e);
            throw new OperationException(GlobalExceptionEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 读取缓存
     * @param key
     * @return
     */
    public static Object get(final String key) {
        try {
            ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
            return operations.get(key);
        } catch (Exception e) {
            log.error("Redis读取缓存出现异常:", e);
            throw new OperationException(GlobalExceptionEnum.SYSTEM_ERROR);
        }

    }

    /**
     * 哈希 添加
     * @param key
     * @param hashKey
     * @param value
     */
    public static void hmSet(String key, Object hashKey, Object value) {
        try {
            HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
            hash.put(key, hashKey, value);
        } catch (Exception e) {
            log.error("Redis添加哈希出现异常:", e);
            throw new OperationException(GlobalExceptionEnum.SYSTEM_ERROR);
        }

    }
    public static void hmSetAll(String key, Map<?,?> var2) {
        try {
            HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
            hash.putAll(key, var2);
        } catch (Exception e) {
            log.error("Redis添加哈希出现异常:", e);
            throw new OperationException(GlobalExceptionEnum.SYSTEM_ERROR);
        }
    }
    /**
     * 哈希 添加 重写将hashKey转为String
     * @param key
     * @param hashKey
     * @param value
     */
    public static void hmSet(String key, Integer hashKey, Object value) {
        try {
            HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
            hash.put(key, String.valueOf(hashKey), value);
        } catch (Exception e) {
            log.error("Redis添加哈希出现异常:", e);
            throw new OperationException(GlobalExceptionEnum.SYSTEM_ERROR);
        }

    }

    /**
     * 哈希获取数据
     * @param key
     * @param hashKey
     * @return
     */
    public static Object hmGet(String key, Object hashKey) {
        try {
            HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
            return hash.get(key, hashKey);
        } catch (Exception e) {
            log.error("Redis获取哈希数据出现异常:", e);
            throw new OperationException(GlobalExceptionEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 获取哈希键值对
     * @param key
     * @return
     */
    public static Map<Object, Object> entries(String key) {
        try {
            HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
            return hash.entries(key);
        } catch (Exception e) {
            log.error("Redis获取哈希数据出现异常:", e);
            throw new OperationException(GlobalExceptionEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 哈希获取数据
     * @param key
     * @param hashKey
     * @return
     */
    public static Object hmGet(String key, Integer hashKey) {
        try {
            HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
            return hash.get(key, String.valueOf(hashKey));
        } catch (Exception e) {
            log.error("Redis获取哈希数据出现异常:", e);
            throw new OperationException(GlobalExceptionEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 哈希删除数据
     * @param key
     * @param hashKey
     * @return
     */
    public static Object hmRemove(String key, Object hashKey) {
        try {
            HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
            return hash.delete(key, hashKey);
        } catch (Exception e) {
            log.error("Redis删除哈希出现异常:", e);
            throw new OperationException(GlobalExceptionEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 哈希删除数据
     * @param key
     * @param hashKey
     * @return
     */
    public static Object hmRemove(String key, Integer hashKey) {
        try {
            HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
            return hash.delete(key, String.valueOf(hashKey));
        } catch (Exception e) {
            log.error("Redis删除哈希出现异常:", e);
            throw new OperationException(GlobalExceptionEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 列表添加
     * @param k
     * @param v
     */
    public static Long rPush(String k, Object v) {
        try {
            ListOperations<String, Object> list = redisTemplate.opsForList();
            Long aLong = list.rightPush(k, v);
            return aLong;
        } catch (Exception e) {
            log.error("Redis添加list出现异常:", e);
            throw new OperationException(GlobalExceptionEnum.SYSTEM_ERROR);
        }

    }

    public static void rPushAll(String k, Collection<Object> values) {
        try {
            ListOperations<String, Object> list = redisTemplate.opsForList();
            list.rightPushAll(k,values);
        } catch (Exception e) {
            log.error("Redis添加list出现异常:", e);
            throw new OperationException(GlobalExceptionEnum.SYSTEM_ERROR);
        }

    }

    /**
     * 获取队列长度
     * @param k
     * @return
     */
    public static Long listSize(String k) {
        try {
            ListOperations<String, Object> list = redisTemplate.opsForList();
            Long size = list.size(k);
            return size;
        } catch (Exception e) {
            log.error("Redis添加list出现异常:", e);
            throw new OperationException(GlobalExceptionEnum.SYSTEM_ERROR);
        }
    }
    public static Object rightPopTimeOut(String k, long time,TimeUnit timeUnit) {
        try {
            ListOperations<String, Object> list = redisTemplate.opsForList();
            Object pop = list.rightPop(k, time, timeUnit);
            return pop;
        } catch (Exception e) {
            log.error("Redis弹出list元素出现异常:", e);
            throw new OperationException(GlobalExceptionEnum.SYSTEM_ERROR);
        }
    }
    public static Object rightPop(String k) {
        try {
            ListOperations<String, Object> list = redisTemplate.opsForList();
            Object pop = list.rightPop(k);
            return pop;
        } catch (Exception e) {
            log.error("Redis右弹出list元素出现异常:", e);
            throw new OperationException(GlobalExceptionEnum.SYSTEM_ERROR);
        }
    }
    public static Object leftPop(String k) {
        try {
            ListOperations<String, Object> list = redisTemplate.opsForList();
            Object pop = list.leftPop(k);
            return pop;
        } catch (Exception e) {
            log.error("Redis左弹出list元素出现异常:", e);
            throw new OperationException(GlobalExceptionEnum.SYSTEM_ERROR);
        }
    }
    /**
     * 列表获取
     * @param k
     * @param l
     * @param l1
     * @return
     */
    public static List<Object> lRange(String k, long l, long l1) {
        try {
            ListOperations<String, Object> list = redisTemplate.opsForList();
            return list.range(k, l, l1);
        } catch (Exception e) {
            log.error("Redis获取list出现异常:", e);
            throw new OperationException(GlobalExceptionEnum.SYSTEM_ERROR);
        }

    }

    /**
     * 集合添加
     * @param key
     * @param value
     */
    public static void setAdd(String key, Object value) {

        try {
            SetOperations<String, Object> set = redisTemplate.opsForSet();
            set.add(key, value);
        } catch (Exception e) {
            log.error("Redis添加set出现异常:", e);
            throw new OperationException(GlobalExceptionEnum.SYSTEM_ERROR);
        }

    }
    public static void setAdds(String key, Object ... value) {

        try {
            SetOperations<String, Object> set = redisTemplate.opsForSet();
            set.add(key, value);
        } catch (Exception e) {
            log.error("Redis添加set出现异常:", e);
            throw new OperationException(GlobalExceptionEnum.SYSTEM_ERROR);
        }
    }
    /**
     * 集合获取
     * @param key
     * @return
     */
    public static Set<Object> setMembers(String key) {
        try {
            SetOperations<String, Object> set = redisTemplate.opsForSet();
            return set.members(key);
        } catch (Exception e) {
            log.error("Redis获取set出现异常:", e);
            throw new OperationException(GlobalExceptionEnum.SYSTEM_ERROR);
        }

    }

    /**
     * 检查集合中是否存在该值
     * @param key
     * @param value
     */
    public static boolean isMember(String key, Object value) {

        try {
            SetOperations<String, Object> set = redisTemplate.opsForSet();
            return set.isMember(key, value);

        } catch (Exception e) {
            log.error("Redis检查集合中是否存在该值出现异常:", e);
            throw new OperationException(GlobalExceptionEnum.SYSTEM_ERROR);
        }

    }

    /**
     * 有序集合添加
     * @param key
     * @param value
     * @param scoure
     */
    public static void zAdd(String key, Object value, double scoure) {
        try {
            ZSetOperations<String, Object> zset = redisTemplate.opsForZSet();
            Boolean add = zset.add(key, value, scoure);
        } catch (Exception e) {
            log.error("Redis有序集合添加出现异常:", e);
            throw new OperationException(GlobalExceptionEnum.SYSTEM_ERROR);
        }

    }

    /**
     * 有序集合删除指定元素
     * @param key
     * @param value
     */
    public static void zRemove(String key, Object... value) {
        try {
            ZSetOperations<String, Object> zset = redisTemplate.opsForZSet();
            Long remove = zset.remove(key, value);
        } catch (Exception e) {
            log.error("Redis有序集合删除指定元素出现异常:", e);
            throw new OperationException(GlobalExceptionEnum.SYSTEM_ERROR);
        }

    }

    /**
     * 有序集合删除分数区间内的元素
     * @param key
     * @param scoure
     * @param scoure1
     */
    public static void zRemoveRangebyscore(String key, double scoure, double scoure1) {
        try {
            ZSetOperations<String, Object> zset = redisTemplate.opsForZSet();
            zset.removeRangeByScore(key, scoure, scoure1);
        } catch (Exception e) {
            log.error("Redis有序集合删除指定元素出现异常:", e);
            throw new OperationException(GlobalExceptionEnum.SYSTEM_ERROR);
        }

    }

    /**
     * @param
     * @param key
     * @param scoure
     * @param scoure1
     * @return java.lang.Long
     * @Description 获取分值在某区间内的数量
     * @date 2020/3/4 23:06
     * @auther lizy
     */
    public static Long getZcount(String key, double scoure, double scoure1) {
        try {
            ZSetOperations<String, Object> zset = redisTemplate.opsForZSet();
            return zset.count(key, scoure, scoure1);
        } catch (Exception e) {
            log.error("Redis获取分值在某区间内的值出现异常:", e);
            throw new OperationException(GlobalExceptionEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 有序集合获取
     * @param key
     * @param scoure
     * @param scoure1
     * @return
     */
    public static Set<Object> rangeByScore(String key, double scoure, double scoure1) {
        try {
            ZSetOperations<String, Object> zset = redisTemplate.opsForZSet();
            return zset.rangeByScore(key, scoure, scoure1);
        } catch (Exception e) {
            log.error("Redis有序集合获取出现异常:", e);
            throw new OperationException(GlobalExceptionEnum.REDIS_EXCEPTION);
        }
    }

    /**
     * 有序集合获取(由大到小且指定条数)
     * @param key
     * @param scoure
     * @param scoure1
     * @return
     */
    public static Set<Object> reverseRange(String key, Long scoure, Long scoure1) {
        try {
            ZSetOperations<String, Object> zset = redisTemplate.opsForZSet();
            return zset.reverseRange(key, scoure, scoure1);
        } catch (Exception e) {
            log.error("Redis有序集合获取(由大到小且指定条数)出现异常:", e);
            throw new OperationException(GlobalExceptionEnum.SYSTEM_ERROR);
        }

    }

    /**
     * @param
     * @param key
     * @param value
     * @return java.lang.Double
     * @Description 获取指定zset的分值score
     * @date 2020/3/4 23:06
     * @auther lizy
     */
    public static Double getReportCategoryBuyCount(String key, Object value) {
        try {
            ZSetOperations<String, Object> zset = redisTemplate.opsForZSet();
            Double d = zset.score(key, value);
            return d == null ? 0 : d;
        } catch (Exception e) {
            log.error("Redis获取指定zset的分值score出现异常:", e);
            throw new OperationException(GlobalExceptionEnum.SYSTEM_ERROR);
        }

    }

    /**
     * @param
     * @param key
     * @param value
     * @return java.lang.Long
     * @Description 获取某一元素的排名(由大到小)
     * @date 2020/3/5 8:09
     * @auther lizy
     */
    public static Long getZrevrankByValue(String key, Object value) {
        try {
            ZSetOperations<String, Object> zset = redisTemplate.opsForZSet();
            Long d = zset.reverseRank(key, value);
            return d == null ? 0 : d;
        } catch (Exception e) {
            log.error("Redis获取某一元素的排名(由大到小)出现异常:", e);
            throw new OperationException(GlobalExceptionEnum.SYSTEM_ERROR);
        }

    }

    /**
     * 有序集合增加数值
     */
    public static Double getZincrbyByValue(String key, Object value, double scoure) {

        try {
            ZSetOperations<String, Object> zset = redisTemplate.opsForZSet();
            return zset.incrementScore(key, value, scoure);
        } catch (Exception e) {
            log.error("Redis有序集合增加数值出现异常:", e);
            throw new OperationException(GlobalExceptionEnum.SYSTEM_ERROR);
        }

    }


    /**
     * @param mapper
     * @param id            主键Id也是查询数据库的Id
     * @param objectVirtual 需要作为虚拟数据的类
     * @return Object
     * @Description 查询缓存如果没有则从数据库中获取(使用selectPK ()),数据库中如果没有则设置一个默认的状态为禁用的数据
     * @Param key 为前缀 在方法中使用时会将key+id作为主键
     * @Date 11:46 2020/12/18
     * @Author lizy
     **/
//    public static Object queryAndSetSyncN(String key, IMBaseDAO mapper, Integer id,
//                                          Object objectVirtual) {
//
//        Object object = get(key);
//        if (object == null) {
//            synchronized (locks.computeIfAbsent(key, k -> new Object())) {
//                //利用redis锁id
//                object = RedisUtils.get(key);
//                if (object != null) {
//                    return object;
//                }
//                object = mapper.selectPk(id);
//                if (object != null) {
//                    RedisUtils.set(key, object);
//                } else if (redisVirtualDataState == 0) {
//                    RedisVirtualVO virtualVO = new RedisVirtualVO(id);
//                    BeanUtil.copyProperties(virtualVO, objectVirtual);
//                    //设置一个虚拟的为禁用的数据
//                    RedisUtils.setEX(key, objectVirtual, RedisConstant.VIRTUAL_EX);
//                    return objectVirtual;
//                }
//            }
//        }
//        return object;
//    }


    /**
     * 自增
     * @param key
     * @return
     */
    public static Long increment(final String key) {
        try {
            return redisTemplate.opsForValue().increment(key, 1);
        } catch (Exception e) {
            log.error("Redis自增1出现异常:", e);
            throw new OperationException(GlobalExceptionEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 自减
     * @param key
     * @return
     */
    public static Long decrement(final String key) {
        try {
            return redisTemplate.opsForValue().decrement(key, 1);
        } catch (Exception e) {
            log.error("Redis自增1出现异常:", e);
            throw new OperationException(GlobalExceptionEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 设置key过期时间默认毫秒
     * @param key
     * @param expireTime
     * @return
     */
    public static boolean expire(final String key, long expireTime) {
        try {
            return redisTemplate.expire(key, expireTime, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("Redis设置key过期时间默认毫秒出现异常:", e);
            throw new OperationException(GlobalExceptionEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 设置key过期时间
     * @param key
     * @param expireTime
     * @param timeUnit
     * @return
     */
    public static boolean expire(final String key, Long expireTime, TimeUnit timeUnit) {
        try {
            return redisTemplate.expire(key, expireTime, timeUnit);
        } catch (Exception e) {
            log.error("Redis设置key过期时间出现异常:", e);
            throw new OperationException(GlobalExceptionEnum.SYSTEM_ERROR);
        }
    }

    /**
     * redis分布式锁
     * @param key
     * @param clientId
     * @param expire
     * @return
     */
    public static boolean lock(String key, String clientId, long expire) {
        try {
            return stringRedisTemplate.opsForValue().setIfAbsent(key, clientId, expire,
                    TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("Redis设置分布式锁出现异常:", e);
            throw new OperationException(GlobalExceptionEnum.SYSTEM_ERROR);
        }
    }

    /**
     * redis释放锁
     * @param key
     * @param clientId
     */
    public static void safeUnLock(String key, String clientId) {
        try {
            String luaScript = "if redis.call('get', KEYS[1])==KEYS[2] then return redis" +
                    ".call('del', KEYS[1]) else return 0 end";
            DefaultRedisScript<Long> defaultRedisScript = new DefaultRedisScript<>();
            defaultRedisScript.setResultType(Long.class);
            defaultRedisScript.setScriptText(luaScript);
            Object execute = stringRedisTemplate.execute(defaultRedisScript,
                    Arrays.asList(key, clientId));
        } catch (Exception e) {
            log.error("Redis释放锁出现异常:", e);
            throw new OperationException(GlobalExceptionEnum.SYSTEM_ERROR);
        }
    }
}
