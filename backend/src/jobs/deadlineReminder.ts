import cron from "node-cron"
import { DeadlineCheck } from "../services/notification.services"

export function DeadRemJob(){
    cron.schedule("*/15 * * * *",()=>{
        DeadlineCheck().catch((err)=>
            console.error("Deadline job error :",err)
        );
    })
    console.log("Deadline reminder cron scheduled for every 15 min")

    DeadlineCheck().catch((err)=>console.error("Initial Check Error: ",err))
}