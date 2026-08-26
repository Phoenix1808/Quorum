import express, {Request , Response} from 'express';
import cors from "cors";
import proposalsRoutes from "./routes/proposal.routes"
import daoRoutes from "./routes/dao.routes"
import userRoutes from "./routes/users.routes"
// TEMP: FCM delivery verify karne ke liye — test ke baad hata dena
import debugRoutes from "./routes/debug.routes"
import { messaging } from "./clients/firebase.client"


const app = express();
app.use(cors())
app.use(express.json())

app.use("/api/proposals",proposalsRoutes);
app.use("/api/daos", daoRoutes)
app.use("/api/users", userRoutes)
app.use("/api/debug", debugRoutes)   // TEMP

app.get("/health",(req: Request,res: Response)=>{
    res.json({
        status: "ok",
        timestamp:new Date().toISOString(),
        // service account load hua ya nahi — deploy ke baad turant pata chal jata hai
        push: messaging ? "on" : "off",
    })
})

export default app;