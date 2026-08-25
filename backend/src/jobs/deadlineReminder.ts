import cron from "node-cron"
import { DeadlineCheck } from "../services/notification.services"
import { redis } from "../clients/redis.client"

export function DeadRemJob(){
    cron.schedule("*/15 * * * *",()=>{
        DeadlineCheck().catch((err)=>
            console.error("Deadline job error :",err)
        );
    })
    console.log("Deadline reminder cron scheduled for every 15 min")

    // Boot pe ek turant check — par Redis connect hone ke BAAD. enableOfflineQueue:false
    // ki wajah se connection ready hone se pehle bheja gaya command foran fail ho jata hai.
    // Redis kabhi connect na ho to ye chalta hi nahi, jo sahi hai — chalta to fail hi hota.
    redis.once("ready",()=>{
        DeadlineCheck().catch((err)=>console.error("Initial Check Error: ",err))
    })
}