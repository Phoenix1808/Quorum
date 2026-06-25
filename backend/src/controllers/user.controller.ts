import {Request,Response} from "express";
import { register,list,follow,unfollow } from "../services/user.services";
import { DAOs} from "../config/dao";

//post /api/users
export async function postUser(req:Request,res:Response){
    try{
        const {address,fcmToken} = req.body;
        if(!address || !fcmToken){
            res.status(400).json({error:"address & fcmToken required"})
            return;
        }
        const user = await register(address,fcmToken);
        res.status(201).json({user})
    } catch(error){
        console.error("Post Error: ", error);
        res.status(500).json({error:"Failed to Register"})
    }
}

//post /api/users/:address/follow
export async function postfollow(req:Request,res:Response){
    try{
        const address = req.params.address as string;
        const { daoId } = req.body;

        const exists = DAOs.some((d)=> d.id === daoId)
        if(!exists){
            res.status(400).json({error:"Unknown DaoID"})
            return;
        }

        await follow(address,daoId);
        const follows = await list(address);
        res.json({follows})
    } catch(err){
        console.error("PostFollow Error: ",err);
        res.status(500).json({error:"Failed To follow"})
    }
}

//delete /api/users/:address/follow/:daoId
export async function deleteFollow(req:Request,res:Response){
    try{
        const address = req.params.address as string;
        const daoId = req.params.daoId as string;
        await unfollow(address,daoId);
        const follows = await list(address);
        res.json({follows})
    } catch(err){
        console.error("DeleteFollow Error: ",err);
        res.status(500).json({error:"Failed To unfollow"})
    }
}

//get /api/users/:address/follows
export async function getFollows(req:Request,res:Response){
    try{
        const address = req.params.address as string;
        const follows = await list(address);
        res.json({follows})
    } catch(err){
        console.error("GetFollows Error: ",err);
        res.status(500).json({error:"Failed To get follows"})
    }
}
