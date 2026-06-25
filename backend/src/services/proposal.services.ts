import {redis} from "../clients/redis.client"
import { fetchProposalById, fetchProposals } from "./snapshot.service"

const cache_ttl_sec = 60;

export async function getPropsCached(params:{
    spaces: string[]; state?: string; first:number,skip:number
}){
    const {spaces,state,first,skip} = params;

    //ye line request ke parameters ko jod kr unique name banati hai 
    // jisse redis us exact request ka data store krta hai
    const cacheaKey =`proposals:${spaces.join(",")}:${state ?? "all"}:${first}:${skip}`;

    const cached = await redis.get(cacheaKey)
    if(cached){
        console.log("Cache Hit :",cacheaKey)
        return JSON.parse(cached);
    }

    console.log("Cache MISS : ", cacheaKey)
    const proposals = await fetchProposals({spaces,state,skip,first});

    await redis.set(cacheaKey,JSON.stringify(proposals),"EX",cache_ttl_sec);
    return proposals;
}

//proposal fetching id

export async function getPropsCachedID(id:string){
    const cacheKey = `proposal:${id}`;
    const cached = await redis.get(cacheKey)
    if(cached){
        console.log("cache hit: ", cacheKey)
        return JSON.parse(cached);
    } else{
     console.log("Cache Miss:", cacheKey);
    }
    
    const proposal = await fetchProposalById(id);
    if(proposal){
        await redis.set(cacheKey,JSON.stringify(proposal),"EX",60);
    }
    return proposal;
}