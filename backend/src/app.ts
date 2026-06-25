import express, {Request , Response} from 'express';
import cors from "cors";
import proposalsRoutes from "./routes/proposal.routes"
import { Timestamp } from 'firebase-admin/firestore';   

const app = express();
app.use(cors())
app.use(express.json())

app.use("/api/proposals",proposalsRoutes);

app.get("/health",(req: Request,res: Response)=>{
    res.json({
        status: "ok",
        timestamp:new Date().toISOString(),
    })
})

export default app;