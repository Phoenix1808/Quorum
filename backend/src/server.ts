import "dotenv/config";
import app from "./app"
import "./clients/redis.client";
import { DeadRemJob } from "./jobs/deadlineReminder";

const PORT = Number(process.env.PORT) || 3000;

app.listen(PORT,()=>{
    console.log(`Server Running on ${PORT}`)
});
DeadRemJob();

