import {Request,Response} from "express";
import {DAOs} from "../config/dao"
import { fetchSpaces } from "../services/snapshot.service";

//this time we are just returning the dao llocal list not fewtching them hence no async/await required for it

export async function getDaos(req:Request,res:Response){
    try{
        const spaceIds = DAOs.map((d)=> d.space);
        const spaces = await fetchSpaces(spaceIds)
        const daos = DAOs.map((dao)=>{
            const meta = spaces.find((s)=>s.id === dao.space);
            return{
                 id: dao.id,
                 name: meta?.name ?? dao.name,         
                 space: dao.space,
                 about: meta?.about ?? "",
                 avatar: meta?.avatar ?? null,
                 followersCount: meta?.followersCount ?? 0,
                 proposalsCount: meta?.proposalsCount ?? 0,
            };
        });
      res.json({count:daos.length, daos:daos});
    } catch(err){
        console.error("getDaos error:",err);
        res.status(500).json({error: "Failed to fetch DAOs"})
    } 
}
