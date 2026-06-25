import express, {Request , Response} from 'express';
import cors from "cors";
import proposalsRoutes from "./routes/proposal.routes"
import daoRoutes from "./routes/dao.routes"
import userRoutes from "./routes/users.routes"


const app = express();
app.use(cors())
app.use(express.json())

app.use("/api/proposals",proposalsRoutes);
app.use("/api/daos", daoRoutes)
app.use("/api/users", userRoutes)

app.get("/health",(req: Request,res: Response)=>{
    res.json({
        status: "ok",
        timestamp:new Date().toISOString(),
    })
})

export default app;