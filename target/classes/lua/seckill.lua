-- 秒杀库存检查和扣减的Lua脚本
-- 检查库存是否充足，充足则扣减，否则返回失败

-- 参数：
-- KEYS[1]：库存键
-- KEYS[2]：用户秒杀标记键
-- ARGV[1]：用户ID
-- ARGV[2]：商品ID

-- 1. 检查用户是否已经参与过秒杀
if redis.call('exists', KEYS[2]) == 1 then
    return -1 -- 用户已经秒杀过
end

-- 2. 检查库存是否充足
local stock = tonumber(redis.call('get', KEYS[1]))
if not stock or stock <= 0 then
    return 0 -- 库存不足
end

-- 3. 扣减库存
redis.call('decr', KEYS[1])

-- 4. 设置用户秒杀标记
redis.call('set', KEYS[2], '1', 'EX', 86400) -- 24小时过期

return 1 -- 秒杀成功