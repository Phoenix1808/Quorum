import {redis} from "../clients/redis.client"
import { fetchProposalById, fetchProposals } from "./snapshot.service"

const cache_ttl_sec = 60;

// Cache "best effort" hai. Redis gira to hum bas Snapshot se seedha le lenge —
// request marni nahi chahiye. Isi ek cheez ki kami se poora backend 500 de raha tha.
async function cacheGet(key: string): Promise<string | null> {
    try {
        return await redis.get(key);
    } catch (err) {
        console.warn("Cache read skip:", (err as Error).message);
        return null;   // miss jaisa treat karo
    }
}

async function cacheSet(key: string, value: string, ttl: number): Promise<void> {
    try {
        await redis.set(key, value, "EX", ttl);
    } catch (err) {
        console.warn("Cache write skip:", (err as Error).message);
        // jaan bujh ke nigal rahe hain — cache save na hona koi failure nahi
    }
}

export async function getPropsCached(params:{
    spaces: string[]; state?: string; first:number,skip:number
}){
    const {spaces,state,first,skip} = params;
    const cacheaKey =`proposals:${spaces.join(",")}:${state ?? "all"}:${first}:${skip}`;

    const cached = await cacheGet(cacheaKey)
    if(cached){
        console.log("Cache Hit :",cacheaKey)
        return JSON.parse(cached);
    }

    console.log("Cache MISS : ", cacheaKey)
    const proposals = await fetchProposals({spaces,state,skip,first});

    await cacheSet(cacheaKey,JSON.stringify(proposals),cache_ttl_sec);
    return proposals;
}

export async function getPropsCachedID(id:string){
    const cacheKey = `proposal:${id}`;
    const cached = await cacheGet(cacheKey)
    if(cached){
        console.log("cache hit: ", cacheKey)
        return JSON.parse(cached);
    } else{
     console.log("Cache Miss:", cacheKey);
    }

    const proposal = await fetchProposalById(id);
    if(proposal){
        await cacheSet(cacheKey,JSON.stringify(proposal),cache_ttl_sec);
    }
    return proposal;
}
