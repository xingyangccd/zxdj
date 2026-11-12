package com.example.lockdemo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

// 1.初始化库存的方法
// 2.查询库存的方法
// 3.扣减库存的方法
@Service
@Slf4j
@RequiredArgsConstructor
public class StockService {

    private final StringRedisTemplate stringRedisTemplate;

    // 关键：springboot项目自动配置的，我们不需要手动配置。
    private final RedissonClient redissonClient;

    private static final String STOCK_KEY_PREFIX = "stock:";

    // 设置分布式锁对应的key的前缀（锁的key是： lock:stock1001）
    private static final String LOCK_KEY_PREFIX = "lock:stock";

    /**
     * 扣减库存，在这里演示分布式锁的使用，使用Redisson框架自动管理分布式锁。
     * @param productId 商品ID
     * @param quantity 扣减库存数量
     * @return 结果描述
     */
    public String deductStock(Long productId, int quantity) {

        // 拼接出分布式锁的完整key
        String lockKey = LOCK_KEY_PREFIX + productId;

        // 封装一个锁对象（这个代码不是获取锁的代码，仅仅是封装一个锁对象，面向Lock对象调用API方法。）
        // 【可重入原理】Redisson 底层用 Hash 结构存锁：大 Key=锁名，Field=UUID+线程ID，Value=重入次数。
        // 同一 JVM 实例的 UUID 固定，同线程去重入时，Field 完全一致，底层执行 HINCRBY 把次数 +1 即可。
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 尝试获取锁
            // 参数1：最多等待5秒（其他线程持有锁的时候，当前线程最多等5秒）
            // 参数2：leaseTime，当你不指定 leaseTime参数时，自动启用看门狗自动续期（默认30秒，每10秒续一次）：想自动启用看门狗，第二个参数空着不写。
            // 参数3：时间单位
            //boolean acquired = lock.tryLock(5, 10 ,TimeUnit.SECONDS);

            // 这个代码是会自动启用看门狗的，并且默认30秒有效期，每10秒续一次。
            // 返回值true表示锁获取到了。false表示没有获取到锁。
            // 【避坑1：避免阻塞等待】使用 tryLock 而不是 lock()，最多等 5 秒，抢不到直接返回 false，保护 Tomcat 线程池。lock()是无限期等待
            // 【避坑2：避免锁提前释放（看门狗）】只传两个参数，刻意不传 leaseTime，以此激活看门狗机制。只要业务没跑完，后台每 10 秒自动续期，绝不过期。
            // 【避坑3：避免死锁】tryLock 底层是原子 Lua 脚本。只要加了锁，哪怕进程突然宕机，默认的 30 秒过期时间一到，锁自动释放，不会永久死锁。
            boolean acquired = lock.tryLock(5, TimeUnit.SECONDS);

            if(!acquired){
                log.warn("获取锁超时，productId={}", productId);
                return "系统繁忙，请稍后再试";
            }

            log.info("成功获取锁，productId={}, thread={}", productId, Thread.currentThread().getName());

            // 获取的库存
            String stockStr = stringRedisTemplate.opsForValue().get(STOCK_KEY_PREFIX + productId);

            if(stockStr == null){
                return "商品不存在";
            }

            // 获取的当前库存
            int currentStock = Integer.parseInt(stockStr);
            log.info("当前库存：{}，请求扣减：{}", currentStock, quantity);

            if(currentStock < quantity){
                return "库存不足，当前剩余：" + currentStock;
            }

            // 模拟业务耗时（验证看门狗续期效果，可以注释掉）
            try { Thread.sleep(35000); } catch (InterruptedException e) { }

            int newStock = currentStock - quantity;
            // 更新库存
            stringRedisTemplate.opsForValue().set(STOCK_KEY_PREFIX + productId, String.valueOf(newStock));

            log.info("扣减成功，剩余库存：{}", newStock);
            return "扣减成功，剩余库存：" + newStock;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 终止当前线程执行
            return "操作被中断";
        } finally {
            // 【避坑4：误删别人的锁】
            // 必须保证锁的释放，因此在这里编写释放锁的代码
            // 先通过 isHeldByCurrentThread 判断是不是自己的锁
            if(lock.isHeldByCurrentThread()){ // 条件成立，则表示属于当前线程的锁。可以释放锁。以防释放别人的锁。
                // 该方法底层会自动使用lua脚本，做原子性的操作：检查并释放。
                // 这样就可以保证哪怕你的业务卡了35秒（锁已过期被B抢走了），当你醒来解锁的时候也不会把B的锁释放掉。
                lock.unlock();
                log.info("释放锁，productId={}", productId);
            }
        }
    }


    /**
     * 初始化库存
     * @param productId 商品ID
     * @param stock 库存
     */
    public void initStock(Long productId, int stock) {
        // key的格式：  stock:1
        // value的格式： 10
        stringRedisTemplate.opsForValue().set(STOCK_KEY_PREFIX + productId, String.valueOf(stock));
    }

    /**
     * 查询库存
     * @param productId 商品ID
     * @return 库存数量
     */
    public Integer getStock(Long productId) {
        String stock = stringRedisTemplate.opsForValue().get(STOCK_KEY_PREFIX + productId);
        return stock == null ? 0 : Integer.parseInt(stock);
    }
}
