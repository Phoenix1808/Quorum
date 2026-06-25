//deadline check 

import { redis } from "../clients/redis.client";
import { follow, getAllUsers } from "./user.services";
import {list as getFollows} from "./user.services";
import { getPropsCached } from "./proposal.services";
import { DAOs } from "../config/dao";

const hour = 3600; const day = 86400;

//Remainder for Deadline of proposal
function getRem(end: number, now: number): "1h" | "24h" | null {
   const timeleft = end - now;
   if(timeleft<=0) return null;
    if(timeleft<=hour) return "1h";
    if(timeleft<=day) return "24h";
    return null;
}

//Preventing Duplication (like if notified already then refrain)
async function alreadySent(address:string,proposalId:string,type:string){
  const key = `sent:${address}:${proposalId}:${type}`;
  const exists = await redis.exists(key);
  return exists === 1;
}

//fxn to mark that notified
async function markSent(address:string,proposalId:string,type:string){
    const key = `sent:${address}:${proposalId}:${type}`;
    await redis.set(key,"1","EX",2 * day) //2 din baad record khud expire
}

//this fxn will trigger cron repeatedly
export async function DeadlineCheck(){
  const now = Math.floor(Date.now()/1000);
  const users = await getAllUsers();
  if(users.length==0){
    console.log("Deadline CHeck : No User")
    return;
  }
  console.log(`Dealdine Check : ${users.length} user(s)`);

  for(const address of users){
    const followsIds = await getFollows(address);
    if(followsIds.length==0) continue;

    const spaces = followsIds
    .map((id)=>DAOs.find((d)=>d.id===id)?.space)
    .filter(Boolean) as string[];
    if(spaces.length == 0) continue;

    const proposals = await getPropsCached({spaces,state:"active",first:50,skip:0});
    for(const p of proposals){
        const hrLeft = Math.round((p.end - now)/ hour);
        console.log(`  * ${p.dao.name}: "${p.title.slice(0,40)}..." -- ${hrLeft}h left`);

        const type = getRem(p.end,now)
        if(!type) continue;
        if(await alreadySent(address,p.id,type)) continue;
        console.log(`[WOULD SEND] ${address} → "${p.title}" (${p.dao.name}) closing in ${type}`)
        await markSent(address, p.id, type);
    }
  }
  console.log("Deadline Check Done ..")
}