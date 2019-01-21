package com.tqz.dao.cache;

import com.tqz.entity.CacheEntry;
import com.tqz.entity.Seckill;
import com.tqz.utils.JedisUtils;
import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/**
 * 需要自己配置spring IOC
 */
public class RedisDao {
    private final JedisPool jedisPool;

    public RedisDao(String ip, int port) {
        jedisPool = new JedisPool(ip, port);
    }

    // Protostuff 序列化需要的工具，通过RuntimeSchema序列化
    private RuntimeSchema<Seckill> seckillObjectSchema = RuntimeSchema.createFrom(Seckill.class);
    private static RuntimeSchema<CacheEntry> seckillListSchema = RuntimeSchema.createFrom(CacheEntry.class);
    /**
     * 通过seckillId，从缓存中获取seckill对象
     * @return 获取失败返回null
     */
    public Seckill getSeckill(long seckillId) {
        return getSeckill(seckillId, null);
    }

    /**
     * 通过seckillId，向缓存中存放seckill对象
     * @return 成功返回ok
     */
    public String putSeckill(Seckill seckill) {
        return putSeckill(seckill, null);
    }

    public Seckill getSeckill(long seckillId, Jedis jedis) {
        boolean hasJedis = jedis != null;
        //redis操作逻辑
        try {
            if (!hasJedis) {
                jedis = jedisPool.getResource();
            }
            try {
                String key = "seckill:" + seckillId;
                // Redis并没有实现内部序列化操作，存对象进去要先手动序列化
                /**
                 * 序列化的方式：
                 * 1. JDK自带序列化（既然是高并发，JDK序列化速度慢，所以不可取）
                 * 2. 使用开源工具，Google的Protostuff (就用此方案！)
                 *      注意：使用Protostuff序列化的对象必须是一个POJO，要有getset方法
                 */

                // 通过key尝试获取对象的字节数组
                byte[] bytes = jedis.get(key.getBytes());
                // 缓存获取到就反序列化
                if (bytes != null) {
                    Seckill seckill = seckillObjectSchema.newMessage();
                    ProtostuffIOUtil.mergeFrom(bytes, seckill, seckillObjectSchema);
                    return seckill;
                    // 以上操作过程解释：https://www.imooc.com/video/11823 18分30秒
                }
            } finally {
                if (!hasJedis) {
                    jedis.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String putSeckill(Seckill seckill, Jedis jedis) {
        boolean hasJedis = jedis != null;
        try {
            if (!hasJedis) {
                jedis = jedisPool.getResource();
            }
            try {
                String key = "seckill:" + seckill.getSeckillId();
                // 序列化对象为字节数组
                byte[] bytes = ProtostuffIOUtil.toByteArray(seckill, seckillObjectSchema,
                        LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
                //超时缓存setex：1分钟
                int timeout = 60;
                String result = jedis.setex(key.getBytes(), timeout, bytes);
                // 上面的key存的是字节数组，则通过key取值时也要是字节数组类型，当然不用字节数组也可以
                return result;
            } finally {
                if (!hasJedis) {
                    jedis.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public List<Seckill> getSeckillList(int offset, int limit) {
       Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            try {
                String key = "seckill:seckillList;offset:" + offset + "limit:" + limit;
                byte[] bytes = jedis.get(key.getBytes());
                if (bytes != null) {
                    CacheEntry cacheEntry = seckillListSchema.newMessage();
                    ProtostuffIOUtil.mergeFrom(bytes, cacheEntry, seckillListSchema);
                    return cacheEntry.getSeckillList();
                }
            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String putSeckillList(List<Seckill> seckillList, int offset, int limit) {
       Jedis jedis = null;
       CacheEntry cacheEntry = new CacheEntry(seckillList);
        try {
            jedis = jedisPool.getResource();
            try {
                String key = "seckill:seckillList;offset:" + offset + "limit:" + limit;
                byte[] bytes = ProtostuffIOUtil.toByteArray(cacheEntry, seckillListSchema,
                        LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
                //超时缓存setex：60秒
                int timeout = 60;
                String result = jedis.setex(key.getBytes(), timeout, bytes);
                return result;
            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 从缓存获取，如果没有，则从数据库获取
     * 会用到分布式锁
     *
     * @param seckillId     id
     * @param getDataFromDb 从数据库获取的方法
     * @return 返回商品信息
     */
    public Seckill getOrPutSeckill(long seckillId, Function<Long, Seckill> getDataFromDb) {

        String lockKey = "seckill:locks:getSeckill:" + seckillId;
        String lockRequestId = UUID.randomUUID().toString();
        Jedis jedis = jedisPool.getResource();

        try {
            // 循环直到获取到数据
            while (true) {
                Seckill seckill = getSeckill(seckillId, jedis);
                if (seckill != null) {
                    return seckill;
                }
                // 尝试获取锁。
                // 锁过期时间是防止程序突然崩溃来不及解锁，而造成其他线程不能获取锁的问题。过期时间是业务容忍最长时间。
                boolean getLock = JedisUtils.tryGetDistributedLock(jedis, lockKey, lockRequestId, 1000);
                if (getLock) {
                    // 获取到锁，从数据库拿数据, 然后存redis
                    seckill = getDataFromDb.apply(seckillId);
                    putSeckill(seckill, jedis);
                    return seckill;
                }

                // 获取不到锁，睡一下，等会再出发。sleep的时间需要斟酌，主要看业务处理速度
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }
            }
        } catch (Exception ignored) {
        } finally {
            // 无论如何，最后要去解锁
            JedisUtils.releaseDistributedLock(jedis, lockKey, lockRequestId);
            jedis.close();
        }
        return null;
    }
}
