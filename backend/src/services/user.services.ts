import {redis} from "../clients/redis.client";

export async function register(address:string, fcmToken:string){
    const key = `user:${address.toLowerCase()}`;
    await redis.hset(key,{fcmToken}); //hash mai field set
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