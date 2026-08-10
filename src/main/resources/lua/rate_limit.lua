local key = KEYS[1]
local limit = tonumber(ARGV[1])
local window = tonumber(ARGV[2])
local now = tonumber(ARGV[3])

redis.call('ZREMRANGEBYSCORE', key, 0, now - window)
local count = redis.call('ZCARD', key)
if count >= limit then
    return -1
end

redis.call('ZADD', key, now, now .. ':' .. math.random(1000000))
redis.call('EXPIRE', key, window)
return count + 1
