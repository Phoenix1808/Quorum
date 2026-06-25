import { Request,Response } from "express";
import { fetchProposals } from "../services/snapshot.service";
import { default_space } from "../config/dao";


export async function getProposals(req:Request,res:Response){
    try{
        const spPara = (req.query.spaces as string) || "";
        const spaces = spPara ? spPara.split(",") : default_space;

        const state = req.query.state as string | undefined;
        
        //pagination
        const page = Number(req.query.page) || 1;
        const first = 20;
        const skip = (page-1)*first;

        const proposals = await fetchProposals({spaces,state,first,skip});
        res.json({page,count: proposals.length, proposals});
    } catch(err){
        console.error("getProposals error: ", err)
        res.status(500).json({error:"Faild to fetch the proposals"})
    }
}