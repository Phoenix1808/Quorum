//deadline check 

import { redis } from "../clients/redis.client";
import { follow, getAllUsers, getToken, clearToken } from "./user.services";
import {list as getFollows} from "./user.services";
import { messaging } from "../clients/firebase.client";
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

// Ek push bhejo. true = gaya, false = nahi gaya.
//
// notification + data DONO bhejte hain:
//   - app background me ho -> Android khud notification dikha deta hai (notification se)
//   - app foreground me ho -> onMessageReceived call hota hai, app khud dikhata hai (data se)
// Sirf data bhejte to background me delivery deprioritize ho sakti thi.
async function sendPush(address:string, token:string, p:any, type:"1h"|"24h"){
    if(!messaging){
        console.log(`[PUSH OFF] ${address} -> "${p.title}" (${type})`);
        return false;
    }

    const title = `${p.dao.name} — voting closes in ${type}`;
    const body = p.title;

    try{
        await messaging.send({
            token,
            notification: { title, body },
            data: { proposalId: String(p.id), title, body },
        });
        console.log(`[SENT] ${address} -> "${p.title}" (${type})`);
        return true;
    } catch(err:any){
        const code = err?.errorInfo?.code || err?.code || "";
        // Token mar chuka hai -> hata do, warna har 15 min isi par try hota rahega
        if(code.includes("registration-token-not-registered") || code.includes("invalid-argument")){
            console.warn(`[DEAD TOKEN] ${address} -> hata rahe hain (${code})`);
            await clearToken(address);
        } else {
            console.error(`[PUSH FAIL] ${address}:`, err?.message || err);
        }
        return false;
    }
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

    // Token nahi hai to proposals fetch karne ka bhi koi fayda nahi
    const token = await getToken(address);
    if(!token){
        console.log(`  ${address}: koi fcmToken nahi -- skip`);
        continue;
    }

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

        // markSent SIRF tab jab sach me chala gaya ho -- warna ek network hiccup par
        // user ka reminder hamesha ke liye gum ho jayega (dedup key 2 din rehti hai).
        const ok = await sendPush(address, token, p, type);
        if(ok) await markSent(address, p.id, type);
    }
  }
  console.log("Deadline Check Done ..")
}