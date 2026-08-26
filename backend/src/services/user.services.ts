import {redis} from "../clients/redis.client";

export async function register(address:string, fcmToken:string){
    const key = `user:${address.toLowerCase()}`;
    await redis.hset(key,{fcmToken}); //hash mai field set
    await redis.sadd("users", address.toLowerCase()); //global users index (cron ke liye)
    return {address:address.toLowerCase(),fcmToken}
}

//dao follow
export async function follow(address:string,daoId:string){
    const key = `user:${address.toLowerCase()}:follows`;
    await redis.sadd(key,daoId) //sadd = set mein add
}

//unfollow
export async function unfollow(address:string,daoId:string){
    const key = `user:${address.toLowerCase()}:follows`
    await redis.srem(key,daoId) //srem = set se remove
}

//list of followed dao
export async function list(address:string){
    const key = `user:${address.toLowerCase()}:follows`;
    return redis.smembers(key); //smembers => all items of set
}
// Push bhejne ke liye user ka token chahiye
export async function getToken(address:string){
    const key = `user:${address.toLowerCase()}`;
    return redis.hget(key,"fcmToken");
}

// Token invalid ho gaya (app uninstall / data clear) -> hata do, warna har cron run
// me usi dead token par bhejne ki koshish hoti rahegi.
export async function clearToken(address:string){
    const key = `user:${address.toLowerCase()}`;
    await redis.hdel(key,"fcmToken");
}

export async function getAllUsers(){
    return redis.smembers("users");
}
//list of registered users
