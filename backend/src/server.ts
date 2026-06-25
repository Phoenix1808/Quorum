import "dotenv/config";
import app from "./app"
import "./clients/redis.client";

const PORT = Number(process.env.PORT) || 3000;

app.listen(PORT,()=>{
    console.log(`Server Running on ${PORT}`)
});

