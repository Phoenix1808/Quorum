import Redis from "ioredis";

const redisUrl = process.env.REDIS_URL
if(!redisUrl){
    throw new Error("Redis Url is not set in .env");
}

export const redis = new Redis(redisUrl)
redis.on("connect",()=>console.log("Redis Connected"))
redis.on("error",(err)=> console.error("Redis Error: ", err.message));