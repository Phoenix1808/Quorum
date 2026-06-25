import { Request,Response } from "express";
import { fetchProposals } from "../services/snapshot.service";
import { default_space } from "../config/dao";
import { getPropsCached } from "../services/proposal.services";
import { getPropsCachedID } from "../services/proposal.services";

export async function getProposals(req:Request,res:Response){
    try{
        const spPara = (req.query.spaces as string) || "";
        const spaces = spPara ? spPara.split(",") : default_space;

        const state = req.query.state as string | undefined;
        
        //pagination
        const page = Number(req.query.page) || 1;
        const first = 20;
        const skip = (page-1)*first;

        const proposals = await getPropsCached({spaces,state,first,skip});
        res.json({page,count: proposals.length, proposals});
    } catch(err){
        console.error("getProposals error: ", err)
        res.status(500).json({error:"Faild to fetch the proposals"})
    }
}

//cached id
export async function getProposalById(req:Request,res:Response){
    try{
        const id  = req.params.id as string; //id made string explictly //type assertion
        const proposal =  await getPropsCachedID(id);
        if(!proposal){
            res.status(404).json({error:"Proposal Not Found"});
          return;
        }
        res.json({proposal})
    } catch(err){
        console.error("getProposalByID error :",err);
        res.status(500).json({error:"Failed to fetch proposal"})
    }
}